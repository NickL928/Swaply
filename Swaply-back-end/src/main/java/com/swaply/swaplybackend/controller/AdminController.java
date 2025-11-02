package com.swaply.swaplybackend.controller;

import com.swaply.swaplybackend.entity.Announcement;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.enums.UserRole;
import com.swaply.swaplybackend.repository.*;
import com.swaply.swaplybackend.service.IUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private final BidRepository bidRepository;

    public AdminController(IUserService userService,
                           AnnouncementRepository announcementRepository,
                           UserRepository userRepository,
                           ListingRepository listingRepository,
                           OrderRepository orderRepository,
                           BidRepository bidRepository) {
        this.userService = userService;
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.orderRepository = orderRepository;
        this.bidRepository = bidRepository;
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

    @GetMapping("/bids")
    public List<com.swaply.swaplybackend.entity.Bid> allBids() { return bidRepository.findAll(); }

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

    @DeleteMapping("/bids/{id}")
    public ResponseEntity<?> deleteBid(@PathVariable Long id) {
        if (!bidRepository.existsById(id)) return ResponseEntity.notFound().build();
        bidRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/listings/{id}")
    public ResponseEntity<?> deleteListing(@PathVariable Long id) {
        if (!listingRepository.existsById(id)) return ResponseEntity.notFound().build();
        listingRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
