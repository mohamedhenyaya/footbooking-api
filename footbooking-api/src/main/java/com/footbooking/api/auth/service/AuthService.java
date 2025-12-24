package com.footbooking.api.auth.service;

import com.footbooking.api.auth.dto.*;
import com.footbooking.api.auth.model.User;
import com.footbooking.api.auth.repository.RoleRepository;
import com.footbooking.api.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalStateException("Email déjà utilisé");
        }

        var roleUser = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Role USER manquant"));

        User user = User.builder()
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
        user.getRoles().add(roleUser);

        userRepository.save(user);

        UserDetails details = userDetailsService.loadUserByUsername(req.email());
        return new AuthResponse(jwtService.generateToken(details));
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        var userDetails = userDetailsService.loadUserByUsername(req.email());
        return new AuthResponse(jwtService.generateToken(userDetails));
    }
}
