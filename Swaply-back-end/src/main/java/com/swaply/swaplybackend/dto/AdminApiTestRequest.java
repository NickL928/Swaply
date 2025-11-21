package com.swaply.swaplybackend.dto;

import java.util.Map;

public class AdminApiTestRequest {
    private String method; // GET, POST, etc
    private String url;    // relative URL like /api/orders
    private Map<String, String> headers;
    private String body;   // raw JSON/body text

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}