package com.footbooking.api.common.exception;

import com.footbooking.api.booking.exception.SlotAlreadyBookedException;
import com.footbooking.api.terrain.exception.TerrainNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(SlotAlreadyBookedException.class)
    public ResponseEntity<Map<String, Object>> handleSlotAlreadyBooked(SlotAlreadyBookedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 409,
                "error", "CONFLICT",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(TerrainNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTerrainNotFound(TerrainNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 404,
                "error", "NOT_FOUND",
                "message", ex.getMessage()
        ));
    }

    // Correction ici : Ajout de l'annotation @ manquante
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString()); // toString() pour cohérence

        String message = ex.getMessage();
        body.put("message", message);

        // Cas 1 : Compte déjà existant -> HTTP 409
        if ("Ce numéro est déjà enregistré".equals(message) || "Email déjà utilisé".equals(message)) {
            body.put("status", 409);
            body.put("error", "USER_ALREADY_EXISTS");
            body.put("suggestedAction", "SHOW_FORGOT_PASSWORD_BUTTON");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }

        // Cas 2 : Erreur OTP -> HTTP 400
        if ("Code OTP invalide".equals(message) || "Le code OTP a expiré".equals(message)) {
            body.put("status", 400);
            body.put("error", "INVALID_OTP");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        // Par défaut pour les autres IllegalStateException
        body.put("status", 400);
        body.put("error", "ILLEGAL_STATE");
        return ResponseEntity.badRequest().body(body);
    }
}