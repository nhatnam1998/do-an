package epu.aeshop.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import epu.aeshop.entity.Category;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Long> {

}
