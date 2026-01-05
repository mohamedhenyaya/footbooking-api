package com.footbooking.api.auth.service;

import com.footbooking.api.auth.dto.*;
import com.footbooking.api.auth.model.User;
import com.footbooking.api.auth.repository.RoleRepository;
import com.footbooking.api.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    // Récupère l'URL Cloudflare du serveur Node.js depuis application.properties
    @Value("${whatsapp.service.url}")
    private String whatsappServiceUrl;

    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    /**
     * Génère et envoie un code OTP par WhatsApp via le tunnel Cloudflare
     */
    public void sendWhatsAppOtp(String phoneNumber) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(phoneNumber, new OtpData(otp));

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> body = new HashMap<>();

        String cleanNumber = phoneNumber.replaceAll("\\D", "");
        if (cleanNumber.length() == 8) {
            cleanNumber = "222" + cleanNumber;
        }

        body.put("number", cleanNumber);
        body.put("message", "Votre code de vérification FootBooking est : " + otp);

        try {
            // Utilise l'URL dynamique au lieu de localhost:8082
            restTemplate.postForEntity(whatsappServiceUrl + "/send-otp", body, String.class);
            System.out.println("✅ OTP envoyé via tunnel à : " + cleanNumber);
        } catch (Exception e) {
            System.out.println("❌ Erreur Tunnel Node.js: " + e.getMessage());
            System.out.println("👉 CODE OTP (Backup Console): " + otp);
        }
    }

    /**
     * Valide l'inscription finale
     */
    public AuthResponse registerWithWhatsApp(WhatsAppRegisterRequest req) {
        // Validation de l'OTP
        validateOtp(req.phoneNumber(), req.otpCode());

        if (userRepository.existsByEmail(req.phoneNumber())) {
            throw new IllegalStateException("Ce numéro est déjà enregistré");
        }

        User user = User.builder()
                .email(req.phoneNumber())
                .phone(req.phoneNumber())
                .password(passwordEncoder.encode(req.password()))
                .name("Joueur " + req.phoneNumber())
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .roles(new HashSet<>())
                .build();

        userRepository.save(user);

        otpStorage.remove(req.phoneNumber());
        UserDetails details = userDetailsService.loadUserByUsername(req.phoneNumber());
        return new AuthResponse(jwtService.generateToken(details));
    }

    /**
     * Réinitialise le mot de passe
     */
    public void resetPassword(WhatsAppRegisterRequest req) {
        validateOtp(req.phoneNumber(), req.otpCode());

        User user = userRepository.findByEmail(req.phoneNumber())
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));

        user.setPassword(passwordEncoder.encode(req.password()));
        userRepository.save(user);

        otpStorage.remove(req.phoneNumber());
        System.out.println("✅ Mot de passe réinitialisé pour : " + req.phoneNumber());
    }

    /**
     * Méthode utilitaire interne pour valider l'OTP
     */
    private void validateOtp(String phone, String code) {
        OtpData savedOtpData = otpStorage.get(phone);

        if (savedOtpData == null || !savedOtpData.code.equals(code)) {
            throw new IllegalStateException("Code OTP invalide");
        }

        if (savedOtpData.isExpired()) {
            otpStorage.remove(phone);
            throw new IllegalStateException("Le code OTP a expiré");
        }
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        var userDetails = userDetailsService.loadUserByUsername(req.email());
        return new AuthResponse(jwtService.generateToken(userDetails));
    }

    public AuthMeResponse getMe(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var roles = user.getRoles().stream()
                .map(r -> "ROLE_" + r.getName())
                .toList();

        return new AuthMeResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                roles);
    }

    public void createAdmin(CreateAdminRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalStateException("Un utilisateur avec cet email existe déjà");
        }

        var adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("Role ADMIN not found"));

        var roles = new HashSet<com.footbooking.api.auth.model.Role>();
        roles.add(adminRole);

        User user = User.builder()
                .name(req.name())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .roles(roles)
                .build();

        userRepository.save(user);
    }

    // Classe interne pour les données OTP
    private static class OtpData {
        String code;
        LocalDateTime expiryTime;

        OtpData(String code) {
            this.code = code;
            this.expiryTime = LocalDateTime.now().plusMinutes(5);
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }
}