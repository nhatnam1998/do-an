package epu.aeshop.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import epu.aeshop.entity.Buyer;
import epu.aeshop.entity.Seller;
import epu.aeshop.entity.User;

import java.util.List;

@Repository
public interface SellerRepository extends CrudRepository<Seller, Long> {
    Seller findSellerByUser(User user);
}
