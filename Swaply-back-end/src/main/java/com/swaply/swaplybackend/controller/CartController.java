package com.swaply.swaplybackend.controller;

import com.swaply.swaplybackend.dto.AddCartItemRequest;
import com.swaply.swaplybackend.dto.CartItemResponse;
import com.swaply.swaplybackend.dto.CheckoutResponse;
import com.swaply.swaplybackend.service.CartService;
import com.swaply.swaplybackend.service.OrderService;
import com.swaply.swaplybackend.service.UserService;
import com.swaply.swaplybackend.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;

    public CartController(CartService cartService, OrderService orderService, UserService userService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.userService = userService;
    }

    private User currentUser(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return userService.getUserByUserName(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.getName()));
    }

    @GetMapping
    public List<CartItemResponse> list(Principal principal) {
        return cartService.list(currentUser(principal));
    }

    @PostMapping
    public CartItemResponse add(@Valid @RequestBody AddCartItemRequest request, Principal principal) {
        return cartService.addItem(currentUser(principal), request.getListingId(), request.getQuantity());
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> remove(@PathVariable Long cartItemId, Principal principal) {
        cartService.remove(currentUser(principal), cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/listing/{listingId}")
    public ResponseEntity<Void> removeByListing(@PathVariable Long listingId, Principal principal) {
        cartService.removeByListing(currentUser(principal), listingId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/clear")
    public ResponseEntity<Void> clear(Principal principal) {
        cartService.clear(currentUser(principal));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public CheckoutResponse checkout(Principal principal) {
        return orderService.createOrdersFromCart(currentUser(principal));
    }
}
