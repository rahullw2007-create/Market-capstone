package com.zenithbazaar.service;

import com.zenithbazaar.dto.LoginRequest;
import com.zenithbazaar.dto.RegisterRequest;
import com.zenithbazaar.dto.UserDto;
import com.zenithbazaar.exception.ForbiddenException;
import com.zenithbazaar.exception.NotFoundException;
import com.zenithbazaar.exception.UnauthorizedException;
import com.zenithbazaar.exception.ValidationException;
import com.zenithbazaar.model.Role;
import com.zenithbazaar.model.User;
import com.zenithbazaar.repository.UserDao;
import com.zenithbazaar.security.PasswordUtil;
import com.zenithbazaar.utility.ValidationUtil;

import java.util.List;
import java.util.stream.Collectors;

public class UserService {
    private final UserDao userDao;

    public UserService() {
        this(new UserDao());
    }

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserDto register(RegisterRequest req) {
        if (req == null) {
            throw new ValidationException("Registration request cannot be null");
        }
        ValidationUtil.validateEmail(req.getEmail());
        ValidationUtil.validateMinLength(req.getPassword(), 6, "Password");
        ValidationUtil.requireNotBlank(req.getFullName(), "Full Name");

        if (req.getRole() == null || req.getRole() == Role.ADMINISTRATOR) {
            throw new ForbiddenException("Administrator self-registration is strictly prohibited");
        }

        if (userDao.findByEmail(req.getEmail()).isPresent()) {
            throw new ValidationException("An account with this email address already exists");
        }

        User user = new User();
        user.setEmail(req.getEmail().trim());
        user.setPasswordHash(PasswordUtil.hashPassword(req.getPassword()));
        user.setFullName(req.getFullName().trim());
        user.setRole(req.getRole());
        user.setActive(true);

        User saved = userDao.save(user);
        return new UserDto(saved);
    }

    public UserDto login(LoginRequest req) {
        if (req == null) {
            throw new ValidationException("Login request cannot be null");
        }
        ValidationUtil.validateEmail(req.getEmail());
        ValidationUtil.requireNotBlank(req.getPassword(), "Password");

        User user = userDao.findByEmail(req.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Your account has been deactivated. Please contact support.");
        }

        if (!PasswordUtil.checkPassword(req.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return new UserDto(user);
    }

    public UserDto getUserById(Long id) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return new UserDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userDao.findAll().stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
    }

    public boolean updateUserStatus(Long userId, boolean active) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getRole() == Role.ADMINISTRATOR && !active) {
            throw new ValidationException("Cannot deactivate an administrator account");
        }
        return userDao.updateStatus(userId, active);
    }
}
