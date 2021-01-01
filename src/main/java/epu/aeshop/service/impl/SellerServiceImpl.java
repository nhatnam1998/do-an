package epu.aeshop.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import epu.aeshop.entity.Buyer;
import epu.aeshop.entity.Seller;
import epu.aeshop.entity.User;
import epu.aeshop.repository.SellerRepository;
import epu.aeshop.service.SellerService;

import java.util.List;

@Service
public class SellerServiceImpl implements SellerService {
    @Autowired
    SellerRepository sellerRepository;

    @Override
    public Seller getSellerById(Long id) {
        return sellerRepository.findById(id).get();
    }

    @Override
    public Seller save(Seller seller) {
        return sellerRepository.save(seller);
    }

    @Override
    public Seller getSellerByUser(User user) {
        return sellerRepository.findSellerByUser(user);
    }

    @Override
    public List<Buyer> getFollowers(Long sellerId) {
        return sellerRepository.findById(sellerId).get().getBuyers();
    }

    public Seller updateSeller(Seller seller) {
        Seller selectSeller = sellerRepository.findById(seller.getId()).get();
        selectSeller.setName(seller.getName());
        selectSeller.setDescription(seller.getDescription());
        selectSeller.setPicture(seller.getPicture());
        return sellerRepository.save(selectSeller);
    }

    @Override
    public List<Seller> getAllSellers() {
        return (List) sellerRepository.findAll();
    }
}
