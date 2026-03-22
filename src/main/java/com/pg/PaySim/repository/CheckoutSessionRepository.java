package com.pg.PaySim.repository;

import com.pg.PaySim.models.CheckoutSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CheckoutSessionRepository extends JpaRepository<CheckoutSession, UUID> {
}
