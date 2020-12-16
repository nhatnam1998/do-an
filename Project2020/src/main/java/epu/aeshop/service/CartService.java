package epu.aeshop.service;

import java.math.BigDecimal;
import java.util.List;

import epu.aeshop.entity.Buyer;
import epu.aeshop.entity.CartItem;

public interface CartService {
    void addCartItem(CartItem item);
    CartItem saveCartItem(Buyer buyer, CartItem item);
    void removeCartItem(Long id);
    List<CartItem> getCartByBuyerId(Long buyerId);
    BigDecimal getTotalAmount(Long buyerId);
}
