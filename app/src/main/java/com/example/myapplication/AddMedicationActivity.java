package com.example.myapplication;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class AddMedicationActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editMedName, editDosage, editStartDate, editEndDate;
    private Spinner spinnerType, spinnerFrequency;
    private Button btnAddMedication;
    private ImageView medImage;
    private String imagePath = "";
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Initialiser composants
        editMedName = findViewById(R.id.editMedName);
        editDosage = findViewById(R.id.editDosage);
        editStartDate = findViewById(R.id.editStartDate);
        editEndDate = findViewById(R.id.editEndDate);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerFrequency = findViewById(R.id.spinnerFrequency);
        btnAddMedication = findViewById(R.id.btnAddMedication);
        medImage = findViewById(R.id.medImage);

        dbHelper = new DatabaseHelper(this);

        setupSpinners();
        setupDatePickers();

        // Choix de l'image
        medImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnAddMedication.setOnClickListener(v -> {
            String name = editMedName.getText().toString().trim();
            String type = spinnerType.getSelectedItem().toString();
            String dosage = editDosage.getText().toString().trim();
            String frequency = spinnerFrequency.getSelectedItem().toString();
            String startDate = editStartDate.getText().toString().trim();
            String endDate = editEndDate.getText().toString().trim();

            if (name.isEmpty() || type.isEmpty() || dosage.isEmpty() || frequency.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                boolean success = dbHelper.insertMedication(name, type, dosage, frequency, startDate, endDate, imagePath);
                if (success) {
                    scheduleAllRemindersForFrequency(frequency, name, dosage, imagePath);
                    Toast.makeText(this, "Medication added with reminders", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MedicationsActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Failed to add medication", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.medication_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> freqAdapter = ArrayAdapter.createFromResource(this,
                R.array.medication_frequencies, android.R.layout.simple_spinner_item);
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(freqAdapter);
    }

    private void setupDatePickers() {
        editStartDate.setOnClickListener(v -> showDatePickerDialog(editStartDate));
        editEndDate.setOnClickListener(v -> showDatePickerDialog(editEndDate));
    }

    private void showDatePickerDialog(EditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
            targetEditText.setText(formattedDate);
        }, year, month, day);

        datePickerDialog.show();
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

    private void scheduleAllRemindersForFrequency(String frequency, String medName, String dosage, String imagePath) {
        int[] hours;

        if (frequency.equalsIgnoreCase("1 fois par jour")) {
            hours = new int[]{8};
        } else if (frequency.equalsIgnoreCase("2 fois par jour")) {
            hours = new int[]{8, 20};
        } else if (frequency.equalsIgnoreCase("3 fois par jour")) {
            hours = new int[]{8, 14, 20};
        } else {
            hours = new int[]{8}; // valeur par défaut
        }

        for (int hour : hours) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);

            // Si l'heure est passée pour aujourd'hui, on programme pour demain
            if (cal.before(Calendar.getInstance())) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            scheduleMedicationReminder(cal.getTimeInMillis(), medName, dosage, imagePath);
        }
    }

    private void scheduleMedicationReminder(long triggerTimeMillis, String medName, String dosage, String imagePath) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("med_name", medName);
        intent.putExtra("dosage", dosage);
        intent.putExtra("image_path", imagePath);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot schedule exact alarm on this device", Toast.LENGTH_SHORT).show();
        }
    }
}
