package com.metrotecnica.api.controller;

import com.metrotecnica.api.dto.LoginRequest;
import com.metrotecnica.api.dto.LoginResponse;
import com.metrotecnica.api.model.User;
import com.metrotecnica.api.repository.UserRepository;
import com.metrotecnica.api.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "E-mail ou senha inválidos"));
        }

        Long tenantId = user.getTenant() != null ? user.getTenant().getId() : null;
        String token = jwtService.generateToken(user.getEmail(), user.getRole(), tenantId);

        return ResponseEntity.ok(new LoginResponse(token, user.getRole()));
    }
}