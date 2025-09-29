package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.CreateOrderDto;
import com.swaply.swaplybackend.dto.OrderDto;
import com.swaply.swaplybackend.dto.UpdateOrderDto;

import java.util.List;

public interface IOrderService {

    OrderDto createOrder(CreateOrderDto createOrderDto);

    OrderDto getOrderById(Long orderId);

    List<OrderDto> getOrdersByBuyer(Long buyerId);

    List<OrderDto> getOrdersBySeller(Long sellerId);

    OrderDto updateOrder(Long orderId, UpdateOrderDto updateOrderDto);

    void deleteOrder(Long orderId);
}
