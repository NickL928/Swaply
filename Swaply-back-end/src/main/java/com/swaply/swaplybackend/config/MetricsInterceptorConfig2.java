package com.swaply.swaplybackend.config;

import com.swaply.swaplybackend.service.MetricsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MetricsInterceptorConfig2 implements WebMvcConfigurer {

    private final MetricsService metricsService;

    public MetricsInterceptorConfig2(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new MetricsInterceptor(metricsService));
    }

    private static class MetricsInterceptor implements HandlerInterceptor {

        private final MetricsService metricsService;
        private static final String REQUEST_START_TIME = "requestStartTime";

        public MetricsInterceptor(MetricsService metricsService) {
            this.metricsService = metricsService;
        }

        @Override
        public boolean preHandle(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler) {
            // Track request start time
            request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());

            // Track active user by username from JWT authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String username = auth.getName();
                if (username != null && !username.isBlank()) {
                    metricsService.registerUser(username);
                }
            }

            return true;
        }

        @Override
        public void afterCompletion(@NonNull HttpServletRequest request,
                                   @NonNull HttpServletResponse response,
                                   @NonNull Object handler,
                                   Exception ex) {
            // Calculate request duration
            Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                metricsService.trackRequest(duration);
            }
        }
    }
}

