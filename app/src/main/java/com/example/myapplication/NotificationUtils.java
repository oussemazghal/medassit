package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotificationUtils {

    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleMedicationReminder(Context context, long triggerTimeMillis, String medName, String dosage, String imagePath) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("med_name", medName);
        intent.putExtra("dosage", dosage);
        intent.putExtra("image_path", imagePath);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
    }
}
