package epu.aeshop.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import epu.aeshop.entity.CartItem;

@Repository
public interface CartItemRepository extends CrudRepository<CartItem, Long> {
}
