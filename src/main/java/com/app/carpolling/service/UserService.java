package com.app.carpolling.service;

import com.app.carpolling.dto.AuthResponse;
import com.app.carpolling.dto.LoginRequest;
import com.app.carpolling.dto.UserDetailsResponse;
import com.app.carpolling.dto.UserRegistrationRequest;
import com.app.carpolling.entity.Driver;
import com.app.carpolling.entity.User;
import com.app.carpolling.entity.UserRole;
import com.app.carpolling.exception.BaseException;
import com.app.carpolling.exception.ErrorCode;
import com.app.carpolling.repository.DriverRepository;
import com.app.carpolling.repository.UserRepository;
import com.app.carpolling.utils.JWTUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DriverRepository driverRepository;
    private final JWTUtils jwtUtils;
    
    @Transactional
    public AuthResponse registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BaseException(ErrorCode.PHONE_ALREADY_REGISTERED);
        }
        
        // Check if email is provided and already exists
        if (request.getEmail() != null && !request.getEmail().isEmpty() 
            && userRepository.existsByEmail(request.getEmail())) {
            throw new BaseException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }
        
        // Validate password strength
        validatePassword(request.getPassword());

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail()); // Can be null
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setIsActive(true);
        
        User savedUser = userRepository.save(user);

        return new AuthResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail(),
            savedUser.getPhone(), null, savedUser.getRole());
    }
    
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
            .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND, "User not found with this phone number"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BaseException(ErrorCode.INVALID_CREDENTIALS);
        }
        
        if (!user.getIsActive()) {
            throw new BaseException(ErrorCode.USER_ACCOUNT_INACTIVE);
        }

        String token = jwtUtils.generateToken(user.getPhone());

        return new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(),
            token, user.getRole());
    }
    
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public UserDetailsResponse getUserByPhone(String phoneNumber) {
        Long driverId = null;
        User user = userRepository.findByPhone(phoneNumber).orElseThrow(
            () -> new BaseException(ErrorCode.USER_NOT_FOUND, "User not found with phone number: " + phoneNumber));

        if (UserRole.DRIVER.equals(user.getRole())) {
            driverId = driverRepository.findByUserId(user.getId()).map(Driver::getId).orElse(null);
        }

        return new UserDetailsResponse(user.getId(), driverId, user.getName(), user.getEmail(),
            user.getPhone(), user.getRole(), user.getIsActive(), user.getCreatedAt(),
            user.getUpdatedAt());
    }
    
    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new BaseException(ErrorCode.PASSWORD_TOO_SHORT);
        }
        
        boolean hasUpperCase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecialChar = password.chars().anyMatch(ch -> 
            "!@#$%^&*()_+-=[]{}|;':\"\\,.<>/?".indexOf(ch) >= 0
        );
        
        if (!hasUpperCase) {
            throw new BaseException(ErrorCode.PASSWORD_MISSING_UPPERCASE);
        }
        if (!hasDigit) {
            throw new BaseException(ErrorCode.PASSWORD_MISSING_NUMBER);
        }
        if (!hasSpecialChar) {
            throw new BaseException(ErrorCode.PASSWORD_MISSING_SPECIAL_CHAR);
        }
    }
}




