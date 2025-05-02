package com.example.myapplication;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class OpenRouterChatbot {

    private static final String API_KEY = "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";
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    public interface ResponseCallback {
        void onResponse(String response);
        void onError(String error);
    }

    public static void sendMessage(String userInput, ResponseCallback callback) {
        OkHttpClient client = new OkHttpClient();

        // Construire le JSON de la requête
        JSONObject message = new JSONObject();
        try {
            message.put("role", "user");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            message.put("content", userInput);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        JSONArray messages = new JSONArray();
        messages.put(message);

        JSONObject payload = new JSONObject();
        try {
            payload.put("model", "mistralai/mistral-7b-instruct");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            payload.put("messages", messages);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestBody body = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );

        // Préparer la requête
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("HTTP-Referer", "https://yourapp.example") // facultatif
                .addHeader("X-Title", "MedAssist Chatbot")
                .post(body)
                .build();

        // Envoyer la requête
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("HTTP error: " + response.code());
                    return;
                }

                String responseBody = response.body().string();

                try {
                    JSONObject json = new JSONObject(responseBody);
                    JSONArray choices = json.getJSONArray("choices");
                    String reply = choices.getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                            .trim();

                    callback.onResponse(reply);
                } catch (Exception e) {
                    callback.onError("Parsing error: " + e.getMessage());
                }
            }
        });
    }
}
