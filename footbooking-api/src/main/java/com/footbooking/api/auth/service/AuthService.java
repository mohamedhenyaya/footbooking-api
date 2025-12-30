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
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
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

    // Stockage temporaire des codes OTP (Clé: Numéro de téléphone, Valeur: Code)
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    /**
     * Génère et envoie un code OTP par WhatsApp
     */
    public void sendWhatsAppOtp(String phoneNumber) {
        String otp = String.format("%06d", new Random().nextInt(999999));

        // On enregistre l'objet OtpData (qui contient le timer de 5 min)
        otpStorage.put(phoneNumber, new OtpData(otp));

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> body = new HashMap<>();

        // 1. On nettoie le numéro (enlève tout ce qui n'est pas un chiffre)
        String cleanNumber = phoneNumber.replaceAll("\\D", "");

        // 2. On s'assure que le numéro commence par 222 (Mauritanie)
        // Si l'utilisateur a saisi 8 chiffres (ex: 42302133), on ajoute 222
        if (cleanNumber.length() == 8) {
            cleanNumber = "222" + cleanNumber;
        }

        body.put("number", cleanNumber);
        body.put("message", "Votre code de vérification FootBooking est : " + otp);

        try {
            restTemplate.postForEntity("http://localhost:8082/send-otp", body, String.class);
            System.out.println("✅ OTP envoyé au : " + cleanNumber);
        } catch (Exception e) {
            System.out.println("❌ Erreur Node.js: " + e.getMessage());
            System.out.println("👉 CODE OTP (Console): " + otp);
        }
    }
    /**
     * Valide l'inscription finale avec OTP et Mot de passe
     */
    public AuthResponse registerWithWhatsApp(WhatsAppRegisterRequest req) {
        // 1. On récupère l'OTP stocké
        OtpData savedOtpData = otpStorage.get(req.phoneNumber());

        if (savedOtpData == null || !savedOtpData.code.equals(req.otpCode())) {
            throw new IllegalStateException("Code OTP invalide");
        }

        if (savedOtpData.isExpired()) {
            otpStorage.remove(req.phoneNumber());
            throw new IllegalStateException("Le code OTP a expiré");
        }

        // 2. On vérifie si l'identifiant existe déjà
        if (userRepository.existsByEmail(req.phoneNumber())) {
            throw new IllegalStateException("Ce numéro est déjà enregistré");
        }

        // 3. Création SANS les rôles (pour éviter l'erreur de table vide)
        User user = User.builder()
                .email(req.phoneNumber()) // Stocké dans la colonne email
                .phone(req.phoneNumber()) // Stocké aussi dans la colonne phone (ton image pgAdmin montre cette colonne)
                .password(passwordEncoder.encode(req.password()))
                .name("Joueur " + req.phoneNumber())
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .roles(new HashSet<>()) // On initialise un set vide
                .build();

        // 4. Sauvegarde physique
        try {
            userRepository.save(user);
            System.out.println("✅ UTILISATEUR ENREGISTRÉ DANS PGADMIN : " + req.phoneNumber());
        } catch (Exception e) {
            System.out.println("❌ ERREUR BASE DE DONNÉES : " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'écriture en base de données");
        }

        // 5. Nettoyage et Token
        otpStorage.remove(req.phoneNumber());
        UserDetails details = userDetailsService.loadUserByUsername(req.phoneNumber());
        return new AuthResponse(jwtService.generateToken(details));
    }
    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        var userDetails = userDetailsService.loadUserByUsername(req.email());
        return new AuthResponse(jwtService.generateToken(userDetails));
    }

    // On peut garder l'ancien register si on veut laisser le choix (Email vs WhatsApp)
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalStateException("Email déjà utilisé");
        }
        // ... (reste de ta logique register par email)
        return null; // Simplifié pour l'exemple
    }
    private static class OtpData {
        String code;
        LocalDateTime expiryTime;

        OtpData(String code) {
            this.code = code;
            this.expiryTime = LocalDateTime.now().plusMinutes(5); // Définit l'expiration
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }

    /**
     * Réinitialise le mot de passe après vérification de l'OTP
     */
    public void resetPassword(WhatsAppRegisterRequest req) { // On réutilise le même DTO car il contient phone, otp et password
        // 1. Vérification de l'OTP (Même logique que l'inscription)
        OtpData savedOtpData = otpStorage.get(req.phoneNumber());

        if (savedOtpData == null || !savedOtpData.code.equals(req.otpCode())) {
            throw new IllegalStateException("Code OTP invalide");
        }

        if (savedOtpData.isExpired()) {
            otpStorage.remove(req.phoneNumber());
            throw new IllegalStateException("Le code OTP a expiré");
        }

        // 2. Recherche de l'utilisateur
        User user = userRepository.findByEmail(req.phoneNumber()) // Rappel: vous stockez le phone dans la colonne email
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));

        // 3. Mise à jour du mot de passe
        user.setPassword(passwordEncoder.encode(req.password()));
        userRepository.save(user);

        // 4. Nettoyage
        otpStorage.remove(req.phoneNumber());
        System.out.println("✅ Mot de passe réinitialisé pour : " + req.phoneNumber());
    }
}