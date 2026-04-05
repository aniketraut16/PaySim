package com.pg.PaySim.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pg.PaySim.dto.RegisterMerchant;
import com.pg.PaySim.exceptions.AuthenticationFailedException;
import com.pg.PaySim.exceptions.DuplicateResourceException;
import com.pg.PaySim.models.AuthToken;
import com.pg.PaySim.models.Merchant;
import com.pg.PaySim.models.Users;
import com.pg.PaySim.models.enums.ROLES;
import com.pg.PaySim.repository.AuthTokenRepository;
import com.pg.PaySim.repository.MerchantRepository;
import com.pg.PaySim.repository.UsersRepository;

import jakarta.transaction.Transactional;

@Service
public class AuthService {

    private static final int MAX_MERCHANT_ID_ATTEMPTS = 10;

    private final MerchantRepository merchantRepository;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthTokenRepository authTokenRepository;
    private final SecureRandom merchantIdRandom = new SecureRandom();

    public AuthService(
            MerchantRepository merchantRepository,
            UsersRepository usersRepository,
            PasswordEncoder passwordEncoder,
            JWTService jwtService,
            AuthTokenRepository authTokenRepository) {
        this.merchantRepository = merchantRepository;
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authTokenRepository = authTokenRepository;
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateMerchantId() {
        int n = merchantIdRandom.nextInt(100_000, 1_000_000);
        return "MER-" + n;
    }

    private String generateUniqueMerchantId() {
        for (int i = 0; i < MAX_MERCHANT_ID_ATTEMPTS; i++) {
            String merchantId = generateMerchantId();
            if (merchantRepository.findById(merchantId).isEmpty()) {
                return merchantId;
            }
        }
        return "MER-" + UUID.randomUUID().toString().replace("-", "");
    }

    @Transactional
    public String register(RegisterMerchant registerMerchant) {
        String merchantEmail = normalizeEmail(registerMerchant.getMerchantEmail());
        String userEmail = normalizeEmail(registerMerchant.getUserEmail());

        Optional<Merchant> merchantOptional =
                merchantRepository.findByEmailOrName(merchantEmail, registerMerchant.getMerchantName());
        if (merchantOptional.isPresent()) {
            throw new DuplicateResourceException("Merchant with this email or name already exists");
        }

        Optional<Users> userOptional = usersRepository.findByEmail(userEmail);
        if (userOptional.isPresent()) {
            throw new DuplicateResourceException("User with this email already exists");
        }

        Merchant merchant = new Merchant();
        merchant.setId(generateUniqueMerchantId());
        merchant.setName(registerMerchant.getMerchantName());
        merchant.setEmail(merchantEmail);
        Merchant savedMerchant = merchantRepository.save(merchant);

        Users user = new Users();
        user.setMerchant(savedMerchant);
        user.setRole(ROLES.ADMIN);
        user.setEmail(userEmail);
        user.setName(registerMerchant.getUserName());
        user.setPasswordHash(passwordEncoder.encode(registerMerchant.getUserPassword()));
        Users savedUser = usersRepository.save(user);

        String token = jwtService.generateToken(savedUser.getEmail(), Map.of("role", savedUser.getRole().name()));
        AuthToken authToken = new AuthToken();
        authToken.setUser(savedUser);
        authToken.setToken(token);
        authToken.setExpiresAt(jwtService.computeTokenExpiry());
        AuthToken savedAuthToken = authTokenRepository.save(authToken);

        return savedAuthToken.getToken();
    }

    @Transactional
    public String login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        Optional<Users> userOptional = usersRepository.findByEmail(normalizedEmail);
        if (userOptional.isEmpty()) {
            throw new AuthenticationFailedException("Invalid email or password");
        }
        Users user = userOptional.get();
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new AuthenticationFailedException("Invalid email or password");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid email or password");
        }

        authTokenRepository.deleteActiveTokensForUser(user, LocalDateTime.now());

        String token = jwtService.generateToken(user.getEmail(), Map.of("role", user.getRole().name()));
        AuthToken authToken = new AuthToken();
        authToken.setUser(user);
        authToken.setToken(token);
        authToken.setExpiresAt(jwtService.computeTokenExpiry());
        AuthToken savedAuthToken = authTokenRepository.save(authToken);
        return savedAuthToken.getToken();
    }
}
