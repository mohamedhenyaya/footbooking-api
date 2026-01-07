package com.footbooking.api.common.exception;

import com.footbooking.api.terrain.exception.TerrainNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
@lombok.extern.slf4j.Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(TerrainNotFoundException.class)
        public ResponseEntity<?> handleTerrainNotFound(TerrainNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                Map.of(
                                                "timestamp", Instant.now(),
                                                "status", 404,
                                                "error", "NOT_FOUND",
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(UsernameNotFoundException.class)
        public ResponseEntity<?> handleUsernameNotFound(UsernameNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                Map.of(
                                                "timestamp", Instant.now(),
                                                "status", 404,
                                                "error", "USER_NOT_FOUND",
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                Map.of(
                                                "timestamp", Instant.now(),
                                                "status", 403,
                                                "error", "FORBIDDEN",
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(com.footbooking.api.payment.exception.BankAccountNotFoundException.class)
        public ResponseEntity<?> handleBankAccountNotFound(
                        com.footbooking.api.payment.exception.BankAccountNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                Map.of(
                                                "timestamp", Instant.now(),
                                                "status", 404,
                                                "error", "BANK_ACCOUNT_NOT_FOUND",
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(com.footbooking.api.payment.exception.PaymentProofNotFoundException.class)
        public ResponseEntity<?> handlePaymentProofNotFound(
                        com.footbooking.api.payment.exception.PaymentProofNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                Map.of(
                                                "timestamp", Instant.now(),
                                                "status", 404,
                                                "error", "PAYMENT_PROOF_NOT_FOUND",
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(com.footbooking.api.storage.exception.FileStorageException.class)
        public ResponseEntity<?> handleFileStorageException(
                        com.footbooking.api.storage.exception.FileStorageException ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                Map.of(
                                                "timestamp", Instant.now(),
                                                "status", 500,
                                                "error", "FILE_STORAGE_ERROR",
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(com.footbooking.api.storage.exception.InvalidFileException.class)
        public ResponseEntity<?> handleInvalidFileException(
                        com.footbooking.api.storage.exception.InvalidFileException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                Map.of(
                                                "timestamp", Instant.now(),
                                                "status", 400,
                                                "error", "INVALID_FILE",
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(org.springframework.web.multipart.support.MissingServletRequestPartException.class)
        public ResponseEntity<?> handleMissingServletRequestPart(
                        org.springframework.web.multipart.support.MissingServletRequestPartException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                Map.of(
                                                "timestamp", Instant.now(),
                                                "status", 400,
                                                "error", "MISSING_FILE",
                                                "message",
                                                "Le paramètre 'file' est requis. Assurez-vous d'envoyer le fichier en multipart/form-data"));
        }

        @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
        public ResponseEntity<?> handleMaxUploadSizeExceeded(
                        org.springframework.web.multipart.MaxUploadSizeExceededException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                Map.of(
                                                "timestamp", Instant.now(),
                                                "status", 400,
                                                "error", "FILE_TOO_LARGE",
                                                "message", "Le fichier dépasse la taille maximale autorisée de 5MB"));
        }

        @ExceptionHandler(com.footbooking.api.bookingrequest.exception.BookingRequestNotFoundException.class)
        public ResponseEntity<?> handleBookingRequestNotFound(
                        com.footbooking.api.bookingrequest.exception.BookingRequestNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                Map.of(
                                                "timestamp", Instant.now(),
                                                "status", 404,
                                                "error", "BOOKING_REQUEST_NOT_FOUND",
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(com.footbooking.api.bookingrequest.exception.BookingRequestExpiredException.class)
        public ResponseEntity<?> handleBookingRequestExpired(
                        com.footbooking.api.bookingrequest.exception.BookingRequestExpiredException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                Map.of(
                                                "timestamp", Instant.now(),
                                                "status", 400,
                                                "error", "BOOKING_REQUEST_EXPIRED",
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<?> handleIllegalStateException(IllegalStateException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                Map.of(
                                                "timestamp", Instant.now(),
                                                "status", 400,
                                                "error", "ILLEGAL_STATE",
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                Map.of(
                                                "timestamp", Instant.now(),
                                                "status", 400,
                                                "error", "BAD_REQUEST",
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
        public ResponseEntity<?> handleMethodArgumentTypeMismatch(
                        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                Map.of(
                                                "timestamp", Instant.now(),
                                                "status", 400,
                                                "error", "BAD_REQUEST",
                                                "message",
                                                String.format("The parameter '%s' of value '%s' could not be converted to type '%s'",
                                                                ex.getName(), ex.getValue(),
                                                                ex.getRequiredType().getSimpleName())));
        }

        @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
        public ResponseEntity<?> handleDataIntegrityViolation(
                        org.springframework.dao.DataIntegrityViolationException ex) {
                log.error("Data integrity violation: ", ex);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                                Map.of(
                                                "timestamp", Instant.now(),
                                                "status", 409,
                                                "error", "CONFLICT",
                                                "message",
                                                "Database error: " + ex.getMostSpecificCause().getMessage()));
        }

        @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
        public ResponseEntity<?> handleHttpMessageNotReadable(
                        org.springframework.http.converter.HttpMessageNotReadableException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                Map.of(
                                                "timestamp", Instant.now(),
                                                "status", 400,
                                                "error", "BAD_REQUEST",
                                                "message", "Malformed JSON request"));
        }

        @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<?> handleHttpRequestMethodNotSupported(
                        org.springframework.web.HttpRequestMethodNotSupportedException ex) {
                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                                Map.of(
                                                "timestamp", Instant.now(),
                                                "status", 405,
                                                "error", "METHOD_NOT_ALLOWED",
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<?> handleGenericException(Exception ex) {
                log.error("Unexpected error: ", ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                Map.of(
                                                "timestamp", Instant.now(),
                                                "status", 500,
                                                "error", "INTERNAL_SERVER_ERROR",
                                                "message", ex.getMessage() != null ? ex.getMessage()
                                                                : "An unexpected error occurred"));
        }
}
