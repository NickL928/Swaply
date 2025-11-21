package com.swaply.swaplybackend.controller;

import com.swaply.swaplybackend.entity.Announcement;
import com.swaply.swaplybackend.entity.Auction;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.enums.UserRole;
import com.swaply.swaplybackend.repository.*;
import com.swaply.swaplybackend.service.IUserService;
import com.swaply.swaplybackend.service.MetricsService;
import com.swaply.swaplybackend.dto.AdminApiTestRequest;
import com.swaply.swaplybackend.dto.AdminApiTestResponse;
import com.swaply.swaplybackend.dto.AdminDbQueryRequest;
import com.swaply.swaplybackend.dto.AdminDbQueryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final IUserService userService;
    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final OrderRepository orderRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final JdbcTemplate jdbcTemplate;
    private final MetricsService metricsService;
    private final RestTemplate restTemplate = new RestTemplate();

    public AdminController(IUserService userService,
                           AnnouncementRepository announcementRepository,
                           UserRepository userRepository,
                           ListingRepository listingRepository,
                           OrderRepository orderRepository,
                           AuctionRepository auctionRepository,
                           BidRepository bidRepository,
                           JdbcTemplate jdbcTemplate,
                           MetricsService metricsService) {
        this.userService = userService;
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.orderRepository = orderRepository;
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.metricsService = metricsService;
    }

    // User management
    @GetMapping("/users")
    public List<User> allUsers() { return userService.getAllUsers(); }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> changeRole(@PathVariable Long id, @RequestParam("role") String role) {
        return userService.getUserById(id).map(u -> {
            try {
                u.setRole(UserRole.valueOf(role));
                userService.updateUser(u);
                return ResponseEntity.ok().build();
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid role");
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable Long id, @RequestParam("active") boolean active) {
        return userService.getUserById(id).map(u -> {
            u.setIsActive(active);
            userService.updateUser(u);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    // Announcements
    @PostMapping("/announcements")
    public Announcement createAnnouncement(@RequestBody Announcement a) { return announcementRepository.save(a); }

    @PutMapping("/announcements/{id}")
    public ResponseEntity<Announcement> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement a) {
        return announcementRepository.findById(id).map(existing -> {
            existing.setTitle(a.getTitle());
            existing.setContent(a.getContent());
            existing.setActive(a.getActive());
            return ResponseEntity.ok(announcementRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/announcements/{id}")
    public ResponseEntity<?> deleteAnnouncement(@PathVariable Long id) {
        if (!announcementRepository.existsById(id)) return ResponseEntity.notFound().build();
        announcementRepository.deleteById(id); return ResponseEntity.ok().build();
    }

    // Stats summary
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Map<String, Object> m = new HashMap<>();
        m.put("users", userRepository.count());
        m.put("listings", listingRepository.count());
        m.put("orders", orderRepository.count());
        m.put("bids", bidRepository.count());
        m.put("auctions", auctionRepository.count());
        // simple totals
        BigDecimal totalOrderAmount = orderRepository.findAll().stream()
                .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        m.put("totalOrderAmount", totalOrderAmount);
        return m;
    }

    // Listings/Orders/Bids retrieve
    @GetMapping("/listings")
    public List<com.swaply.swaplybackend.entity.Listing> allListings() { return listingRepository.findAll(); }

    @GetMapping("/orders")
    public List<com.swaply.swaplybackend.entity.Order> allOrders() { return orderRepository.findAll(); }

    @GetMapping("/auctions")
    public List<Auction> allAuctions() { return auctionRepository.findAll(); }

    // Category list
    @GetMapping("/categories")
    public List<String> categories() {
        return java.util.Arrays.stream(com.swaply.swaplybackend.enums.Category.values())
                .map(Enum::name)
                .toList();
    }

    // Management mutations
    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestParam("status") String status) {
        return orderRepository.findById(id).map(o -> {
            try {
                o.setStatus(com.swaply.swaplybackend.enums.OrderStatus.valueOf(status));
                orderRepository.save(o);
                return ResponseEntity.ok().build();
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid status");
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/listings/{id}/status")
    public ResponseEntity<?> updateListingStatus(@PathVariable Long id, @RequestParam("status") String status) {
        return listingRepository.findById(id).map(l -> {
            try {
                l.setStatus(com.swaply.swaplybackend.enums.ListingStatus.valueOf(status));
                listingRepository.save(l);
                return ResponseEntity.ok().build();
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid status");
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/auctions/{id}")
    public ResponseEntity<?> deleteAuction(@PathVariable Long id) {
        return auctionRepository.findById(id).map(auction -> {
            bidRepository.deleteByAuction(auction);
            auctionRepository.delete(auction);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/listings/{id}")
    public ResponseEntity<?> deleteListing(@PathVariable Long id) {
        if (!listingRepository.existsById(id)) return ResponseEntity.notFound().build();
        listingRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api-test")
    public AdminApiTestResponse apiTest(@RequestBody AdminApiTestRequest request) {
        long start = System.currentTimeMillis();
        var method = org.springframework.http.HttpMethod.valueOf(
                request.getMethod() == null ? "GET" : request.getMethod().toUpperCase());
        var url = request.getUrl();
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url is required");
        }
        if (!url.startsWith("http")) {
            if (!url.startsWith("/")) {
                url = "/" + url;
            }
            url = "http://localhost:8080" + url;
        }
        var headers = new org.springframework.http.HttpHeaders();
        if (request.getHeaders() != null) {
            request.getHeaders().forEach(headers::add);
        }
        var entity = new org.springframework.http.HttpEntity<>(request.getBody(), headers);
        var responseEntity = restTemplate.exchange(url, method, entity, String.class);

        AdminApiTestResponse resp = new AdminApiTestResponse();
        resp.setStatus(responseEntity.getStatusCode().value());
        resp.setBody(responseEntity.getBody());
        java.util.Map<String, String> headerMap = new java.util.HashMap<>();
        responseEntity.getHeaders().forEach((k, v) -> headerMap.put(k, String.join(",", v)));
        resp.setHeaders(headerMap);
        resp.setDurationMs(System.currentTimeMillis() - start);
        return resp;
    }

    @PostMapping("/db-query")
    public AdminDbQueryResponse dbQuery(@RequestBody AdminDbQueryRequest request) {
        // NOTE: This endpoint requires ADMIN role (see SecurityConfig: /api/admin/**)
        String sql = request.getSql();
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("sql is required");
        }
        // VERY IMPORTANT: basic safety – restrict to SELECT only
        String normalized = sql.trim().toUpperCase();
        if (!normalized.startsWith("SELECT")) {
            throw new IllegalArgumentException("Only SELECT queries are allowed in this tool");
        }
        int maxRows = request.getMaxRows() != null ? request.getMaxRows() : 100;

        long start = System.currentTimeMillis();
        java.util.List<java.util.Map<String, Object>> rows = jdbcTemplate.query(sql, rs -> {
            java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
            int columns = rs.getMetaData().getColumnCount();
            int count = 0;
            while (rs.next() && count < maxRows) {
                java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
                for (int i = 1; i <= columns; i++) {
                    String colName = rs.getMetaData().getColumnLabel(i);
                    row.put(colName, rs.getObject(i));
                }
                list.add(row);
                count++;
            }
            return list;
        });

        AdminDbQueryResponse resp = new AdminDbQueryResponse();
        resp.setRows(rows);
        resp.setDurationMs(System.currentTimeMillis() - start);
        return resp;
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        // Get all system metrics from MetricsService
        return metricsService.getAllMetrics();
    }

    @GetMapping("/trends")
    public Map<String, List<Map<String, Object>>> trends() {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate from = today.minusDays(6);

        java.util.List<java.util.Map<String, Object>> userPoints = new java.util.ArrayList<>();
        java.util.List<java.util.Map<String, Object>> listingPoints = new java.util.ArrayList<>();

        for (int i = 0; i < 7; i++) {
            java.time.LocalDate d = from.plusDays(i);
            java.time.LocalDateTime start = d.atStartOfDay();
            java.time.LocalDateTime end = d.plusDays(1).atStartOfDay();

            long userCount = userRepository.countByCreatedAtBetween(start, end);
            long listingCount = listingRepository.countByCreatedDateBetween(start, end);

            java.util.Map<String, Object> u = new java.util.HashMap<>();
            u.put("ts", d.toString());
            u.put("count", userCount);
            userPoints.add(u);

            java.util.Map<String, Object> l = new java.util.HashMap<>();
            l.put("ts", d.toString());
            l.put("count", listingCount);
            listingPoints.add(l);
        }

        java.util.Map<String, List<Map<String, Object>>> out = new java.util.HashMap<>();
        out.put("users", userPoints);
        out.put("listings", listingPoints);
        return out;
    }

}
