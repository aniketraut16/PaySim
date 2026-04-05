package com.pg.PaySim.repository;

import com.pg.PaySim.models.Merchant;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant, String> {

    Optional<Merchant> findByEmailOrName(String email, String name);
}
