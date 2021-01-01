package epu.aeshop.service;

import java.util.List;

import epu.aeshop.entity.OrderItem;

public interface OrderItemService {
    OrderItem saveOrderItem(OrderItem orderItem);

    List<OrderItem> getOrderItems();

    List<OrderItem> getOrderItemsWithNotNullReviews();

    OrderItem getOrderItemById(Long id);

    List<String> getApprovedReviews(Long itemId);

    void deleteOrderItem(Long id);
}
