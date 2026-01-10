package com.app.carpolling.controller;

import com.app.carpolling.dto.ApiResponse;
import com.app.carpolling.dto.AuthResponse;
import com.app.carpolling.dto.LoginRequest;
import com.app.carpolling.dto.UserDetailsResponse;
import com.app.carpolling.dto.UserRegistrationRequest;
import com.app.carpolling.service.TokenBlacklistService;
import com.app.carpolling.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final TokenBlacklistService tokenBlacklistService;
    
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
        @Valid @RequestBody LoginRequest request,
        HttpServletResponse httpServletResponse
    ) {
        try {
            AuthResponse response = userService.login(request);
            
            // Create JWT cookie
            Cookie jwtCookie = new Cookie("jwt", response.getToken());
            jwtCookie.setHttpOnly(true);  // Prevent XSS attacks
            jwtCookie.setSecure(false);   // Set to true in production with HTTPS
            jwtCookie.setPath("/");       // Cookie available for all paths
            jwtCookie.setMaxAge(24 * 60 * 60);  // Cookie expires in 24 hours
            
            // Add cookie to response
            httpServletResponse.addCookie(jwtCookie);
            
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        try {
            String token = null;
            
            // Try to get token from cookie first
            Cookie[] cookies = httpServletRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
            
            // If no token in cookie, try Authorization header
            if (token == null) {
                String authHeader = httpServletRequest.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }
            
            // Add token to blacklist if found
            if (token != null && !token.trim().isEmpty()) {
                tokenBlacklistService.invalidateToken(token);
            }
            
            // Clear JWT cookie by setting it to null and maxAge to 0
            Cookie jwtCookie = new Cookie("jwt", null);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false);   // Set to true in production with HTTPS
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(0);       // Expire cookie immediately
            
            // Add expired cookie to response
            httpServletResponse.addCookie(jwtCookie);
            
            // Clear security context
            SecurityContextHolder.clearContext();
            
            return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Logout failed: " + e.getMessage()));
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









