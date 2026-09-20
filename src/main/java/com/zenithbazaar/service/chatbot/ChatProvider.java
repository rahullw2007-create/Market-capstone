package com.zenithbazaar.service.chatbot;

public interface ChatProvider {
    String generateResponse(String userMessage);
    String getProviderName();
}
