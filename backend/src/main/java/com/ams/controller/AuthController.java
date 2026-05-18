package com.ams.controller;

import com.ams.config.JwtUtil;
import com.ams.dto.LoginRequest;
import com.ams.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final long TOKEN_EXPIRATION_MS = 86400000L; // 24 hours in milliseconds

    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        if (ADMIN_USERNAME.equals(request.getUsername()) && ADMIN_PASSWORD.equals(request.getPassword())) {
            Long userId = 1L; // TODO: lookup from employee table by username
            String token = jwtUtil.generateToken(ADMIN_USERNAME, ADMIN_ROLE, userId, TOKEN_EXPIRATION_MS);

            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .expiresIn(TOKEN_EXPIRATION_MS / 1000)
                    .role(ADMIN_ROLE)
                    .username(ADMIN_USERNAME)
                    .userId(userId)
                    .build();

            log.info("Login successful for user: {}", request.getUsername());
            return ResponseEntity.ok(response);
        }

        log.warn("Login failed for user: {} - Invalid credentials", request.getUsername());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid credentials"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication, HttpServletRequest request) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.replace("ROLE_", ""))
                .orElse("UNKNOWN");

        // Extract userId from JWT token
        Long userId = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            userId = jwtUtil.getUserIdFromToken(token);
        }

        return ResponseEntity.ok(Map.of(
                "username", username,
                "role", role,
                "userId", userId != null ? userId : 0,
                "authorities", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        ));
    }
}
