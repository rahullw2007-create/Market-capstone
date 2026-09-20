package com.zenithbazaar.service.chatbot;

import java.util.Locale;

public class MockChatProvider implements ChatProvider {

    @Override
    public String generateResponse(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Hello! I am ZenithBot, your virtual shopping assistant. How can I help you today?";
        }
        String msg = userMessage.toLowerCase(Locale.ROOT);

        if (msg.contains("order") || msg.contains("track") || msg.contains("status") || msg.contains("shipping")) {
            return "You can view your order history and status by logging in and navigating to the 'Orders' section from the top navigation bar.";
        } else if (msg.contains("return") || msg.contains("refund") || msg.contains("policy")) {
            return "ZenithBazaar offers a 30-day hassle-free return policy for delivered items. Contact support for assistance with returns.";
        } else if (msg.contains("vendor") || msg.contains("sell") || msg.contains("store")) {
            return "Interested in selling on ZenithBazaar? Register as a Vendor to create products, manage inventory, and view incoming orders!";
        } else if (msg.contains("payment") || msg.contains("pay") || msg.contains("card")) {
            return "We currently support simulated payments for quick testing during checkout. No credit card is charged!";
        } else if (msg.contains("headphone") || msg.contains("keyboard") || msg.contains("watch") || msg.contains("product") || msg.contains("item")) {
            return "We have great electronics, fashion, and home products! Browse our Catalogue page to explore active listings and filter by category.";
        } else {
            return "Thank you for reaching out to ZenithBot! I can assist with product search, order status, vendor registration, and site navigation. How may I assist you further?";
        }
    }

    @Override
    public String getProviderName() {
        return "mock";
    }
}
