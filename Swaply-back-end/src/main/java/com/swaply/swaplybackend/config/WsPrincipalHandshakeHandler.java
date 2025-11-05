package com.swaply.swaplybackend.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
public class WsPrincipalHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        Object userId = attributes.get("userId");
        Object username = attributes.get("username");
        // Use userId string as Principal name when available to enable /user destination addressing by ID
        String name = (userId != null) ? String.valueOf(userId) : (username != null ? (String) username : null);
        if (name == null) return null;
        return new StompPrincipal(name, userId instanceof Long ? (Long) userId : (userId != null ? Long.parseLong(String.valueOf(userId)) : null));
    }

    static class StompPrincipal implements Principal {
        private final String name;
        @Nullable
        private final Long userId;
        StompPrincipal(String name, @Nullable Long userId) { this.name = name; this.userId = userId; }
        @Override public String getName() { return name; }
        @Nullable public Long getUserId() { return userId; }
    }
}

