package com.pg.PaySim.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pg.PaySim.dto.LoginRequest;
import com.pg.PaySim.dto.RegisterMerchant;
import com.pg.PaySim.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterMerchant registerMerchant) {
        return ResponseEntity.ok(authService.register(registerMerchant));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest.getEmail(), loginRequest.getPassword()));
    }
}
