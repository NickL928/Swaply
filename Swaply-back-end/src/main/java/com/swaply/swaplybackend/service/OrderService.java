package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.CreateOrderDto;
import com.swaply.swaplybackend.dto.OrderDto;
import com.swaply.swaplybackend.dto.UpdateOrderDto;
import com.swaply.swaplybackend.entity.Listing;
import com.swaply.swaplybackend.entity.Order;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.enums.OrderStatus;
import com.swaply.swaplybackend.enums.ListingStatus;
import com.swaply.swaplybackend.exception.InvaldOrderException;
import com.swaply.swaplybackend.repository.ListingRepository;
import com.swaply.swaplybackend.repository.OrderRepository;
import com.swaply.swaplybackend.repository.UserRepository;
import com.swaply.swaplybackend.entity.CartItem;
import com.swaply.swaplybackend.repository.CartItemRepository;
import com.swaply.swaplybackend.dto.CheckoutResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService implements IOrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    public OrderDto createOrder(CreateOrderDto createOrderDto) {

        //Authenticate buyer
        User buyer = userRepository.findById(createOrderDto.getBuyerId())
                .orElseThrow(() -> new InvaldOrderException("Buyer not found"));

        //Check Listing
        Listing listing = listingRepository.findById(createOrderDto.getListingId())
                .orElseThrow(() -> new InvaldOrderException("Listing not found"));

        //Check if buyer is not the seller and listing is available
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new InvaldOrderException("Listing is not active");
        }

        if (listing.getUser().getUserId().equals(createOrderDto.getBuyerId())) {
            throw new InvaldOrderException("Buyer cannot be the seller");
        }

        //create order
        Order order = new Order();
        order.setBuyer(buyer);
        order.setListing(listing);
        order.setTotalAmount(listing.getPrice());
        order.setQuantity(1); // explicit default
        order.setNotes(createOrderDto.getNotes());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }

    @Override
    public OrderDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new InvaldOrderException("Order not found with id: " + orderId));
        return convertToDto(order);
    }

    @Override
    public List<OrderDto> getOrdersByBuyer(Long buyerId) {
        List<Order> orders = orderRepository.findByBuyerUserId(buyerId);
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getOrdersBySeller(Long sellerId) {
        List<Order> orders = orderRepository.findBySellerUserId(sellerId);
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDto updateOrder(Long orderId, UpdateOrderDto updateOrderDto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new InvaldOrderException("Order not found with id: " + orderId));

        order.setStatus(updateOrderDto.getStatus());
        order.setNotes(updateOrderDto.getNotes());
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }


    @Override
    public void deleteOrder(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new InvaldOrderException("Order not found with id: " + orderId);
        }
        orderRepository.deleteById(orderId);
    }

    public CheckoutResponse createOrdersFromCart(User buyer) {
        var cartItems = cartItemRepository.findByUser(buyer);
        if (cartItems.isEmpty()) {
            throw new InvaldOrderException("Cart is empty");
        }
        java.util.List<Long> orderIds = new java.util.ArrayList<>();
        java.math.BigDecimal grandTotal = java.math.BigDecimal.ZERO;
        int totalLineItems = 0;
        for (CartItem ci : cartItems) {
            Listing listing = ci.getListing();
            if (listing.getStatus() != ListingStatus.ACTIVE) {
                continue; // skip inactive listings
            }
            Order order = new Order();
            order.setBuyer(buyer);
            order.setListing(listing);
            order.setQuantity(ci.getQuantity());
            java.math.BigDecimal lineTotal = listing.getPrice().multiply(java.math.BigDecimal.valueOf(ci.getQuantity()));
            order.setTotalAmount(lineTotal);
            order.setNotes("Cart checkout qty=" + ci.getQuantity());
            order.setStatus(OrderStatus.PENDING);
            order.setCreatedAt(java.time.LocalDateTime.now());
            order.setUpdatedAt(java.time.LocalDateTime.now());
            Order saved = orderRepository.save(order);
            orderIds.add(saved.getOrderId());
            grandTotal = grandTotal.add(lineTotal);
            totalLineItems += ci.getQuantity();
        }
        cartItemRepository.deleteByUser(buyer);
        if (orderIds.isEmpty()) {
            throw new InvaldOrderException("No active listings in cart to checkout");
        }
        CheckoutResponse response = new CheckoutResponse();
        response.setOrderIds(orderIds);
        response.setOrdersCreated(orderIds.size());
        response.setGrandTotal(grandTotal);
        response.setItemsCount(totalLineItems);
        return response;
    }

    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setOrderId(order.getOrderId());
        dto.setBuyerId(order.getBuyer().getUserId());
        dto.setBuyerName(order.getBuyer().getUserName());
        var seller = order.getSeller();
        if (seller != null) {
            dto.setSellerId(seller.getUserId());
            dto.setSellerName(seller.getUserName());
        }
        dto.setListingId(order.getListing().getListingId());
        dto.setListingTitle(order.getListing().getTitle());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setNotes(order.getNotes());
        dto.setQuantity(order.getQuantity());
        return dto;
    }
}
