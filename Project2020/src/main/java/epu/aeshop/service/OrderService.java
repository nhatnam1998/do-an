package epu.aeshop.service;

import java.io.File;
import java.util.List;

import epu.aeshop.domain.view.OrderInfo;
import epu.aeshop.entity.Buyer;
import epu.aeshop.entity.Order;
import epu.aeshop.entity.OrderItem;

public interface OrderService {
    Order getOrderById(Long id);
    Order saveOrder(Buyer buyer, Order order);
    Order updateOrder(Order order);
    void deleteOrder(Long id);
    OrderItem saveOrderItem(OrderItem orderItem);
    void completeOrder(Order order);
    void cancelOrder(Order order);
    File downloadReceipt(OrderInfo orderInfo) throws Exception;
    OrderItem getOrderItemById(Long itemId);
    List<OrderItem> getOrderItemsBySeller(Long sellerId);
    List<Order> getAll();
    List<OrderItem> getDeliveredOrderItemsByOrder(Long orderId);
}
