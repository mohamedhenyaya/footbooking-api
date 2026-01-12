package com.footbooking.api.bookingrequest.repository;

import com.footbooking.api.bookingrequest.model.BookingRequest;
import com.footbooking.api.bookingrequest.model.BookingRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {

        List<BookingRequest> findByUserId(Long userId);

        List<BookingRequest> findByStatus(BookingRequestStatus status);

        List<BookingRequest> findByStatusAndDeadlineBefore(BookingRequestStatus status, LocalDateTime deadline);

        @Query("SELECT br FROM BookingRequest br WHERE br.terrain.id = :terrainId AND br.status = :status")
        List<BookingRequest> findByTerrainIdAndStatus(@Param("terrainId") Long terrainId,
                        @Param("status") BookingRequestStatus status);

        @Query("SELECT br FROM BookingRequest br " +
                        "WHERE br.status = 'EN_ATTENTE_VALIDATION_ADMIN' " +
                        "AND (:terrainId IS NULL OR br.terrain.id = :terrainId)")
        List<BookingRequest> findPendingRequests(@Param("terrainId") Long terrainId);

        Optional<BookingRequest> findByIdAndUserId(Long id, Long userId);

        void deleteByUserIdIn(List<Long> userIds);
}
