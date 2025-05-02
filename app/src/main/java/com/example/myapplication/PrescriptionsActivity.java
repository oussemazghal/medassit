package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class PrescriptionsActivity extends AppCompatActivity {

    private LinearLayout prescriptionList;
    private EditText searchEditText;
    private DatabaseHelper dbHelper;
    private ArrayList<Prescription> allPrescriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescriptions);

        prescriptionList = findViewById(R.id.prescriptionList);
        searchEditText = findViewById(R.id.searchEditText);
        dbHelper = new DatabaseHelper(this);

        loadPrescriptionsFromDatabase();

        ImageButton uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(v -> {
            startActivity(new Intent(PrescriptionsActivity.this, AddPrescriptionActivity.class));
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPrescriptions(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPrescriptionsFromDatabase();
    }

    private void loadPrescriptionsFromDatabase() {
        prescriptionList.removeAllViews();
        allPrescriptions = new ArrayList<>();

        Cursor cursor = dbHelper.getAllPrescriptions();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String source = cursor.getString(cursor.getColumnIndexOrThrow("source"));
                String imageUri = cursor.getString(cursor.getColumnIndexOrThrow("image_uri"));

                allPrescriptions.add(new Prescription(id, title, source, imageUri));
            } while (cursor.moveToNext());
            cursor.close();
        }

        displayPrescriptions(allPrescriptions);
    }

    private void displayPrescriptions(ArrayList<Prescription> list) {
        prescriptionList.removeAllViews();

        for (Prescription prescription : list) {
            addPrescriptionCard(prescription);
        }
    }

    private void addPrescriptionCard(Prescription prescription) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_prescription, prescriptionList, false);

        TextView titleText = card.findViewById(R.id.prescriptionTitle);
        TextView sourceText = card.findViewById(R.id.prescriptionSource);
        ImageView image = card.findViewById(R.id.prescriptionImage);
        ImageButton shareButton = card.findViewById(R.id.shareButton);
        ImageButton deleteButton = card.findViewById(R.id.deleteButton);

        titleText.setText(prescription.title);
        sourceText.setText(prescription.source);

        if (prescription.imageUri != null && !prescription.imageUri.isEmpty()) {
            image.setImageURI(Uri.parse(prescription.imageUri));

            image.setOnLongClickListener(v -> {
                showFullImage(prescription.imageUri);
                return true;
            });
        } else {
            image.setImageResource(R.drawable.placeholder);
        }

        shareButton.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Prescription: " + prescription.title + "\nFrom: " + prescription.source);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        deleteButton.setOnClickListener(v -> {
            boolean deleted = dbHelper.deletePrescription(prescription.id);
            if (deleted) {
                Toast.makeText(this, "Prescription deleted", Toast.LENGTH_SHORT).show();
                loadPrescriptionsFromDatabase();
            } else {
                Toast.makeText(this, "Failed to delete prescription", Toast.LENGTH_SHORT).show();
            }
        });

        card.setOnClickListener(v -> {
            Intent editIntent = new Intent(PrescriptionsActivity.this, EditPrescriptionActivity.class);
            editIntent.putExtra("id", prescription.id);
            editIntent.putExtra("title", prescription.title);
            editIntent.putExtra("source", prescription.source);
            editIntent.putExtra("imageUri", prescription.imageUri);
            startActivity(editIntent);
        });

        prescriptionList.addView(card);
    }

    private void filterPrescriptions(String text) {
        ArrayList<Prescription> filteredList = new ArrayList<>();
        for (Prescription pres : allPrescriptions) {
            if (pres.title.toLowerCase().contains(text.toLowerCase()) || pres.source.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(pres);
            }
        }
        displayPrescriptions(filteredList);
    }

    private void showFullImage(String imageUri) {
        if (imageUri == null || imageUri.isEmpty()) return;

        View fullView = LayoutInflater.from(this).inflate(R.layout.dialog_full_image, null);
        ImageView fullImageView = fullView.findViewById(R.id.fullImageView);
        fullImageView.setImageURI(Uri.parse(imageUri));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(fullView);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static class Prescription {
        int id;
        String title;
        String source;
        String imageUri;

        Prescription(int id, String title, String source, String imageUri) {
            this.id = id;
            this.title = title;
            this.source = source;
            this.imageUri = imageUri;
        }
    }
}
