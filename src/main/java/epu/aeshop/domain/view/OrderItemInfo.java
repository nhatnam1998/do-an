package epu.aeshop.domain.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import epu.aeshop.entity.Buyer;
import epu.aeshop.entity.OrderItem;
import epu.aeshop.entity.OrderItemStatus;
import epu.aeshop.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderItemInfo {
    private Long sellerId;
    private int quantity;
    private OrderItemStatus orderStatus;
    private String productName;
    private String productPrice;
    private String firstName;
    private String lastName;
    private String buyerPhone;
    private String buyerEmail;
    private String shippingAddress;
    private String billingAddress;
}
