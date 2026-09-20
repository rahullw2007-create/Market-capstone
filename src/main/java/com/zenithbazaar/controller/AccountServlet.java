package com.zenithbazaar.controller;

import com.zenithbazaar.dto.LoginRequest;
import com.zenithbazaar.dto.RegisterRequest;
import com.zenithbazaar.dto.UserDto;
import com.zenithbazaar.security.SessionUtil;
import com.zenithbazaar.service.UserService;
import com.zenithbazaar.utility.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/account/*")
public class AccountServlet extends BaseServlet {
    private final UserService userService;

    public AccountServlet() {
        this.userService = new UserService();
    }

    public AccountServlet(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if ("/me".equals(pathInfo)) {
                UserDto user = SessionUtil.getCurrentUser(req);
                if (user != null) {
                    sendSuccess(resp, user);
                } else {
                    sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
                }
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if ("/register".equals(pathInfo)) {
                RegisterRequest regReq = JsonUtil.parseRequestBody(req, RegisterRequest.class);
                UserDto registeredUser = userService.register(regReq);
                SessionUtil.createSession(req, registeredUser);
                sendCreated(resp, registeredUser, "Registration successful");
            } else if ("/login".equals(pathInfo)) {
                LoginRequest loginReq = JsonUtil.parseRequestBody(req, LoginRequest.class);
                UserDto user = userService.login(loginReq);
                SessionUtil.createSession(req, user);
                sendSuccess(resp, user, "Login successful");
            } else if ("/logout".equals(pathInfo)) {
                SessionUtil.invalidateSession(req);
                sendSuccess(resp, null, "Logged out successfully");
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            handleError(resp, e);
        }
    }
}
