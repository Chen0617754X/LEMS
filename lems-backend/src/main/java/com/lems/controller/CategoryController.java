
package com.lems.controller;

import com.lems.common.ApiResponse;
import com.lems.entity.Category;
import com.lems.service.CategoryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // 1. 查询全部分类
    @GetMapping
    public ApiResponse<List<Category>> getAllCategories() {

        return ApiResponse.success(
                categoryService.getAllCategories()
        );
    }

    // 2. 根据ID查询分类
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(
            @PathVariable Long id) {

        Category category = categoryService.getCategoryById(id);

        if (category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(404, "分类不存在"));
        }

        return ResponseEntity.ok(
                ApiResponse.success(category)
        );
    }

    // 3. 新增分类
    @PostMapping
    public ResponseEntity<ApiResponse<Category>> addCategory(
            @RequestBody Category category) {

        boolean success = categoryService.addCategory(category);

        if (success) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created(category));
        }

        return ResponseEntity.internalServerError()
                .body(ApiResponse.failure(500, "分类新增失败"));
    }

    // 4. 修改分类
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateCategory(
            @PathVariable Long id,
            @RequestBody Category category) {

        boolean success =
                categoryService.updateCategory(id, category);

        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(404, "分类不存在或修改失败"));
        }

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }

    // 5. 删除分类
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long id) {

        boolean success = categoryService.deleteCategory(id);

        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(404, "分类不存在或删除失败"));
        }

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }
}
