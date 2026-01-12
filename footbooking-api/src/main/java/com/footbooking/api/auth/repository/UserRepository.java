package com.footbooking.api.auth.repository;

import com.footbooking.api.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findAllByOrderByScoreDesc();

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE NOT EXISTS (SELECT r FROM u.roles r WHERE r.name IN ('ADMIN', 'SUPERADMIN')) ORDER BY u.score DESC")
    List<User> findAllPlayersOrderByScoreDesc();

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) FROM User u WHERE u.score > :score AND NOT EXISTS (SELECT r FROM u.roles r WHERE r.name IN ('ADMIN', 'SUPERADMIN'))")
    Long countPlayersByScoreGreaterThan(Integer score);

    boolean existsByEmail(String email);
}
