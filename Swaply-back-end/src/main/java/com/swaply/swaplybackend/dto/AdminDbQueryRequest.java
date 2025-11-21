package com.swaply.swaplybackend.dto;

import java.util.Map;

public class AdminDbQueryRequest {
    private String sql;
    private Map<String, Object> params;
    private Integer maxRows;

    public String getSql() { return sql; }
    public void setSql(String sql) { this.sql = sql; }

    public Map<String, Object> getParams() { return params; }
    public void setParams(Map<String, Object> params) { this.params = params; }

    public Integer getMaxRows() { return maxRows; }
    public void setMaxRows(Integer maxRows) { this.maxRows = maxRows; }
}

