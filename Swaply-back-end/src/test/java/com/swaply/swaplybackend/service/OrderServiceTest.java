package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.CreateOrderDto;
import com.swaply.swaplybackend.dto.OrderDto;
import com.swaply.swaplybackend.dto.UpdateOrderDto;
import com.swaply.swaplybackend.entity.Listing;
import com.swaply.swaplybackend.entity.Order;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.enums.ListingStatus;
import com.swaply.swaplybackend.enums.OrderStatus;
import com.swaply.swaplybackend.exception.InvaldOrderException;
import com.swaply.swaplybackend.repository.CartItemRepository;
import com.swaply.swaplybackend.repository.ListingRepository;
import com.swaply.swaplybackend.repository.OrderRepository;
import com.swaply.swaplybackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock UserRepository userRepository;
    @Mock ListingRepository listingRepository;
    @Mock CartItemRepository cartItemRepository;

    @InjectMocks OrderService orderService;

    private User buyer;
    private User seller;
    private Listing listing;

    @BeforeEach
    void setup() {
        seller = new User();
        seller.setUserId(1L);
        seller.setUserName("nick");

        buyer = new User();
        buyer.setUserId(2L);
        buyer.setUserName("buyer");

        listing = new Listing();
        listing.setListingId(10L);
        listing.setTitle("Vintage Camera");
        listing.setPrice(new BigDecimal("150"));
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setUser(seller);
    }

    @Test
    void createOrder_withValidData_createsOrder() {
        CreateOrderDto dto = new CreateOrderDto();
        dto.setBuyerId(2L);
        dto.setListingId(10L);
        dto.setNotes("Please ship fast");

        Order savedOrder = new Order();
        savedOrder.setOrderId(100L);
        savedOrder.setBuyer(buyer);
        savedOrder.setListing(listing);
        savedOrder.setTotalAmount(new BigDecimal("150"));
        savedOrder.setStatus(OrderStatus.PENDING);

        when(userRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderDto result = orderService.createOrder(dto);

        assertThat(result.getOrderId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository).save(argThat(order ->
                order.getBuyer().equals(buyer) &&
                order.getListing().equals(listing) &&
                order.getStatus() == OrderStatus.PENDING));
    }

    @Test
    void createOrder_whenBuyerIsSeller_throwsException() {
        CreateOrderDto dto = new CreateOrderDto();
        dto.setBuyerId(1L);
        dto.setListingId(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(InvaldOrderException.class)
                .hasMessageContaining("Buyer cannot be the seller");
    }

    @Test
    void createOrder_whenListingInactive_throwsException() {
        listing.setStatus(ListingStatus.SOLD);
        CreateOrderDto dto = new CreateOrderDto();
        dto.setBuyerId(2L);
        dto.setListingId(10L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(InvaldOrderException.class)
                .hasMessageContaining("Listing is not active");
    }

    @Test
    void updateOrder_whenCompleted_marksListingAsSold() {
        Order order = new Order();
        order.setOrderId(50L);
        order.setStatus(OrderStatus.PENDING);
        order.setBuyer(buyer);
        order.setListing(listing);

        UpdateOrderDto updateDto = new UpdateOrderDto();
        updateDto.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        OrderDto result = orderService.updateOrder(50L, updateDto);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        verify(listingRepository).save(argThat(l -> l.getStatus() == ListingStatus.SOLD));
    }

    @Test
    void updateOrder_whenAlreadyCompleted_throwsException() {
        Order order = new Order();
        order.setOrderId(50L);
        order.setStatus(OrderStatus.COMPLETED);

        UpdateOrderDto updateDto = new UpdateOrderDto();
        updateDto.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrder(50L, updateDto))
                .isInstanceOf(InvaldOrderException.class)
                .hasMessageContaining("finalized");
    }
}

