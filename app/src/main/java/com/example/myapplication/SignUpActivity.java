package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, usernameEditText, passwordEditText;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        dbHelper = new DatabaseHelper(this);

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        TextView loginText = findViewById(R.id.loginText);
        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.signUpButton).setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Validation du nom (doit pas contenir de chiffres)
            if (name.matches(".*\\d.*")) {
                Toast.makeText(SignUpActivity.this, "Le nom ne peut pas contenir de chiffres", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validation de l'adresse e-mail (doit contenir @gmail.com)
            if (!email.endsWith("@gmail.com")) {
                Toast.makeText(SignUpActivity.this, "L'adresse e-mail doit être un compte Gmail", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validation du mot de passe (doit contenir au moins 6 caractères)
            if (password.length() < 6) {
                Toast.makeText(SignUpActivity.this, "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show();
                return;
            }

            if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                boolean success = dbHelper.insertUser(name, email, username, password);
                if (success) {
                    Toast.makeText(SignUpActivity.this, "Sign up successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
