package com.zenithbazaar.service.chatbot;

import com.zenithbazaar.config.AppConfig;
import com.zenithbazaar.dto.ChatRequest;
import com.zenithbazaar.dto.ChatResponse;
import com.zenithbazaar.exception.ValidationException;
import com.zenithbazaar.utility.ValidationUtil;

public class ChatbotService {
    private final ChatProvider provider;

    public ChatbotService() {
        String configuredProvider = AppConfig.get("chat.provider", "mock");
        if ("gemini".equalsIgnoreCase(configuredProvider)) {
            this.provider = new GeminiChatProvider();
        } else {
            this.provider = new MockChatProvider();
        }
    }

    public ChatbotService(ChatProvider provider) {
        this.provider = provider;
    }

    public ChatResponse processChat(ChatRequest request) {
        if (request == null) {
            throw new ValidationException("Chat request cannot be empty");
        }
        ValidationUtil.requireNotBlank(request.getMessage(), "Message");

        String msg = request.getMessage().trim();
        if (msg.length() > 500) {
            throw new ValidationException("Message length exceeds maximum limit of 500 characters");
        }

        String reply = provider.generateResponse(msg);
        return new ChatResponse(reply, provider.getProviderName());
    }
}
