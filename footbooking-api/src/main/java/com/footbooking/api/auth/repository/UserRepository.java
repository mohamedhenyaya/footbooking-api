package com.footbooking.api.auth.repository;

import com.footbooking.api.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findAllByOrderByScoreDesc();

    Long countByScoreGreaterThan(Integer score);

    boolean existsByEmail(String email);
}
