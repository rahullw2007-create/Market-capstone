package com.zenithbazaar.service.chatbot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zenithbazaar.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GeminiChatProvider implements ChatProvider {
    private static final Logger logger = LoggerFactory.getLogger(GeminiChatProvider.class);
    private final MockChatProvider fallbackProvider = new MockChatProvider();

    @Override
    public String generateResponse(String userMessage) {
        String apiKey = AppConfig.get("chat.gemini.api.key", "").trim();
        if (apiKey.isEmpty() || "YOUR_API_KEY".equalsIgnoreCase(apiKey)) {
            logger.debug("Gemini API key not configured, using MockChatProvider fallback");
            return fallbackProvider.generateResponse(userMessage);
        }

        try {
            String model = AppConfig.get("chat.gemini.model", "gemini-1.5-flash");
            String endpointUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

            URL url = new URL(endpointUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);

            String systemContext = "You are ZenithBot, a helpful AI shopping assistant for ZenithBazaar multi-vendor marketplace. Keep responses polite, concise, and focused on e-commerce, products, and order assistance. User query: ";
            String jsonPayload = String.format(
                    "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}",
                    escapeJson(systemContext + userMessage)
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                    JsonObject responseJson = JsonParser.parseReader(reader).getAsJsonObject();
                    String replyText = responseJson
                            .getAsJsonArray("candidates").get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts").get(0).getAsJsonObject()
                            .get("text").getAsString();
                    return replyText.trim();
                }
            } else {
                logger.warn("Gemini API returned status code {}, using fallback", responseCode);
                return fallbackProvider.generateResponse(userMessage);
            }
        } catch (Exception e) {
            logger.error("Error communicating with Gemini API, using fallback", e);
            return fallbackProvider.generateResponse(userMessage);
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    @Override
    public String getProviderName() {
        return "gemini";
    }
}
