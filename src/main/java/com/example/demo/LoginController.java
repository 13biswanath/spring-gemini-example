package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;


@RestController
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request, HttpSession session) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Username and password are required");
        }

        // If existing user, validate password
        if (userService.userExists(username)) {
            if (userService.validateUser(username, password)) {
                session.setAttribute("username", username);
                return ResponseEntity.ok("LOGIN_OK");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("INVALID_CREDENTIALS");
            }
        }

        // New user → register and log in
        userService.registerUser(username, password);
        session.setAttribute("username", username);
        return ResponseEntity.ok("REGISTERED");
    }

    @PostMapping("/api/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    // Simple DTO class for JSON body
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
    }
}
