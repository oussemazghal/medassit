package com.example.myapplication;

import android.content.Intent;
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

public class AddPrescriptionActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTitle, editSource;
    private ImageView imagePrescription;
    private Button btnSave;
    private String imagePath = "";
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_prescription);

        editTitle = findViewById(R.id.editTitle);
        editSource = findViewById(R.id.editSource);
        imagePrescription = findViewById(R.id.imagePrescription);
        btnSave = findViewById(R.id.btnSave);

        dbHelper = new DatabaseHelper(this);

        imagePrescription.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String source = editSource.getText().toString().trim();

            if (title.isEmpty() || source.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = dbHelper.insertPrescription(title, source, imagePath);
            if (success) {
                Toast.makeText(this, "Prescription added successfully", Toast.LENGTH_SHORT).show();
                finish(); // Retourner à PrescriptionsActivity
            } else {
                Toast.makeText(this, "Failed to add prescription", Toast.LENGTH_SHORT).show();
            }
        });
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
                        String fileName = "prescription_image_" + System.currentTimeMillis() + ".jpg";
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
                        imagePrescription.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Image load failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
