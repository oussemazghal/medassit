package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private RecyclerView navRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🧭 Setup Toolbar and Drawer
        drawerLayout = findViewById(R.id.drawerLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        getSupportActionBar().setTitle("MedAssist");


        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 🧭 Navigation list setup
        navRecyclerView = findViewById(R.id.navRecyclerView);
        navRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<String> navItems = new ArrayList<>();
        navItems.add("Profile");

        navItems.add("Logout");



        NavAdapter navAdapter = new NavAdapter(this, navItems, item -> {
            switch (item) {
                case "Profile":
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                    break;


                case "Logout":
                    // Créer une alerte pour confirmer la déconnexion
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Are you sure you want to log out?")
                            .setCancelable(false) // L'utilisateur doit choisir entre "Yes" ou "No"
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Si "Yes" est cliqué, procéder à la déconnexion
                                    getSharedPreferences("MyPrefs", MODE_PRIVATE)
                                            .edit()
                                            .putBoolean("isLoggedIn", false)
                                            .apply();

                                    // Rediriger vers la page de connexion
                                    Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
                                    logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(logoutIntent);
                                }
                            })
                            .setNegativeButton("No", null) // Si "No" est cliqué, fermer l'alerte
                            .show();
                    break;
            }
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        navRecyclerView.setAdapter(navAdapter);

        // 🟣 Cards navigation
        setupCardNavigation();
    }

    private void setupCardNavigation() {
        LinearLayout cardProfile = findViewById(R.id.cardProfile);
        LinearLayout cardAppointments = findViewById(R.id.cardAppointments);
        LinearLayout cardMedications = findViewById(R.id.cardMedications);
        LinearLayout cardPrescriptions = findViewById(R.id.cardPrescriptions);
        LinearLayout cardEmergency = findViewById(R.id.cardEmergency);
        LinearLayout cardSchedule = findViewById(R.id.cardSchedule);
        LinearLayout cardChatbot = findViewById(R.id.cardChatbot);
        if (cardChatbot != null) {
            cardChatbot.setOnClickListener(v -> startActivity(new Intent(this, ChatbotActivity.class)));
        }




        if (cardProfile != null) {
            cardProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }
        if (cardSchedule != null) {
            cardSchedule.setOnClickListener(v -> startActivity(new Intent(this, ScheduleActivity.class)));
        }

        if (cardAppointments != null) {
            cardAppointments.setOnClickListener(v -> startActivity(new Intent(this, AppointmentsActivity.class)));
        }

        if (cardMedications != null) {
            cardMedications.setOnClickListener(v -> startActivity(new Intent(this, MedicationsActivity.class)));
        }

        if (cardPrescriptions != null) {
            cardPrescriptions.setOnClickListener(v -> startActivity(new Intent(this, PrescriptionsActivity.class)));
        }

        if (cardEmergency != null) {
            cardEmergency.setOnClickListener(v -> startActivity(new Intent(this, EmergencyActivity.class)));
        }
    }
}
