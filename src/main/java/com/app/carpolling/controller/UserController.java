package com.app.carpolling.controller;

import com.app.carpolling.dto.ApiResponse;
import com.app.carpolling.dto.AuthResponse;
import com.app.carpolling.dto.LoginRequest;
import com.app.carpolling.dto.UserDetailsResponse;
import com.app.carpolling.dto.UserRegistrationRequest;
import com.app.carpolling.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerUser(
        @Valid @RequestBody UserRegistrationRequest request
    ) {
        try {
            AuthResponse response = userService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
        @Valid @RequestBody LoginRequest request
    ) {
        try {
            AuthResponse response = userService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/getUserDetails")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> getCurrentUser() {
        try {
            // Extract authenticated user's phone number from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Check if user is authenticated
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not authenticated. Please provide a valid JWT token in Authorization header."));
            }
            
            // Get phone number from principal (set by JWTFilter)
            String phoneNumber = (String) authentication.getPrincipal();
            
            // Validate phone number
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid authentication. Phone number not found in token."));
            }
            
            // Fetch user details using phone number
            UserDetailsResponse userDetails = userService.getUserByPhone(phoneNumber);
            return ResponseEntity.ok(ApiResponse.success("User details fetched successfully", userDetails));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An error occurred while fetching user details: " + e.getMessage()));
        }
    }
}









