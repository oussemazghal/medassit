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

public class EditPrescriptionActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTitle, editSource;
    private ImageView imagePrescription;
    private Button btnUpdate;
    private String imagePath = "";
    private int prescriptionId;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_prescription);

        editTitle = findViewById(R.id.editTitle);
        editSource = findViewById(R.id.editSource);
        imagePrescription = findViewById(R.id.imagePrescription);
        btnUpdate = findViewById(R.id.btnUpdate);

        dbHelper = new DatabaseHelper(this);

        // 🛑 Récupérer les données envoyées depuis PrescriptionsActivity
        Intent intent = getIntent();
        if (intent != null) {
            prescriptionId = intent.getIntExtra("id", -1);
            String title = intent.getStringExtra("title");
            String source = intent.getStringExtra("source");
            imagePath = intent.getStringExtra("imageUri");

            editTitle.setText(title);
            editSource.setText(source);

            if (imagePath != null && !imagePath.isEmpty()) {
                imagePrescription.setImageURI(Uri.parse(imagePath));
            } else {
                imagePrescription.setImageResource(R.drawable.placeholder);
            }
        }

        // 🔵 Changer l'image si on clique dessus
        imagePrescription.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickIntent, PICK_IMAGE_REQUEST);
        });

        // 🔵 Sauvegarder les modifications
        btnUpdate.setOnClickListener(v -> {
            String updatedTitle = editTitle.getText().toString().trim();
            String updatedSource = editSource.getText().toString().trim();

            if (updatedTitle.isEmpty() || updatedSource.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean updated = dbHelper.updatePrescription(prescriptionId, updatedTitle, updatedSource, imagePath);
            if (updated) {
                Toast.makeText(this, "Prescription updated successfully", Toast.LENGTH_SHORT).show();
                finish(); // Retourner à PrescriptionsActivity
            } else {
                Toast.makeText(this, "Failed to update prescription", Toast.LENGTH_SHORT).show();
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
