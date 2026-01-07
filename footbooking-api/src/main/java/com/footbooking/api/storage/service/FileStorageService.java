package com.footbooking.api.storage.service;

import com.footbooking.api.storage.exception.FileStorageException;
import com.footbooking.api.storage.exception.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.max-size:5242880}") // 5MB default
    private long maxFileSize;

    private Path fileStorageLocation;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    @PostConstruct
    public void init() {
        try {
            this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage location initialized at: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create upload directory", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Validate file is not empty
        if (file.isEmpty()) {
            throw new InvalidFileException("Cannot upload empty file");
        }

        // Validate file size
        if (file.getSize() > maxFileSize) {
            throw new InvalidFileException(
                    String.format("File size exceeds maximum allowed size of %d bytes", maxFileSize));
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileException(
                    "Invalid file type. Only images (JPG, PNG, GIF, WEBP) are allowed");
        }

        // Get original filename and validate extension
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);

        // If no extension found in filename, derive from content-type
        if (fileExtension.isEmpty()) {
            fileExtension = getExtensionFromContentType(contentType);
            log.info("No extension in filename, using content-type: {} -> {}", contentType, fileExtension);
        }

        if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new InvalidFileException(
                    "Invalid file extension. Only JPG, PNG, GIF, WEBP are allowed");
        }

        // Generate unique filename
        String newFilename = UUID.randomUUID().toString() + "." + fileExtension;

        try {
            // Check for invalid characters
            if (newFilename.contains("..")) {
                throw new InvalidFileException("Filename contains invalid path sequence");
            }

            // Copy file to target location
            Path targetLocation = this.fileStorageLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored successfully: {}", newFilename);
            return newFilename;

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + newFilename, ex);
        }
    }

    public String getFileUrl(String filename) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(filename)
                .toUriString();
    }

    public Path getFilePath(String filename) {
        return this.fileStorageLocation.resolve(filename).normalize();
    }

    public boolean deleteFile(String filename) {
        try {
            Path filePath = getFilePath(filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.error("Could not delete file: {}", filename, ex);
            return false;
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    private String getExtensionFromContentType(String contentType) {
        if (contentType == null) {
            return "jpg"; // default
        }

        return switch (contentType.toLowerCase()) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}
