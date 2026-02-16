package com.app.carpolling.config;

import com.app.carpolling.repository.BookingRepository;
import com.app.carpolling.repository.UserRepository;
import com.app.carpolling.service.TokenBlacklistService;
import com.app.carpolling.utils.JWTUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);
    private static final String TOKEN_PARAM = "token";

    private final JWTUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public WebSocketConfig(JWTUtils jwtUtils, TokenBlacklistService tokenBlacklistService,
                          UserRepository userRepository, BookingRepository bookingRepository) {
        this.jwtUtils = jwtUtils;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request,
                                                      WebSocketHandler wsHandler,
                                                      Map<String, Object> attributes) {
                        return (Principal) attributes.get("principal");
                    }
                })
                .withSockJS()
                .setInterceptors(new JwtHandshakeInterceptor(jwtUtils, tokenBlacklistService));
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new TripSubscriptionInterceptor(userRepository, bookingRepository));
    }

    /**
     * Extracts JWT from handshake query param (?token=xxx) and validates.
     * Sets authenticated user as Principal in the session.
     */
    private static class JwtHandshakeInterceptor implements org.springframework.web.socket.server.HandshakeInterceptor {

        private final JWTUtils jwtUtils;
        private final TokenBlacklistService tokenBlacklistService;

        JwtHandshakeInterceptor(JWTUtils jwtUtils, TokenBlacklistService tokenBlacklistService) {
            this.jwtUtils = jwtUtils;
            this.tokenBlacklistService = tokenBlacklistService;
        }

        @Override
        public boolean beforeHandshake(org.springframework.http.server.ServerHttpRequest request,
                                      org.springframework.http.server.ServerHttpResponse response,
                                      org.springframework.web.socket.WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
            if (!(request instanceof org.springframework.http.server.ServletServerHttpRequest servletRequest)) {
                return false;
            }
            String token = servletRequest.getServletRequest().getParameter(TOKEN_PARAM);
            if (token == null || token.trim().isEmpty()) {
                logger.warn("WebSocket handshake rejected: missing token");
                return false;
            }
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                logger.warn("WebSocket handshake rejected: token is blacklisted");
                return false;
            }
            if (!jwtUtils.validateToken(token)) {
                logger.warn("WebSocket handshake rejected: invalid token");
                return false;
            }
            String phoneNumber = jwtUtils.extractPhoneNumber(token);
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                logger.warn("WebSocket handshake rejected: could not extract phone from token");
                return false;
            }
            Principal principal = () -> phoneNumber;
            attributes.put("principal", principal);
            logger.info("WebSocket handshake accepted for user: {}", phoneNumber);
            return true;
        }

        @Override
        public void afterHandshake(org.springframework.http.server.ServerHttpRequest request,
                                  org.springframework.http.server.ServerHttpResponse response,
                                  org.springframework.web.socket.WebSocketHandler wsHandler,
                                  Exception exception) {
            // No-op
        }
    }

    /**
     * Validates that customers can only subscribe to /topic/trip/{tripId} if they have a booking.
     */
    private static class TripSubscriptionInterceptor implements ChannelInterceptor {

        private final UserRepository userRepository;
        private final BookingRepository bookingRepository;

        TripSubscriptionInterceptor(UserRepository userRepository, BookingRepository bookingRepository) {
            this.userRepository = userRepository;
            this.bookingRepository = bookingRepository;
        }

        @Override
        public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
            var accessor = org.springframework.messaging.simp.stomp.StompHeaderAccessor.wrap(message);
            if (accessor.getCommand() != StompCommand.SUBSCRIBE) {
                return message;
            }
            String dest = accessor.getDestination();
            if (dest == null || !dest.startsWith("/topic/trip/")) {
                return message;
            }
            String tripIdStr = dest.replace("/topic/trip/", "").split("/")[0];
            Long tripId;
            try {
                tripId = Long.parseLong(tripIdStr);
            } catch (NumberFormatException e) {
                return message;
            }
            Principal principal = accessor.getUser();
            if (principal == null || principal.getName() == null) {
                logger.warn("Subscription to {} rejected: no principal", dest);
                return null; // Reject by returning null
            }
            var user = userRepository.findByPhone(principal.getName()).orElse(null);
            if (user == null || !bookingRepository.existsByUser_IdAndTrip_Id(user.getId(), tripId)) {
                logger.warn("Subscription to {} rejected: user {} has no booking for trip {}", dest, principal.getName(), tripId);
                return null; // Reject subscription
            }
            return message;
        }
    }
}
