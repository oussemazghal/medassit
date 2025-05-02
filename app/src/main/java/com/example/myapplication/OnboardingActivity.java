package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.fragment.app.Fragment; // ✅ Correct

import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class OnboardingActivity extends AppCompatActivity {

    private Button btnNext, btnBack;
    private View dot1, dot2, dot3;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);

        showPage(currentPage);

        btnNext.setOnClickListener(v -> {
            if (currentPage < 2) {
                currentPage++;
                showPage(currentPage);
            } else {
                startActivity(new Intent(this, GetStartedActivity.class));
                finish();
            }
        });

        btnBack.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                showPage(currentPage);
            }
        });
    }

    private void showPage(int page) {
        Fragment fragment;

        switch (page) {
            case 0:
                fragment = new OnboardingFragment1();
                btnBack.setVisibility(View.INVISIBLE);
                btnNext.setText("Next");
                break;
            case 1:
                fragment = new OnboardingFragment2();
                btnBack.setVisibility(View.VISIBLE);
                btnNext.setText("Next");
                break;
            case 2:
                fragment = new OnboardingFragment3();
                btnBack.setVisibility(View.VISIBLE);
                btnNext.setText("Finish");
                break;
            default:
                return;
        }

        // Change le fragment affiché dans le container
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();

        // Met à jour les dots
        updateDots(page);
    }

    private void updateDots(int position) {
        dot1.setBackgroundResource(R.drawable.dot_inactive);
        dot2.setBackgroundResource(R.drawable.dot_inactive);
        dot3.setBackgroundResource(R.drawable.dot_inactive);

        switch (position) {
            case 0: dot1.setBackgroundResource(R.drawable.dot_active); break;
            case 1: dot2.setBackgroundResource(R.drawable.dot_active); break;
            case 2: dot3.setBackgroundResource(R.drawable.dot_active); break;
        }
    }
}
