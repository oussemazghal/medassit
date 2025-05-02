package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

public class AppointmentsActivity extends AppCompatActivity {
    private EditText searchBar;
    private LinearLayout appointmentListContainer;
    private DatabaseHelper dbHelper;
    private String selectedCategory = "Doctor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments);

        dbHelper = new DatabaseHelper(this);
        appointmentListContainer = findViewById(R.id.appointmentListContainer);
        searchBar = findViewById(R.id.searchBar);
        FloatingActionButton fab = findViewById(R.id.fab_add_appointment);

        fab.setOnClickListener(v -> showAddAppointmentDialog());
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAppointments(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadAppointmentsFromDatabase();
    }

    private void showAddAppointmentDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_appointment, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        EditText editTitle = dialogView.findViewById(R.id.editTitle);
        EditText editDate = dialogView.findViewById(R.id.editDate);
        EditText editLocation = dialogView.findViewById(R.id.editLocation);
        Button btnDoctor = dialogView.findViewById(R.id.btnDoctor);
        Button btnAnalysis = dialogView.findViewById(R.id.btnAnalysis);
        Button btnDone = dialogView.findViewById(R.id.btnDoneAppointment);

        editDate.setInputType(InputType.TYPE_NULL);
        editDate.setOnClickListener(v -> showDatePicker(editDate));

        setActiveCategory(btnDoctor, btnAnalysis);

        btnDoctor.setOnClickListener(v -> {
            selectedCategory = "Doctor";
            setActiveCategory(btnDoctor, btnAnalysis);
        });

        btnAnalysis.setOnClickListener(v -> {
            selectedCategory = "Analysis";
            setActiveCategory(btnAnalysis, btnDoctor);
        });

        btnDone.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String date = editDate.getText().toString().trim();
            String location = editLocation.getText().toString().trim();

            if (title.isEmpty() || date.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                boolean inserted = dbHelper.insertAppointment(title, date, location, selectedCategory);
                if (inserted) {
                    Toast.makeText(this, "Appointment added", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadAppointmentsFromDatabase();
                } else {
                    Toast.makeText(this, "Error adding", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    private void showEditDialog(int id, String oldTitle, String oldDate, String oldLocation, String oldCategory) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_appointment, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        EditText editTitle = dialogView.findViewById(R.id.editTitle);
        EditText editDate = dialogView.findViewById(R.id.editDate);
        EditText editLocation = dialogView.findViewById(R.id.editLocation);
        Button btnDoctor = dialogView.findViewById(R.id.btnDoctor);
        Button btnAnalysis = dialogView.findViewById(R.id.btnAnalysis);
        Button btnDone = dialogView.findViewById(R.id.btnDoneAppointment);

        editTitle.setText(oldTitle);
        editDate.setText(oldDate);
        editLocation.setText(oldLocation);

        editDate.setInputType(InputType.TYPE_NULL);
        editDate.setOnClickListener(v -> showDatePicker(editDate));

        selectedCategory = oldCategory;
        if (oldCategory.equals("Doctor")) {
            setActiveCategory(btnDoctor, btnAnalysis);
        } else {
            setActiveCategory(btnAnalysis, btnDoctor);
        }

        btnDoctor.setOnClickListener(v -> {
            selectedCategory = "Doctor";
            setActiveCategory(btnDoctor, btnAnalysis);
        });

        btnAnalysis.setOnClickListener(v -> {
            selectedCategory = "Analysis";
            setActiveCategory(btnAnalysis, btnDoctor);
        });

        btnDone.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String date = editDate.getText().toString().trim();
            String location = editLocation.getText().toString().trim();

            if (title.isEmpty() || date.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                boolean updated = dbHelper.updateAppointment(id, title, date, location, selectedCategory);
                if (updated) {
                    Toast.makeText(this, "Appointment modified", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadAppointmentsFromDatabase();
                }
            }
        });

        dialog.show();
    }

    private void showDatePicker(EditText editDate) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    editDate.setText(formattedDate);
                }, year, month, day);

        datePickerDialog.show();
    }


    private void setActiveCategory(Button active, Button inactive) {
        active.setBackgroundTintList(getColorStateList(R.color.gold_bfa36c));
        active.setTextColor(getColor(R.color.white));
        inactive.setBackgroundTintList(getColorStateList(R.color.beige_fff5dd));
        inactive.setTextColor(getColor(R.color.brown_a4895f));
    }

    private void loadAppointmentsFromDatabase() {
        appointmentListContainer.removeAllViews();
        Cursor cursor = dbHelper.getAllAppointments();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));

                View itemView = LayoutInflater.from(this).inflate(R.layout.item_appointment_card, appointmentListContainer, false);

                TextView tvTitle = itemView.findViewById(R.id.tvTitle);
                TextView tvDate = itemView.findViewById(R.id.tvDate);
                TextView tvLocation = itemView.findViewById(R.id.tvLocation);
                TextView tvCategory = itemView.findViewById(R.id.tvCategory);
                ImageView btnEdit = itemView.findViewById(R.id.ic_edit);
                ImageView btnDelete = itemView.findViewById(R.id.ic_delete);

                tvTitle.setText(title);
                tvDate.setText("Date: " + date);
                tvLocation.setText("📍 " + location);
                tvCategory.setText("Category: " + category);

                btnEdit.setOnClickListener(v -> showEditDialog(id, title, date, location, category));
                btnDelete.setOnClickListener(v -> {
                    boolean deleted = dbHelper.deleteAppointment(id);
                    if (deleted) {
                        Toast.makeText(this, "Appointment deleted", Toast.LENGTH_SHORT).show();
                        loadAppointmentsFromDatabase();
                    }
                });

                appointmentListContainer.addView(itemView);
            }
            cursor.close();
        }
    }

    private void filterAppointments(String query) {
        appointmentListContainer.removeAllViews();
        Cursor cursor = dbHelper.getAllAppointments();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));

                if (title.toLowerCase().contains(query.toLowerCase()) ||
                        location.toLowerCase().contains(query.toLowerCase()) ||
                        category.toLowerCase().contains(query.toLowerCase())) {

                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    View itemView = LayoutInflater.from(this).inflate(R.layout.item_appointment_card, appointmentListContainer, false);

                    TextView tvTitle = itemView.findViewById(R.id.tvTitle);
                    TextView tvDate = itemView.findViewById(R.id.tvDate);
                    TextView tvLocation = itemView.findViewById(R.id.tvLocation);
                    TextView tvCategory = itemView.findViewById(R.id.tvCategory);
                    ImageView btnEdit = itemView.findViewById(R.id.ic_edit);
                    ImageView btnDelete = itemView.findViewById(R.id.ic_delete);

                    tvTitle.setText(title);
                    tvDate.setText("Date: " + date);
                    tvLocation.setText("📍 " + location);
                    tvCategory.setText("Category: " + category);

                    btnEdit.setOnClickListener(v -> showEditDialog(id, title, date, location, category));
                    btnDelete.setOnClickListener(v -> {
                        boolean deleted = dbHelper.deleteAppointment(id);
                        if (deleted) {
                            Toast.makeText(this, "Appointment deleted", Toast.LENGTH_SHORT).show();
                            loadAppointmentsFromDatabase();
                        }
                    });

                    appointmentListContainer.addView(itemView);
                }
            }
            cursor.close();
        }
    }
}
