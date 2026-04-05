package com.pg.PaySim.repository;

import com.pg.PaySim.models.AuthToken;
import com.pg.PaySim.models.Users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {
    List<AuthToken> findByUserAndExpiresAtAfter(Users user, LocalDateTime expiresAt);
}
