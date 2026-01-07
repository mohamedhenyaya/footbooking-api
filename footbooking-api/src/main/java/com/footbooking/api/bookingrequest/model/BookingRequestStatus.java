package com.footbooking.api.bookingrequest.model;

public enum BookingRequestStatus {
    EN_ATTENTE_PAIEMENT, // Waiting for payment proof upload (5 min)
    EN_ATTENTE_VALIDATION_ADMIN, // Payment submitted, waiting for admin approval
    APPROUVEE, // Approved by admin, booking created
    REJETEE, // Rejected by admin
    EXPIREE, // Deadline passed without payment submission
    ANNULEE // Cancelled by user
}
