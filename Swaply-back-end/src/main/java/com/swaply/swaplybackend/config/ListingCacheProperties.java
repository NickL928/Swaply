package com.swaply.swaplybackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "listing.cache")
public class ListingCacheProperties {

    private boolean enabled = true;
    private int preloadLimit = 1000;
    private int treeOrder = 32;
    private long refreshIntervalMs = 60000;
    private int popularityDecayMinutes = 60;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPreloadLimit() {
        return preloadLimit;
    }

    public void setPreloadLimit(int preloadLimit) {
        this.preloadLimit = preloadLimit;
    }

    public int getTreeOrder() {
        return treeOrder;
    }

    public void setTreeOrder(int treeOrder) {
        this.treeOrder = treeOrder;
    }

    public long getRefreshIntervalMs() {
        return refreshIntervalMs;
    }

    public void setRefreshIntervalMs(long refreshIntervalMs) {
        this.refreshIntervalMs = refreshIntervalMs;
    }

    public int getPopularityDecayMinutes() {
        return popularityDecayMinutes;
    }

    public void setPopularityDecayMinutes(int popularityDecayMinutes) {
        this.popularityDecayMinutes = popularityDecayMinutes;
    }
}
