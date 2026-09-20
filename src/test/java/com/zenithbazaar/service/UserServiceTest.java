package com.zenithbazaar.service;

import com.zenithbazaar.TestDatabase;
import com.zenithbazaar.dto.LoginRequest;
import com.zenithbazaar.dto.RegisterRequest;
import com.zenithbazaar.dto.UserDto;
import com.zenithbazaar.exception.ForbiddenException;
import com.zenithbazaar.exception.UnauthorizedException;
import com.zenithbazaar.exception.ValidationException;
import com.zenithbazaar.model.Role;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private static UserService userService;

    @BeforeAll
    static void setUp() {
        TestDatabase.setupInMemoryDatabase();
        userService = new UserService();
    }

    @Test
    void testRegisterCustomerAndLogin() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("newcustomer@test.com");
        req.setPassword("Password123!");
        req.setFullName("Test Customer");
        req.setRole(Role.CUSTOMER);

        UserDto registered = userService.register(req);
        assertNotNull(registered.getId());
        assertEquals("newcustomer@test.com", registered.getEmail());

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("newcustomer@test.com");
        loginReq.setPassword("Password123!");

        UserDto loggedIn = userService.login(loginReq);
        assertEquals(registered.getId(), loggedIn.getId());
    }

    @Test
    void testProhibitAdminSelfRegistration() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("fakeadmin@test.com");
        req.setPassword("Password123!");
        req.setFullName("Fake Admin");
        req.setRole(Role.ADMINISTRATOR);

        assertThrows(ForbiddenException.class, () -> userService.register(req));
    }

    @Test
    void testInvalidPasswordLogin() {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("admin@zenithbazaar.com");
        loginReq.setPassword("WrongPassword!");

        assertThrows(UnauthorizedException.class, () -> userService.login(loginReq));
    }
}
