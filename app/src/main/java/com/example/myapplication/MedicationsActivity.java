package com.example.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

public class MedicationsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView medicationsListView;
    private ArrayList<Medication> medicationList;
    private MedicationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medications);

        dbHelper = new DatabaseHelper(this);
        medicationsListView = findViewById(R.id.medicationsListView);

        FloatingActionButton fab = findViewById(R.id.fab_add_medication);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MedicationsActivity.this, AddMedicationActivity.class);
            startActivity(intent);
        });

        loadMedicationsFromDatabase();
    }

    private void loadMedicationsFromDatabase() {
        medicationList = new ArrayList<>();
        Cursor cursor = dbHelper.getAllMedications();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String dosage = cursor.getString(cursor.getColumnIndexOrThrow("dosage"));
                String frequency = cursor.getString(cursor.getColumnIndexOrThrow("frequency"));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));

                medicationList.add(new Medication(id, name, dosage, frequency, imagePath, type, time));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter = new MedicationAdapter();
        medicationsListView.setAdapter(adapter);
    }

    private class MedicationAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return medicationList.size();
        }

        @Override
        public Object getItem(int position) {
            return medicationList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return medicationList.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView != null ? convertView :
                    LayoutInflater.from(MedicationsActivity.this).inflate(R.layout.item_medication_list, parent, false);

            Medication med = medicationList.get(position);

            TextView tvName = view.findViewById(R.id.tvMedicationName);
            TextView tvInfo = view.findViewById(R.id.tvMedicationDetails);
            TextView tvType = view.findViewById(R.id.tvMedicationType);
            TextView tvTime = view.findViewById(R.id.tvMedicationTime);
            ImageView medImageView = view.findViewById(R.id.medImageCard);
            ImageView iconEdit = view.findViewById(R.id.iconEdit);
            ImageView iconDelete = view.findViewById(R.id.iconDelete);

            tvName.setText(med.name);
            tvInfo.setText(med.dosage + " - " + med.frequency);
            tvType.setText("Type: " + med.type);
            tvTime.setText("Termination Date: " + med.time);


            if (med.imagePath != null && !med.imagePath.isEmpty()) {
                File imageFile = new File(med.imagePath);
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    medImageView.setImageBitmap(bitmap);
                } else {
                    medImageView.setImageResource(R.drawable.placeholder);
                }
            } else {
                medImageView.setImageResource(R.drawable.placeholder);
            }

            iconEdit.setOnClickListener(v -> {
                Intent editIntent = new Intent(MedicationsActivity.this, EditMedicationActivity.class);
                editIntent.putExtra("medication_id", med.id);
                startActivity(editIntent);
            });

            iconDelete.setOnClickListener(v -> {
                boolean deleted = dbHelper.deleteMedicationById(med.id);
                if (deleted) {
                    Toast.makeText(MedicationsActivity.this, "Medication deleted", Toast.LENGTH_SHORT).show();
                    loadMedicationsFromDatabase();
                } else {
                    Toast.makeText(MedicationsActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                }
            });

            return view;
        }
    }

    private static class Medication {
        int id;
        String name, dosage, frequency, imagePath, type, time;

        Medication(int id, String name, String dosage, String frequency, String imagePath, String type, String time) {
            this.id = id;
            this.name = name;
            this.dosage = dosage;
            this.frequency = frequency;
            this.imagePath = imagePath;
            this.type = type;
            this.time = time;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMedicationsFromDatabase(); // Reload when coming back
    }
}
