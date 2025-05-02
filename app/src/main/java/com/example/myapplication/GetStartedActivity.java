package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class GetStartedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        // Bouton "Get Started"
        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aller vers l'activité d'inscription (SignUpActivity)
                Intent intent = new Intent(GetStartedActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish(); // Fermer cette activité
            }
        });
    }
}
