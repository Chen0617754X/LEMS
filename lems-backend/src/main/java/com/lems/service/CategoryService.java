
package com.lems.service;

import com.lems.entity.Category;

import java.util.List;

public interface CategoryService {

    // 查询全部分类
    List<Category> getAllCategories();

    // 根据ID查询分类
    Category getCategoryById(Long id);

    // 新增分类
    boolean addCategory(Category category);

    // 修改分类
    boolean updateCategory(Long id, Category category);

    // 删除分类
    boolean deleteCategory(Long id);
}
