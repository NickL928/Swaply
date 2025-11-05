package com.swaply.swaplybackend.config;

import com.swaply.swaplybackend.Util.JwtUtil;
import com.swaply.swaplybackend.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);

    private final JwtUtil jwtUtil;
    private final IUserService userService;

    public WebSocketAuthChannelInterceptor(JwtUtil jwtUtil, IUserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (SimpMessageType.CONNECT.equals(accessor.getMessageType()) || StompCommand.CONNECT.equals(accessor.getCommand())) {
            String auth = accessor.getFirstNativeHeader("Authorization");
            if (auth == null) auth = accessor.getFirstNativeHeader("authorization");
            if ((auth == null || !auth.startsWith("Bearer ")) && accessor.getFirstNativeHeader("token") != null) {
                auth = "Bearer " + accessor.getFirstNativeHeader("token");
            }
            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7);
                try {
                    String username = jwtUtil.extractUsername(token);
                    var userOpt = userService.getUserByUserName(username);
                    if (userOpt.isPresent()) {
                        var user = userOpt.get();
                        final String principalName = String.valueOf(user.getUserId());
                        accessor.setUser(new Principal() { @Override public String getName() { return principalName; } });
                        // log.debug("STOMP CONNECT authenticated userId={}", principalName);
                    } else {
                        // log.warn("STOMP CONNECT username not found: {}", username);
                    }
                } catch (Exception e) {
                    // log.warn("STOMP CONNECT token parse failed: {}", e.getMessage());
                }
            } else {
                // log.warn("STOMP CONNECT missing Authorization/token header");
            }
        }
        return message;
    }
}
