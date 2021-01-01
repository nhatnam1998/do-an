package epu.aeshop.service;


import java.util.List;

import epu.aeshop.entity.CartItem;

public interface CartItemService {
    public CartItem saveCartItem(CartItem cartItem);

    public List<CartItem> getCartItems();

    public CartItem getCartItemById(Long id);
}
