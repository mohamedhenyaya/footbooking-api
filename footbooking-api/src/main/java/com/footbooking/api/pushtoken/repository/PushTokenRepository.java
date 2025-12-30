package com.footbooking.api.pushtoken.repository;

import com.footbooking.api.pushtoken.model.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PushTokenRepository extends JpaRepository<PushToken, Long> {

    Optional<PushToken> findByUserIdAndToken(Long userId, String token);

    void deleteByUserIdAndToken(Long userId, String token);
}
