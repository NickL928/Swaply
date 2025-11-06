package com.swaply.swaplybackend.controller;

import com.swaply.swaplybackend.dto.OrderDto;
import com.swaply.swaplybackend.dto.UpdateOrderDto;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.enums.OrderStatus;
import com.swaply.swaplybackend.repository.OrderRepository;
import com.swaply.swaplybackend.service.IOrderService;
import com.swaply.swaplybackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final IOrderService orderService;
    private final OrderRepository orderRepository;
    private final UserService userService;

    public OrderController(IOrderService orderService, OrderRepository orderRepository, UserService userService) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.userService = userService;
    }

    private User currentUser(Principal principal) {
        return userService.getUserByUserName(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.getName()));
    }

    @GetMapping("/buyer")
    public List<OrderDto> myOrdersAsBuyer(Principal principal) {
        User me = currentUser(principal);
        return orderService.getOrdersByBuyer(me.getUserId());
    }

    @GetMapping("/seller")
    public List<OrderDto> myOrdersAsSeller(Principal principal) {
        User me = currentUser(principal);
        return orderService.getOrdersBySeller(me.getUserId());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> get(@PathVariable Long orderId, Principal principal) {
        User me = currentUser(principal);
        return orderRepository.findById(orderId)
                .map(o -> {
                    Long buyerId = o.getBuyer() != null ? o.getBuyer().getUserId() : null;
                    Long sellerId = o.getSeller() != null ? o.getSeller().getUserId() : null;
                    if (!me.getUserId().equals(buyerId) && !me.getUserId().equals(sellerId)) {
                        String msg = "Forbidden: meId=" + me.getUserId() + ", buyerId=" + buyerId + ", sellerId=" + sellerId + ", orderId=" + orderId;
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(msg);
                    }
                    return ResponseEntity.ok(orderService.getOrderById(orderId));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<?> update(@PathVariable Long orderId,
                                           @Valid @RequestBody UpdateOrderDto update,
                                           Principal principal) {
        User me = currentUser(principal);
        return orderRepository.findById(orderId)
                .map(o -> {
                    Long sellerId = o.getSeller() != null ? o.getSeller().getUserId() : null;
                    if (sellerId == null || !me.getUserId().equals(sellerId)) {
                        String msg = "Forbidden: meId=" + me.getUserId() + ", sellerId=" + sellerId + ", orderId=" + orderId;
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(msg);
                    }
                    return ResponseEntity.ok(orderService.updateOrder(orderId, update));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<?> updatePut(@PathVariable Long orderId,
                                              @Valid @RequestBody UpdateOrderDto update,
                                              Principal principal) {
        return update(orderId, update, principal);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> delete(@PathVariable Long orderId, Principal principal) {
        User me = currentUser(principal);
        return orderRepository.findById(orderId)
                .map(o -> {
                    Long buyerId = o.getBuyer() != null ? o.getBuyer().getUserId() : null;
                    Long sellerId = o.getSeller() != null ? o.getSeller().getUserId() : null;
                    if (!me.getUserId().equals(buyerId) && !me.getUserId().equals(sellerId)) {
                        String msg = "Forbidden: meId=" + me.getUserId() + ", buyerId=" + buyerId + ", sellerId=" + sellerId + ", orderId=" + orderId;
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(msg);
                    }
                    if (o.getStatus() != OrderStatus.COMPLETED && o.getStatus() != OrderStatus.CANCELLED) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only completed or cancelled orders can be removed");
                    }
                    orderService.deleteOrder(orderId);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
