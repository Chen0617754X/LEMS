
package com.lems.service.impl;

import com.lems.entity.Category;
import com.lems.mapper.CategoryMapper;
import com.lems.service.CategoryService;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    // 1. 查询全部分类
    @Override
    public List<Category> getAllCategories() {
        return categoryMapper.selectAll();
    }

    // 2. 根据ID查询分类
    @Override
    public Category getCategoryById(Long id) {
        checkId(id);
        return categoryMapper.selectById(id);
    }

    // 3. 新增分类
    @Override
    public boolean addCategory(Category category) {

        validateCategory(category);

        return categoryMapper.insert(category) > 0;
    }

    // 4. 修改分类
    @Override
    public boolean updateCategory(Long id, Category category) {

        checkId(id);

        Category oldCategory = categoryMapper.selectById(id);

        if (oldCategory == null) {
            return false;
        }

        validateCategory(category);

        category.setId(id);

        return categoryMapper.update(category) > 0;
    }

    // 5. 删除分类
    @Override
    public boolean deleteCategory(Long id) {

        checkId(id);

        Category category = categoryMapper.selectById(id);

        if (category == null) {
            return false;
        }

        return categoryMapper.deleteById(id) > 0;
    }

    // 检查分类ID
    private void checkId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("分类ID不合法");
        }
    }

    // 检查分类信息
    private void validateCategory(Category category) {

        if (category == null) {
            throw new IllegalArgumentException("分类信息不能为空");
        }

        if (category.getCategoryName() == null
                || category.getCategoryName().isBlank()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }

        category.setCategoryName(
                category.getCategoryName().trim()
        );

        if (category.getDescription() != null) {
            category.setDescription(
                    category.getDescription().trim()
            );
        }
    }
}
