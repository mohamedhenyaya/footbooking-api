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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
                            .map(user -> user.getName() != null ? user.getName() : user.getEmail())
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
}
