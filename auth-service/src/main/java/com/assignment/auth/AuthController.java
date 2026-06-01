package com.assignment.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final SecretKey key;
    private final List<AppUser> users = List.of(
            new AppUser(101L, "manager1@company.com", "password", "MANAGER", "Ananya Manager"),
            new AppUser(102L, "manager2@company.com", "password", "MANAGER", "Rohit Manager"),
            new AppUser(201L, "employee1@company.com", "password", "EMPLOYEE", "Jagdeep Employee"),
            new AppUser(202L, "employee2@company.com", "password", "EMPLOYEE", "Arjun Employee"),
            new AppUser(203L, "employee3@company.com", "password", "EMPLOYEE", "Meera Employee")
    );

    public AuthController(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        log.info("login attempt for {}", request.email());
        AppUser user = users.stream()
                .filter(item -> item.email().equalsIgnoreCase(request.email()) && item.password().equals(request.password()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject(String.valueOf(user.id()))
                .claim("role", user.role())
                .claim("email", user.email())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600 * 8)))
                .signWith(key)
                .compact();

        log.info("login success for {} as {} ({})", user.email(), user.role(), user.id());
        return new LoginResponse(token, user.id(), user.name(), user.email(), user.role(), 28800);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResponse invalidLogin(Exception ex) {
        log.warn("login failed: {}", ex.getMessage());
        return new ErrorResponse(ex.getMessage());
    }

    record LoginRequest(String email, String password) {
    }

    record LoginResponse(String token, Long userId, String name, String email, String role, long expiresInSeconds) {
    }

    record ErrorResponse(String message) {
    }

    record AppUser(Long id, String email, String password, String role, String name) {
    }
}
