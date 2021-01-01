package epu.aeshop.service;

import java.util.List;

import epu.aeshop.entity.Category;

public interface CategoryService {
    List<Category> getCategories();
    Category getCategoryById(Long id);
	Category insertCategory(Category vo);
}
