package com.footbooking.api.storage.dto;

public record FileUploadResponse(
        String url,
        String filename,
        long size) {
}
