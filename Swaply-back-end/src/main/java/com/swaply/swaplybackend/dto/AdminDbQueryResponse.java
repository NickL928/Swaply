package com.swaply.swaplybackend.dto;

import java.util.List;
import java.util.Map;

public class AdminDbQueryResponse {
    private List<Map<String, Object>> rows;
    private long durationMs;

    public List<Map<String, Object>> getRows() { return rows; }
    public void setRows(List<Map<String, Object>> rows) { this.rows = rows; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
}


