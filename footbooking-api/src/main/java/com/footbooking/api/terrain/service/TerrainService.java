package com.footbooking.api.terrain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.footbooking.api.auth.repository.UserRepository;
import com.footbooking.api.booking.repository.BookingJdbcRepository;
import com.footbooking.api.terrain.dto.TerrainDetailDTO;
import com.footbooking.api.terrain.dto.TerrainResponseDto;
import com.footbooking.api.terrain.dto.TerrainReviewDTO;
import com.footbooking.api.terrain.exception.TerrainNotFoundException;
import com.footbooking.api.terrain.model.Terrain;
import com.footbooking.api.terrain.model.TerrainImage;
import com.footbooking.api.terrain.model.TerrainReview;
import com.footbooking.api.terrain.repository.TerrainImageRepository;
import com.footbooking.api.terrain.repository.TerrainRepository;
import com.footbooking.api.terrain.repository.TerrainReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TerrainService {

        private final TerrainRepository terrainRepository;
        private final BookingJdbcRepository bookingJdbcRepository;
        private final TerrainImageRepository terrainImageRepository;
        private final TerrainReviewRepository terrainReviewRepository;
        private final UserRepository userRepository;
        private final ObjectMapper objectMapper = new ObjectMapper();

        public List<TerrainResponseDto> getAllTerrains() {
                return terrainRepository.findAll()
                                .stream()
                                .map(this::toDto)
                                .toList();
        }

        private TerrainResponseDto toDto(Terrain terrain) {
                return new TerrainResponseDto(
                                terrain.getId(),
                                terrain.getName(),
                                terrain.getCity(),
                                terrain.getPricePerHour(),
                                terrain.isIndoor());
        }

        public TerrainResponseDto getTerrainById(Long id) {
                Terrain terrain = terrainRepository.findById(id)
                                .orElseThrow(() -> new TerrainNotFoundException(id));
                return toDto(terrain);
        }

        public List<TerrainResponseDto> getAvailableTerrains(LocalDate date, int hour) {
                List<Long> occupiedIds = bookingJdbcRepository.findOccupiedTerrainIds(date, hour);

                return terrainRepository.findAll().stream()
                                .filter(terrain -> !occupiedIds.contains(terrain.getId()))
                                .map(this::toDto)
                                .toList();
        }

        public TerrainDetailDTO getTerrainDetails(Long id) {
                Terrain terrain = terrainRepository.findById(id)
                                .orElseThrow(() -> new TerrainNotFoundException(id));

                // Parse amenities JSON string to List
                List<String> amenitiesList = parseAmenities(terrain.getAmenities());

                // Get image URLs
                List<String> imageUrls = terrainImageRepository.findByTerrainIdOrderByIsPrimaryDescCreatedAtAsc(id)
                                .stream()
                                .map(TerrainImage::getImageUrl)
                                .collect(Collectors.toList());

                return new TerrainDetailDTO(
                                terrain.getId(),
                                terrain.getName(),
                                terrain.getCity(),
                                terrain.getPricePerHour(),
                                terrain.isIndoor(),
                                terrain.getDescription(),
                                amenitiesList,
                                terrain.getSurface(),
                                terrain.getCapacity(),
                                terrain.getRating(),
                                imageUrls);
        }

        public List<String> getTerrainImages(Long id) {
                if (!terrainRepository.existsById(id)) {
                        throw new TerrainNotFoundException(id);
                }

                return terrainImageRepository.findByTerrainIdOrderByIsPrimaryDescCreatedAtAsc(id)
                                .stream()
                                .map(TerrainImage::getImageUrl)
                                .collect(Collectors.toList());
        }

        public List<TerrainReviewDTO> getTerrainReviews(Long id) {
                if (!terrainRepository.existsById(id)) {
                        throw new TerrainNotFoundException(id);
                }

                List<TerrainReview> reviews = terrainReviewRepository.findByTerrainIdOrderByCreatedAtDesc(id);

                return reviews.stream()
                                .map(review -> {
                                        String userName = userRepository.findById(review.getUserId())
                                                        .map(user -> user.getName() != null ? user.getName()
                                                                        : user.getEmail())
                                                        .orElse("Utilisateur inconnu");

                                        return new TerrainReviewDTO(
                                                        review.getId(),
                                                        userName,
                                                        review.getRating(),
                                                        review.getComment(),
                                                        review.getCreatedAt());
                                })
                                .collect(Collectors.toList());
        }

        private List<String> parseAmenities(String amenitiesJson) {
                if (amenitiesJson == null || amenitiesJson.isEmpty()) {
                        return new ArrayList<>();
                }

                try {
                        return objectMapper.readValue(amenitiesJson, new TypeReference<List<String>>() {
                        });
                } catch (Exception e) {
                        return new ArrayList<>();
                }
        }

        public void createReview(Long terrainId, String email, Integer rating, String comment) {
                // Verify terrain exists
                Terrain terrain = terrainRepository.findById(terrainId)
                                .orElseThrow(() -> new TerrainNotFoundException(terrainId));

                // Get user
                var user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found: " + email));

                // Create review
                TerrainReview review = TerrainReview.builder()
                                .terrainId(terrainId)
                                .userId(user.getId())
                                .rating(rating)
                                .comment(comment)
                                .createdAt(java.time.LocalDateTime.now())
                                .build();

                terrainReviewRepository.save(review);

                // Update terrain rating (calculate average)
                List<TerrainReview> allReviews = terrainReviewRepository.findByTerrainIdOrderByCreatedAtDesc(terrainId);
                double averageRating = allReviews.stream()
                                .mapToInt(TerrainReview::getRating)
                                .average()
                                .orElse(0.0);

                terrain.setRating(java.math.BigDecimal.valueOf(averageRating));
                terrainRepository.save(terrain);
        }

        public java.util.List<TerrainResponseDto> getMyTerrains(String userEmail) {
                var user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                return terrainRepository.findByOwnerId(user.getId())
                                .stream()
                                .map(this::toDto)
                                .toList();
        }

        public TerrainResponseDto createTerrain(com.footbooking.api.terrain.dto.TerrainRequestDto request,
                        String userEmail) {
                // Only Superadmin reaches here due to SecurityConfig
                // But we might want to allow Superadmin to specify an owner?
                // For now, assign to Superadmin itself or maybe null?
                // Requirement said: Superadmin creates.
                // Let's assume Superadmin assigns it to themselves initially or we need a way
                // to assign to an ADMIN.
                // Given DTO doesn't have ownerEmail, we'll assign to creator (Superadmin).

                var user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Terrain terrain = Terrain.builder()
                                .name(request.name())
                                .city(request.city())
                                .pricePerHour(request.pricePerHour())
                                .indoor(request.indoor())
                                .description(request.description())
                                .surface(request.surface())
                                .capacity(request.capacity())
                                .createdAt(java.time.LocalDateTime.now())
                                .owner(user)
                                .build();

                if (request.amenities() != null) {
                        try {
                                terrain.setAmenities(objectMapper.writeValueAsString(request.amenities()));
                        } catch (Exception e) {
                                terrain.setAmenities("[]");
                        }
                }

                return toDto(terrainRepository.save(terrain));
        }

        public TerrainResponseDto updateTerrain(Long id, com.footbooking.api.terrain.dto.TerrainRequestDto request,
                        String userEmail) {
                Terrain terrain = terrainRepository.findById(id)
                                .orElseThrow(() -> new TerrainNotFoundException(id));

                var user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                validateOwnership(terrain, user);

                terrain.setName(request.name());
                terrain.setCity(request.city());
                terrain.setPricePerHour(request.pricePerHour());
                terrain.setIndoor(request.indoor());
                terrain.setDescription(request.description());
                terrain.setSurface(request.surface());
                terrain.setCapacity(request.capacity());

                if (request.amenities() != null) {
                        try {
                                terrain.setAmenities(objectMapper.writeValueAsString(request.amenities()));
                        } catch (Exception e) {
                                // ignore
                        }
                }

                return toDto(terrainRepository.save(terrain));
        }

        public void deleteTerrain(Long id, String userEmail) {
                // Strictly Superadmin
                Terrain terrain = terrainRepository.findById(id)
                                .orElseThrow(() -> new TerrainNotFoundException(id));

                terrainRepository.delete(terrain);
        }

        private void validateOwnership(Terrain terrain, com.footbooking.api.auth.model.User user) {
                boolean isSuperAdmin = user.getRoles().stream()
                                .anyMatch(role -> "SUPERADMIN".equals(role.getName())
                                                || "ROLE_SUPERADMIN".equals(role.getName()));

                System.out.println(
                                "DEBUG: Checking ownership for UserID=" + user.getId() + " (" + user.getEmail() + ")");
                System.out.println("DEBUG: User Roles=" + user.getRoles());
                System.out.println("DEBUG: TerrainID=" + terrain.getId() + ", Owner="
                                + (terrain.getOwner() != null
                                                ? terrain.getOwner().getId() + " (" + terrain.getOwner().getEmail()
                                                                + ")"
                                                : "NULL"));

                if (isSuperAdmin) {
                        System.out.println("DEBUG: Access granted (SuperAdmin)");
                        return;
                }

                if (terrain.getOwner() == null) {
                        throw new AccessDeniedException(
                                        "Unauthorized: Terrain has no owner and you are not Superadmin");
                }

                if (!terrain.getOwner().getId().equals(user.getId())) {
                        System.out.println("DEBUG: Access DENIED. OwnerID mismatch.");
                        throw new AccessDeniedException("Unauthorized: You do not own this terrain");
                }
        }

        public void addUserToWhitelistByIdentifier(Long terrainId, String identifier, String adminEmail) {
                Terrain terrain = terrainRepository.findById(terrainId)
                                .orElseThrow(() -> new TerrainNotFoundException(terrainId));

                var admin = userRepository.findByEmail(adminEmail)
                                .orElseThrow(() -> new RuntimeException("Admin not found"));

                validateOwnership(terrain, admin);

                // Try by email first
                var userToAdd = userRepository.findByEmail(identifier)
                                .orElse(null);

                if (userToAdd == null) {
                        // Need to implement findbyPhone if phone exists?
                        // User entity has 'email', 'name', 'password'. No phone visible in previous
                        // view_file of User.java (Wait, I should check the view).
                        // Lines 1-51 of User.java showed: id, name, email, password, roles.
                        // No phone. So I can only support Email for now.
                        // If user meant "username" as "identifier", I can check name ? But name is not
                        // unique usually.
                        // I'll stick to Email as the primary "login".
                        throw new UsernameNotFoundException("User not found with identifier: " + identifier);
                }

                terrain.getWhitelist().add(userToAdd);
                terrainRepository.save(terrain);
        }

        public void addUserToWhitelist(Long terrainId, Long userId, String adminEmail) {
                Terrain terrain = terrainRepository.findById(terrainId)
                                .orElseThrow(() -> new TerrainNotFoundException(terrainId));

                var admin = userRepository.findByEmail(adminEmail)
                                .orElseThrow(() -> new RuntimeException("Admin not found"));

                validateOwnership(terrain, admin);

                var userToAdd = userRepository.findById(userId)
                                .orElseThrow(() -> new UsernameNotFoundException("User to add not found"));

                terrain.getWhitelist().add(userToAdd);
                terrainRepository.save(terrain);
        }

        public java.util.Set<com.footbooking.api.booking.dto.UserSummaryDto> getWhitelist(Long terrainId,
                        String adminEmail) {
                Terrain terrain = terrainRepository.findById(terrainId)
                                .orElseThrow(() -> new TerrainNotFoundException(terrainId));

                var admin = userRepository.findByEmail(adminEmail)
                                .orElseThrow(() -> new RuntimeException("Admin not found"));

                validateOwnership(terrain, admin);

                return terrain.getWhitelist().stream()
                                .map(u -> new com.footbooking.api.booking.dto.UserSummaryDto(u.getName(), u.getEmail(),
                                                u.getPhone()))
                                .collect(Collectors.toSet());
        }

        public void removeUserFromWhitelist(Long terrainId, String identifier, String adminEmail) {
                if (identifier == null || "undefined".equalsIgnoreCase(identifier)) {
                        throw new RuntimeException("Invalid identifier: undefined or null");
                }

                Terrain terrain = terrainRepository.findById(terrainId)
                                .orElseThrow(() -> new TerrainNotFoundException(terrainId));

                var admin = userRepository.findByEmail(adminEmail)
                                .orElseThrow(() -> new RuntimeException("Admin not found"));

                validateOwnership(terrain, admin);

                com.footbooking.api.auth.model.User userToRemove = null;

                // 1. Try to parse as ID
                try {
                        Long userId = Long.parseLong(identifier);
                        userToRemove = terrain.getWhitelist().stream()
                                        .filter(u -> u.getId().equals(userId))
                                        .findFirst()
                                        .orElse(null);
                } catch (NumberFormatException e) {
                        // Not an ID, proceed to check as email
                }

                // 2. If not found by ID, check by Email
                if (userToRemove == null) {
                        userToRemove = terrain.getWhitelist().stream()
                                        .filter(u -> u.getEmail().equalsIgnoreCase(identifier))
                                        .findFirst()
                                        .orElseThrow(() -> new UsernameNotFoundException(
                                                        "User not found in whitelist with identifier: " + identifier));
                }

                terrain.getWhitelist().remove(userToRemove);
                terrainRepository.save(terrain);
        }
}
