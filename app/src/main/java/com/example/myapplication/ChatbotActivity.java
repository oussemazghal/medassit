package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class ChatbotActivity extends AppCompatActivity {

    private EditText inputField;
    private TextView responseText;
    private ImageButton sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        inputField = findViewById(R.id.inputField);
        responseText = findViewById(R.id.responseText);
        sendBtn = findViewById(R.id.sendBtn);

        sendBtn.setOnClickListener(v -> {
            String message = inputField.getText().toString().trim();
            if (!message.isEmpty()) {
                OpenRouterChatbot.sendMessage(message, new OpenRouterChatbot.ResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        runOnUiThread(() -> responseText.setText("🤖 " + response));
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> responseText.setText("❌ Error: " + error));
                    }
                });

            }
        });
    }
}
