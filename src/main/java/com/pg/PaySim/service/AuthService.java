package com.pg.PaySim.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pg.PaySim.dto.RegisterMerchent;
import com.pg.PaySim.exceptions.AuthenticationFaliedException;
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

    @Autowired
    MerchantRepository merchantRepository;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JWTService jwtService;

    @Autowired
    AuthTokenRepository authTokenRepository;

    @Value("${jwt.expiration}")
    Long expirationDuration;

    private String generateMerchantId() {
        Integer random = new Random().nextInt(100000, 999999);
        return "MER-" + random.toString();
    }

    private String generateUniqueMerchantId(){
        String merchantId = generateMerchantId();
        Optional<Merchant> merchantOptional = merchantRepository.findById(merchantId);
        if (merchantOptional.isPresent()) {
            return generateUniqueMerchantId();
        }
        return merchantId;
    }



    @Transactional
    public String register(RegisterMerchent registerMerchent) {
        Optional<Merchant> mercheOptional = merchantRepository.findByEmailOrName(registerMerchent.getMerchentEmail(), registerMerchent.getMerchentName());
        if (mercheOptional.isPresent()) {
            throw new DuplicateResourceException("Merchant with this email or name already exists");
        }

        Optional<Users> userOptional = usersRepository.findByEmail(registerMerchent.getUserEmail());
        if (userOptional.isPresent()) {
            throw new DuplicateResourceException("User with this email already exists");
        }

        Merchant merchant = new Merchant();
        merchant.setId(generateUniqueMerchantId());
        merchant.setName(registerMerchent.getMerchentName());
        merchant.setEmail(registerMerchent.getMerchentEmail());
        Merchant savedMerchant = merchantRepository.save(merchant);

        Users user = new Users();
        user.setMerchant(savedMerchant);
        user.setRole(ROLES.ADMIN);
        user.setEmail(registerMerchent.getUserEmail());
        user.setName(registerMerchent.getUserName());
        user.setPasswordHash(passwordEncoder.encode(registerMerchent.getUserPassword()));
        Users savedUser = usersRepository.save(user);

        String token = jwtService.generateToken(savedUser.getEmail(), Map.of("role", savedUser.getRole().name()));
        AuthToken authToken = new AuthToken();
        authToken.setUser(savedUser);
        authToken.setToken(token);
        authToken.setExpiresAt(LocalDateTime.now().plusSeconds(expirationDuration/1000));
        AuthToken savedAuthToken = authTokenRepository.save(authToken);

        return savedAuthToken.getToken();
    };

    public String login(String email, String password) {
        Optional<Users> userOptional = usersRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new AuthenticationFaliedException("Invalid email or password");
        }
        Users user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthenticationFaliedException("Invalid email or password");
        }
        
        List<AuthToken> authTokens = authTokenRepository.findByUserAndExpiresAtAfter(user, LocalDateTime.now());
        
        for (AuthToken authToken : authTokens) {
            authToken.setExpiresAt(LocalDateTime.now().minusSeconds(1));
            authTokenRepository.save(authToken);
        }
        
        
        String token = jwtService.generateToken(user.getEmail(), Map.of("role", user.getRole().name()));
        AuthToken authToken = new AuthToken();
        authToken.setUser(user);
        authToken.setToken(token);
        authToken.setExpiresAt(LocalDateTime.now().plusSeconds(expirationDuration/1000));
        AuthToken savedAuthToken = authTokenRepository.save(authToken);
        return savedAuthToken.getToken();
    };


    
}
