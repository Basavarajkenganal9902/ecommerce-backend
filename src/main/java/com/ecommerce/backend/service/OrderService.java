package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.OrderItemRequest;
import com.ecommerce.backend.dto.OrderRequest;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.exception.InsufficientStockException;
import com.ecommerce.backend.repository.OrderItemRepository;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import java.util.List;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Order createOrder(OrderRequest request) {

        Order order = new Order();

        order.setCustomerName(request.getCustomerName());
        order.setEmail(request.getEmail());
        order.setPhone(request.getPhone());
        order.setAddress(request.getAddress());

        // COD only
        order.setPaymentMethod("COD");

        // Initial order status
        order.setStatus("PENDING");

        double total = 0;

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {

            Product product = productRepository
                    .findById(itemRequest.getProductId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Product not found with id: "
                                            + itemRequest.getProductId()
                            )
                    );

            int requestedQuantity = itemRequest.getQuantity();

            if (requestedQuantity > product.getQuantity()) {

                throw new InsufficientStockException(
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }

            double price = product.getPrice();

            double subtotal =
                    price * requestedQuantity;

            total += subtotal;

            // Reduce stock
            product.setQuantity(
                    product.getQuantity() - requestedQuantity
            );

            productRepository.save(product);

            // Create OrderItem
            OrderItem orderItem = new OrderItem();

            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setPrice(price);
            orderItem.setQuantity(requestedQuantity);
            orderItem.setSubtotal(subtotal);

            orderItems.add(orderItem);
        }

        // Set total calculated by backend
        order.setTotalAmount(total);

        // Save order first
        Order savedOrder =
                orderRepository.save(order);

        // Attach order ID to each item
        for (OrderItem orderItem : orderItems) {

            orderItem.setOrderId(savedOrder.getId());

            orderItemRepository.save(orderItem);
        }

        return savedOrder;
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found with id: " + orderId
                        )
                );

        order.setStatus(status);

        return orderRepository.save(order);
    }
}