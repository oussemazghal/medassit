package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // ton layout splash avec logo ou citation

        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
            boolean hasSeenOnboarding = prefs.getBoolean("hasSeenOnboarding", false);

            Intent intent;
            if (isLoggedIn) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else if (!hasSeenOnboarding) {
                intent = new Intent(SplashActivity.this, OnboardingActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();

        }, 3000); // délai de 3 secondes
    }
}
