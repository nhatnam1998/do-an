package epu.aeshop.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import epu.aeshop.entity.Category;
import epu.aeshop.repository.CategoryRepository;
import epu.aeshop.service.CategoryService;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getCategories() {
        return (List<Category>) categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).get();
    }
    
    @Override
    public Category insertCategory(Category vo) {
    	Category newCategory = new Category();
    	if(categoryRepository.findById(vo.getId()) == null) {
    		
    		newCategory.setDescription(vo.getDescription());
    		newCategory.setName(vo.getName());
    		categoryRepository.save(newCategory);
    	}
    	return newCategory;
    }
}
