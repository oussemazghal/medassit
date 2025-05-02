// Imports inchangés
package com.example.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class ScheduleActivity extends AppCompatActivity {

    private LinearLayout eventList;
    private DatabaseHelper dbHelper;
    private DatePicker datePicker;
    private Button btnSpeak;
    private TextToSpeech tts;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final int REQUEST_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        eventList = findViewById(R.id.eventList);
        datePicker = findViewById(R.id.datePicker);
        btnSpeak = findViewById(R.id.btnSpeak);
        dbHelper = new DatabaseHelper(this);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.getDefault());
            }
        });

        updateEventList(getSelectedDate(datePicker));

        datePicker.init(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format("%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
                    updateEventList(selectedDate);
                });

        btnSpeak.setOnClickListener(v -> startVoiceInput());
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void updateEventList(String selectedDate) {
        eventList.removeAllViews();
        ArrayList<EventItem> events = new ArrayList<>();

        Cursor medicationsCursor = dbHelper.getMedicationsByDate(selectedDate);
        if (medicationsCursor != null && medicationsCursor.moveToFirst()) {
            do {
                String medName = medicationsCursor.getString(medicationsCursor.getColumnIndexOrThrow("name"));
                String frequency = medicationsCursor.getString(medicationsCursor.getColumnIndexOrThrow("frequency"));

                if (frequency.equalsIgnoreCase("Twice daily")) {
                    events.add(new EventItem(0, "00:00 - You need to take " + medName));
                    events.add(new EventItem(12, "12:00 - You need to take " + medName));
                } else if (frequency.equalsIgnoreCase("Once daily")) {
                    events.add(new EventItem(8, "08:00 - You need to take " + medName));
                } else if (frequency.equalsIgnoreCase("Three times a day")) {
                    events.add(new EventItem(8, "08:00 - You need to take " + medName));
                    events.add(new EventItem(13, "13:00 - You need to take " + medName));
                    events.add(new EventItem(20, "20:00 - You need to take " + medName));
                } else if (frequency.equalsIgnoreCase("Every 6 hours")) {
                    events.add(new EventItem(0, "00:00 - You need to take " + medName));
                    events.add(new EventItem(6, "06:00 - You need to take " + medName));
                    events.add(new EventItem(12, "12:00 - You need to take " + medName));
                    events.add(new EventItem(18, "18:00 - You need to take " + medName));
                } else {
                    events.add(new EventItem(24, "Unscheduled - You need to take " + medName));
                }
            } while (medicationsCursor.moveToNext());
            medicationsCursor.close();
        }

        Cursor appointmentsCursor = dbHelper.getAppointmentsByDate(selectedDate);
        if (appointmentsCursor != null && appointmentsCursor.moveToFirst()) {
            do {
                String description = appointmentsCursor.getString(appointmentsCursor.getColumnIndexOrThrow("title"));
                events.add(new EventItem(-1, "📅 You have an appointment today: " + description));
            } while (appointmentsCursor.moveToNext());
            appointmentsCursor.close();
        }

        Collections.sort(events, Comparator.comparingInt(e -> e.hour));

        if (events.isEmpty()) {
            addEventCard("No events for this day.");
        } else {
            for (EventItem event : events) {
                addEventCard(event.text);
            }
        }
    }

    private void addEventCard(String content) {
        TextView eventView = new TextView(this);
        eventView.setText(content);
        eventView.setPadding(32, 32, 32, 32);
        eventView.setBackgroundColor(getResources().getColor(android.R.color.white));
        eventView.setTextColor(getResources().getColor(android.R.color.black));
        eventView.setBackground(getResources().getDrawable(android.R.drawable.dialog_holo_light_frame));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 16);
        eventView.setLayoutParams(params);
        eventList.addView(eventView);
    }

    private String getSelectedDate(DatePicker picker) {
        return String.format("%04d-%02d-%02d", picker.getYear(), picker.getMonth() + 1, picker.getDayOfMonth());
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...");
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Votre appareil ne supporte pas la reconnaissance vocale", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                handleSpokenText(spokenText);
            }
        }
    }

    private void handleSpokenText(String spokenText) {
        String lowerCaseText = spokenText.toLowerCase(Locale.ROOT);

        if (lowerCaseText.contains("rendez-vous") || lowerCaseText.contains("appointment")) {
            speakAppointmentsToday();
        } else if (lowerCaseText.contains("médicament") || lowerCaseText.contains("medicine")) {
            speakMedicationsToday();
        } else if (lowerCaseText.contains("aujourd'hui") || lowerCaseText.contains("today")) {
            speakText("Voici les événements d'aujourd'hui.");
            updateEventList(sdf.format(System.currentTimeMillis()));
        } else {
            speakText("Vous avez dit : " + spokenText);
        }
    }

    private void speakAppointmentsToday() {
        String today = getSelectedDate(datePicker);
        Cursor cursor = dbHelper.getAppointmentsByDate(today);

        if (cursor != null && cursor.moveToFirst()) {
            StringBuilder message = new StringBuilder("Voici vos rendez-vous pour aujourd'hui : ");
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                message.append(title).append(". ");
            } while (cursor.moveToNext());
            cursor.close();
            speakText(message.toString());
        } else {
            speakText("Vous n'avez aucun rendez-vous aujourd'hui.");
        }
    }

    private void speakMedicationsToday() {
        String today = getSelectedDate(datePicker);
        Cursor cursor = dbHelper.getMedicationsByDate(today);

        if (cursor != null && cursor.moveToFirst()) {
            StringBuilder message = new StringBuilder("Voici vos médicaments pour aujourd'hui : ");
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String frequency = cursor.getString(cursor.getColumnIndexOrThrow("frequency"));

                if (frequency.equalsIgnoreCase("Twice daily")) {
                    message.append("à 00 heures, ").append(name).append(". ");
                    message.append("à 12 heures, ").append(name).append(". ");
                } else if (frequency.equalsIgnoreCase("Once daily")) {
                    message.append("à 08 heures, ").append(name).append(". ");
                } else if (frequency.equalsIgnoreCase("Three times a day")) {
                    message.append("à 08 heures, ").append(name).append(". ");
                    message.append("à 13 heures, ").append(name).append(". ");
                    message.append("à 20 heures, ").append(name).append(". ");
                } else if (frequency.equalsIgnoreCase("Every 6 hours")) {
                    message.append("à 00 heures, ").append(name).append(". ");
                    message.append("à 06 heures, ").append(name).append(". ");
                    message.append("à 12 heures, ").append(name).append(". ");
                    message.append("à 18 heures, ").append(name).append(". ");
                } else {
                    message.append(name).append(". ");
                }
            } while (cursor.moveToNext());
            cursor.close();
            speakText(message.toString());
        } else {
            speakText("Vous n'avez aucun médicament à prendre aujourd'hui.");
        }
    }

    private void speakText(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private static class EventItem {
        int hour;
        String text;

        EventItem(int hour, String text) {
            this.hour = hour;
            this.text = text;
        }
    }
}
