package epu.aeshop.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import epu.aeshop.entity.Category;
import epu.aeshop.entity.Product;
import epu.aeshop.entity.Seller;
import epu.aeshop.vo.ProductVO;

import java.util.List;

@Repository
public interface ProductRepository extends CrudRepository<Product, Long> {
    List<Product> findProductsByCategory(Category category);

    List<Product> findProductsBySeller(Seller seller);

    @Query("SELECT p FROM Product p WHERE p.name LIKE ?1 ")
    List<Product> findProductsByName(String name);
    
    @Query("SELECT new epu.aeshop.vo.ProductVO(p.id, p.name, p.description, p.origin, p.brand) FROM Product p ")
    List<ProductVO> getSearch();
}
