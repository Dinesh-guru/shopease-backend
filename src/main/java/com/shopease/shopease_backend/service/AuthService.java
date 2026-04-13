package com.shopease.shopease_backend.service;

import com.shopease.shopease_backend.dto.*;
import com.shopease.shopease_backend.entity.Role;
import com.shopease.shopease_backend.entity.User;
import com.shopease.shopease_backend.repository.UserRepository;
import com.shopease.shopease_backend.security.JwtTokenProvider;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    public JwtResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        // Create and save new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // hash password
        user.setRole(Role.USER);

        userRepository.save(user);

        // Generate token and return
        String token = jwtTokenProvider.generateToken(user.getEmail());
        return new JwtResponse(token, user.getEmail(), user.getName(), user.getRole().name());
    }

    public JwtResponse login(LoginRequest request) {
        // This validates email + password — throws if wrong
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(), request.getPassword())
        );

        // If we reach here, credentials are valid
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtTokenProvider.generateToken(user.getEmail());
        return new JwtResponse(token, user.getEmail(), user.getName(), user.getRole().name());
    }
}