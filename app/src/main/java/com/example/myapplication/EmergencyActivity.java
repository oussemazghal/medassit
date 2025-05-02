package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EmergencyActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int MAX_CLICK_DURATION = 10000;
    private static final int MAX_CLICKS = 5;

    private EditText searchBar;
    private Cursor allContactsCursor;
    private Uri selectedImageUri = null;

    private DatabaseHelper dbHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private RecyclerView recyclerContacts;
    private ContactAdapter contactAdapter;

    private int clickCount = 0;
    private long firstClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }
        });

        recyclerContacts = findViewById(R.id.recyclerContacts);
        recyclerContacts.setLayoutManager(new GridLayoutManager(this, 2));

        FloatingActionButton btnEmergency = findViewById(R.id.btn_emergency);
        btnEmergency.setOnClickListener(v -> detectEmergencyClick());

        FloatingActionButton btnSetNumber = findViewById(R.id.btn_set_number);
        btnSetNumber.setOnClickListener(v -> showSetNumberDialog());

        loadContactsFromDatabase();
    }

    private void detectEmergencyClick() {
        long currentTime = System.currentTimeMillis();
        if (firstClickTime == 0 || (currentTime - firstClickTime) > MAX_CLICK_DURATION) {
            firstClickTime = currentTime;
            clickCount = 1;
        } else {
            clickCount++;
        }

        if (clickCount >= MAX_CLICKS) {
            clickCount = 0;
            firstClickTime = 0;
            sendEmergencySMS();
        } else {
            showAddContactDialog();
        }
    }

    private void sendEmergencySMS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        String number = getEmergencyNumber();
                        String message = "🚨 Emergency! Here is my location: https://maps.google.com/?q=" +
                                location.getLatitude() + "," + location.getLongitude();

                        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                        smsIntent.setData(Uri.parse("smsto:" + number));
                        smsIntent.putExtra("sms_body", message);

                        try {
                            startActivity(smsIntent);
                        } catch (Exception e) {
                            Toast.makeText(this, "Failed to send SMS.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "⚠️ Location unavailable.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Location error.", Toast.LENGTH_SHORT).show());
    }

    private void showSetNumberDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter emergency number");
        input.setText(getEmergencyNumber());

        new AlertDialog.Builder(this)
                .setTitle("Set Emergency Number")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String number = input.getText().toString().trim();
                    if (!number.isEmpty()) {
                        saveEmergencyNumber(number);
                        Toast.makeText(this, "Emergency number saved.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Invalid number.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveEmergencyNumber(String number) {
        SharedPreferences prefs = getSharedPreferences("emergency_prefs", MODE_PRIVATE);
        prefs.edit().putString("emergency_number", number).apply();
    }

    private String getEmergencyNumber() {
        SharedPreferences prefs = getSharedPreferences("emergency_prefs", MODE_PRIVATE);
        return prefs.getString("emergency_number", "+21695203939"); // default number
    }

    public void showAddContactDialog(Integer contactId, String nameInit, String phoneInit, String imageUriInit) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        EditText editName = dialogView.findViewById(R.id.editName);
        EditText editPhone = dialogView.findViewById(R.id.editPhone);
        Button btnUploadImage = dialogView.findViewById(R.id.btnUploadImage);
        Button btnDone = dialogView.findViewById(R.id.btnDone);

        if (nameInit != null) editName.setText(nameInit);
        if (phoneInit != null) editPhone.setText(phoneInit);
        if (imageUriInit != null && !imageUriInit.isEmpty()) {
            selectedImageUri = Uri.parse(imageUriInit);
        }

        btnUploadImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnDone.setOnClickListener(view -> {
            String name = editName.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String imageUriStr = selectedImageUri != null ? selectedImageUri.toString() : "";

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            } else {
                boolean result;
                if (contactId == null) {
                    result = dbHelper.insertEmergencyContact(name, phone, imageUriStr);
                    Toast.makeText(this, result ? "Contact added." : "Error adding contact.", Toast.LENGTH_SHORT).show();
                } else {
                    result = dbHelper.updateEmergencyContact(contactId, name, phone, imageUriStr);
                    Toast.makeText(this, result ? "Contact updated." : "Error updating contact.", Toast.LENGTH_SHORT).show();
                }

                if (result) {
                    dialog.dismiss();
                    selectedImageUri = null;
                    loadContactsFromDatabase();
                }
            }
        });

        dialog.show();
    }

    public void showAddContactDialog() {
        showAddContactDialog(null, null, null, null);
    }

    private void loadContactsFromDatabase() {
        if (allContactsCursor != null) allContactsCursor.close();

        Cursor cursor = dbHelper.getAllEmergencyContacts();
        allContactsCursor = copyCursor(cursor);
        cursor.close();

        contactAdapter = new ContactAdapter(this, allContactsCursor, dbHelper, this::loadContactsFromDatabase);
        recyclerContacts.setAdapter(contactAdapter);
    }

    private void filterContacts(String query) {
        if (allContactsCursor == null) return;

        MatrixCursor filteredCursor = new MatrixCursor(new String[]{"id", "name", "phone", "image_uri"});
        allContactsCursor.moveToPosition(-1);

        while (allContactsCursor.moveToNext()) {
            String name = allContactsCursor.getString(allContactsCursor.getColumnIndexOrThrow("name"));
            String phone = allContactsCursor.getString(allContactsCursor.getColumnIndexOrThrow("phone"));
            String imageUri = allContactsCursor.getString(allContactsCursor.getColumnIndexOrThrow("image_uri"));
            int id = allContactsCursor.getInt(allContactsCursor.getColumnIndexOrThrow("id"));

            if (name.toLowerCase().contains(query.toLowerCase()) || phone.toLowerCase().contains(query.toLowerCase())) {
                filteredCursor.addRow(new Object[]{id, name, phone, imageUri});
            }
        }

        contactAdapter.swapCursor(filteredCursor);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Toast.makeText(this, "Image selected.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                sendEmergencySMS();
            } else {
                Toast.makeText(this, "Permissions denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private MatrixCursor copyCursor(Cursor source) {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"id", "name", "phone", "image_uri"});
        if (source != null && source.moveToFirst()) {
            do {
                int id = source.getInt(source.getColumnIndexOrThrow("id"));
                String name = source.getString(source.getColumnIndexOrThrow("name"));
                String phone = source.getString(source.getColumnIndexOrThrow("phone"));
                String imageUri = source.getString(source.getColumnIndexOrThrow("image_uri"));
                matrixCursor.addRow(new Object[]{id, name, phone, imageUri});
            } while (source.moveToNext());
        }
        return matrixCursor;
    }
}
