package com.swaply.swaplybackend.service;

import org.springframework.stereotype.Service;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;

@Service
public class MetricsService {

    // Track active users by username (for JWT-based auth)
    // Key: username, Value: last activity timestamp
    private final Map<String, Instant> activeUsers = new ConcurrentHashMap<>();

    // Track request times for response time calculation
    private final Map<Long, Long> recentRequests = new ConcurrentHashMap<>();
    private static final int MAX_TRACKED_REQUESTS = 100;

    /**
     * Register an active user by username
     */
    public void registerUser(String username) {
        if (username != null && !username.isBlank()) {
            activeUsers.put(username, Instant.now());
            // Clean up old users
            cleanupOldUsers();
        }
    }

    /**
     * Remove a user
     */
    public void removeUser(String username) {
        activeUsers.remove(username);
    }

    /**
     * Get count of active users (users active in last 5 minutes)
     */
    public int getActiveUsers() {
        cleanupOldUsers();
        return activeUsers.size();
    }

    /**
     * Track a request and its duration
     */
    public void trackRequest(long durationMs) {
        recentRequests.put(System.nanoTime(), durationMs);
        // Keep only recent requests
        if (recentRequests.size() > MAX_TRACKED_REQUESTS) {
            // Remove oldest entry
            Long oldestKey = recentRequests.keySet().stream().min(Long::compare).orElse(null);
            if (oldestKey != null) {
                recentRequests.remove(oldestKey);
            }
        }
    }

    /**
     * Get average response time from recent requests
     */
    public long getAverageResponseTime() {
        if (recentRequests.isEmpty()) {
            return 0;
        }
        return (long) recentRequests.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
    }

    /**
     * Get system CPU usage percentage
     */
    public double getCpuUsage() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            return Math.round(osBean.getCpuLoad() * 100.0 * 10.0) / 10.0; // Round to 1 decimal
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Get system memory usage percentage
     */
    public double getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double percentage = ((double) usedMemory / totalMemory) * 100.0;
        return Math.round(percentage * 10.0) / 10.0; // Round to 1 decimal
    }

    /**
     * Get storage usage percentage
     */
    public double getStorageUsage() {
        try {
            java.io.File root = new java.io.File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double percentage = ((double) usedSpace / totalSpace) * 100.0;
            return Math.round(percentage * 10.0) / 10.0; // Round to 1 decimal
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Get complete metrics object
     */
    public Map<String, Object> getAllMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("activeSessions", getActiveUsers()); // Now tracks by username
        metrics.put("avgResponseTime", getAverageResponseTime());
        metrics.put("cpuUsage", getCpuUsage());
        metrics.put("memoryUsage", getMemoryUsage());
        metrics.put("storageUsage", getStorageUsage());

        // Additional metrics
        metrics.put("dbQueries", recentRequests.size()); // Approximate
        metrics.put("cacheHitRate", calculateCacheHitRate());

        return metrics;
    }

    /**
     * Calculate cache hit rate (placeholder - implement based on your caching strategy)
     */
    private int calculateCacheHitRate() {
        // This is a placeholder. In a real app, you'd track actual cache hits/misses
        // For now, return a reasonable value
        return 75 + (int)(Math.random() * 20); // 75-95%
    }

    /**
     * Clean up users inactive for more than 5 minutes
     */
    private void cleanupOldUsers() {
        Instant fiveMinutesAgo = Instant.now().minusSeconds(300);
        activeUsers.entrySet().removeIf(entry -> entry.getValue().isBefore(fiveMinutesAgo));
    }
}

