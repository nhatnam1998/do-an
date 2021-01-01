package epu.aeshop.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import epu.aeshop.entity.Order;
import epu.aeshop.entity.OrderItem;

import java.util.List;

public interface OrderRepository extends CrudRepository<Order, Long> {

}
