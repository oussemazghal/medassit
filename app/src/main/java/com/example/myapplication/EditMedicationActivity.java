package com.example.myapplication;

import android.content.Intent;
import android.database.Cursor;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditMedicationActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 2;

    private EditText editMedName, editType, editDosage, editFrequency, editTime;
    private ImageView medImage;
    private Button btnUpdate, btnDelete;

    private DatabaseHelper dbHelper;
    private int medicationId;
    private String imagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_medication);

        editMedName = findViewById(R.id.editMedName);
        editType = findViewById(R.id.editType);
        editDosage = findViewById(R.id.editDosage);
        editFrequency = findViewById(R.id.editFrequency);
        editTime = findViewById(R.id.editTime);
        medImage = findViewById(R.id.medImage);
        btnUpdate = findViewById(R.id.btnUpdateMedication);
        btnDelete = findViewById(R.id.btnDeleteMedication);

        dbHelper = new DatabaseHelper(this);

        medicationId = getIntent().getIntExtra("medication_id", -1);
        if (medicationId == -1) {
            finish();
            return;
        }

        loadMedicationDetails(medicationId);

        medImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnUpdate.setOnClickListener(v -> {
            String name = editMedName.getText().toString().trim();
            String type = editType.getText().toString().trim();
            String dosage = editDosage.getText().toString().trim();
            String frequency = editFrequency.getText().toString().trim();
            String time = editTime.getText().toString().trim();

            if (name.isEmpty() || type.isEmpty() || dosage.isEmpty() || frequency.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Si aucun nouveau chemin d'image n’a été sélectionné, garder l’ancien
                if (imagePath == null || imagePath.isEmpty()) {
                    Cursor cursor = dbHelper.getMedicationById(medicationId);
                    if (cursor != null && cursor.moveToFirst()) {
                        imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));
                        cursor.close();
                    }
                }

                boolean success = dbHelper.updateMedication(medicationId, name, type, dosage, frequency, time, imagePath);
                if (success) {
                    Toast.makeText(this, "Medication updated", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDelete.setOnClickListener(v -> {
            boolean success = dbHelper.deleteMedication(medicationId);
            if (success) {
                Toast.makeText(this, "Medication deleted", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMedicationDetails(int id) {
        Cursor cursor = dbHelper.getMedicationById(id);
        if (cursor != null && cursor.moveToFirst()) {
            editMedName.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            editType.setText(cursor.getString(cursor.getColumnIndexOrThrow("type")));
            editDosage.setText(cursor.getString(cursor.getColumnIndexOrThrow("dosage")));
            editFrequency.setText(cursor.getString(cursor.getColumnIndexOrThrow("frequency")));
            editTime.setText(cursor.getString(cursor.getColumnIndexOrThrow("time")));
            imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));

            if (imagePath != null && !imagePath.isEmpty()) {
                File file = new File(imagePath);
                if (file.exists()) {
                    medImage.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                }
            }
            cursor.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    InputStream inputStream = null;
                    try {
                        inputStream = getContentResolver().openInputStream(selectedImageUri);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    if (inputStream != null) {
                        String fileName = "med_image_" + System.currentTimeMillis() + ".jpg";
                        File file = new File(getFilesDir(), fileName);
                        FileOutputStream outputStream = new FileOutputStream(file);

                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, len);
                        }

                        outputStream.close();
                        inputStream.close();

                        imagePath = file.getAbsolutePath();
                        medImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Image load failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
