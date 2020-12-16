package epu.aeshop.service;

import java.util.List;

import epu.aeshop.entity.Buyer;
import epu.aeshop.entity.Seller;
import epu.aeshop.entity.User;

public interface SellerService {
    Seller getSellerById(Long id);
    Seller save(Seller seller);
    Seller getSellerByUser(User user);
    List<Buyer> getFollowers(Long sellerId);
    Seller updateSeller(Seller seller);
    List<Seller> getAllSellers();
}
