package com.pg.PaySim.repository;

import com.pg.PaySim.models.AuthToken;
import com.pg.PaySim.models.Users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM AuthToken a WHERE a.user = :user AND a.expiresAt > :now")
    int deleteActiveTokensForUser(@Param("user") Users user, @Param("now") LocalDateTime now);
}
