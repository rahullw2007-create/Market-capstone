package com.zenithbazaar.security;

import com.zenithbazaar.dto.UserDto;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SessionUtil {
    public static final String USER_SESSION_KEY = "LOGGED_IN_USER";

    private SessionUtil() {}

    public static void createSession(HttpServletRequest request, UserDto user) {
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute(USER_SESSION_KEY, user);
    }

    public static UserDto getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (UserDto) session.getAttribute(USER_SESSION_KEY);
    }

    public static void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
