package com.example.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private DatabaseHelper dbHelper;
    private String currentUsername;

    private EditText usernameEditText, ageEditText, genderEditText, bloodTypeEditText,
            weightEditText, heightEditText, allergiesEditText, phoneEditText, addressEditText;
    private ImageView profileImage, cameraIcon;
    private Button saveButton;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUsername = prefs.getString("username", "yossra");

        // Initialisation des vues
        profileImage = findViewById(R.id.profileImage);
        cameraIcon = findViewById(R.id.cameraIcon);
        usernameEditText = findViewById(R.id.usernameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        genderEditText = findViewById(R.id.genderEditText);
        bloodTypeEditText = findViewById(R.id.bloodTypeEditText);
        weightEditText = findViewById(R.id.weightEditText);
        heightEditText = findViewById(R.id.heightEditText);
        allergiesEditText = findViewById(R.id.allergiesEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        addressEditText = findViewById(R.id.addressEditText);
        saveButton = findViewById(R.id.saveButton);

        cameraIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        saveButton.setOnClickListener(v -> saveProfileData());

        loadProfileData();
    }

    private void loadProfileData() {
        Cursor cursor = dbHelper.getProfileByUsername(currentUsername);
        if (cursor.moveToFirst()) {
            usernameEditText.setText(currentUsername);
            ageEditText.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("age"))));
            genderEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
            bloodTypeEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow("blood_type")));
            weightEditText.setText(String.valueOf(cursor.getFloat(cursor.getColumnIndexOrThrow("weight"))));
            heightEditText.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("height"))));
            allergiesEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow("allergies")));
            phoneEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            addressEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow("address")));

            String imageUriStr = cursor.getString(cursor.getColumnIndexOrThrow("image_uri"));
            if (imageUriStr != null && !imageUriStr.isEmpty()) {
                imageUri = Uri.parse(imageUriStr);
                try {
                    File file = new File(imageUri.getPath());
                    if (file.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        profileImage.setImageBitmap(bitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to load local image", Toast.LENGTH_SHORT).show();
                }
            }
        }
        cursor.close();
    }

    private void saveProfileData() {
        try {
            String newUsername = usernameEditText.getText().toString().trim();
            int age = parseIntOrZero(ageEditText.getText().toString());
            String gender = genderEditText.getText().toString();
            String bloodType = bloodTypeEditText.getText().toString();
            float weight = parseFloatOrZero(weightEditText.getText().toString());
            int height = parseIntOrZero(heightEditText.getText().toString());
            String allergies = allergiesEditText.getText().toString();
            String phone = phoneEditText.getText().toString();
            String address = addressEditText.getText().toString();
            String imageUriStr = (imageUri != null) ? imageUri.toString() : "";

            if (!newUsername.equals(currentUsername)) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues userValues = new ContentValues();
                userValues.put("username", newUsername);
                db.update("users", userValues, "username=?", new String[]{currentUsername});

                ContentValues profileValues = new ContentValues();
                profileValues.put("username", newUsername);
                db.update("user_profile", profileValues, "username=?", new String[]{currentUsername});
                db.close();

                getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        .edit()
                        .putString("username", newUsername)
                        .apply();

                currentUsername = newUsername;
            }

            boolean success = dbHelper.saveOrUpdateProfile(
                    currentUsername, age, gender, bloodType, weight, height, allergies, phone, address, imageUriStr
            );

            Toast.makeText(this, success ? "Profile updated successfully ✅" : "Save error ❌", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Invalid entry: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int parseIntOrZero(String value) {
        try {
            return value.trim().isEmpty() ? 0 : Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private float parseFloatOrZero(String value) {
        try {
            return value.trim().isEmpty() ? 0f : Float.parseFloat(value.trim());
        } catch (Exception e) {
            return 0f;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    if (inputStream != null) {
                        String fileName = "profile_image_" + currentUsername + ".jpg";
                        File file = new File(getFilesDir(), fileName);
                        FileOutputStream outputStream = new FileOutputStream(file);

                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, len);
                        }

                        outputStream.close();
                        inputStream.close();

                        imageUri = Uri.fromFile(file);
                        profileImage.setImageURI(imageUri);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error importing image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
