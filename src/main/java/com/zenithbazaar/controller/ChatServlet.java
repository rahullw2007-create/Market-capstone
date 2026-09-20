package com.zenithbazaar.controller;

import com.zenithbazaar.dto.ChatRequest;
import com.zenithbazaar.dto.ChatResponse;
import com.zenithbazaar.service.chatbot.ChatbotService;
import com.zenithbazaar.utility.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/chat")
public class ChatServlet extends BaseServlet {
    private final ChatbotService chatbotService;

    public ChatServlet() {
        this.chatbotService = new ChatbotService();
    }

    public ChatServlet(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ChatRequest chatReq = JsonUtil.parseRequestBody(req, ChatRequest.class);
            ChatResponse chatRes = chatbotService.processChat(chatReq);
            sendSuccess(resp, chatRes);
        } catch (Exception e) {
            handleError(resp, e);
        }
    }
}
