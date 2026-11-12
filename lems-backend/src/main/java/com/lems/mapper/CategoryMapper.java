
package com.lems.mapper;

import com.lems.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    // 1. 查询全部分类
    @Select("""
        SELECT id, category_name, description, create_time
        FROM equipment_category
        ORDER BY id
        """)
    List<Category> selectAll();


    // 2. 根据ID查询分类
    @Select("""
        SELECT id, category_name, description, create_time
        FROM equipment_category
        WHERE id = #{id}
        """)
    Category selectById(@Param("id") Long id);


    // 3. 新增分类
    @Insert("""
        INSERT INTO equipment_category
        (category_name, description)
        VALUES
        (#{categoryName}, #{description})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);


    // 4. 修改分类
    @Update("""
        UPDATE equipment_category
        SET
            category_name = #{categoryName},
            description = #{description}
        WHERE id = #{id}
        """)
    int update(Category category);


    // 5. 删除分类
    @Delete("""
        DELETE FROM equipment_category
        WHERE id = #{id}
        """)
    int deleteById(@Param("id") Long id);
}
