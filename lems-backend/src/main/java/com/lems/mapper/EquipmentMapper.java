
package com.lems.mapper;

import com.lems.entity.Equipment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EquipmentMapper {

    // 1. 查询全部设备
    @Select("""
        SELECT
            id,
            equipment_no,
            equipment_name,
            category_id,
            brand,
            model,
            price,
            location,
            status,
            purchase_date,
            description
        FROM equipment
        ORDER BY id
        """)
    List<Equipment> selectAll();


    // 2. 根据ID查询设备
    @Select("""
        SELECT
            id,
            equipment_no,
            equipment_name,
            category_id,
            brand,
            model,
            price,
            location,
            status,
            purchase_date,
            description
        FROM equipment
        WHERE id = #{id}
        """)
    Equipment selectById(@Param("id") Long id);


    // 3. 新增设备
    @Insert("""
        INSERT INTO equipment (
            equipment_no,
            equipment_name,
            category_id,
            brand,
            model,
            price,
            location,
            status,
            purchase_date,
            description
        )
        VALUES (
            #{equipmentNo},
            #{equipmentName},
            #{categoryId},
            #{brand},
            #{model},
            #{price},
            #{location},
            #{status},
            #{purchaseDate},
            #{description}
        )
        """)
    @Options(
            useGeneratedKeys = true,
            keyProperty = "id"
    )
    int insert(Equipment equipment);


    // 4. 修改设备
    @Update("""
        UPDATE equipment
        SET
            equipment_no = #{equipmentNo},
            equipment_name = #{equipmentName},
            category_id = #{categoryId},
            brand = #{brand},
            model = #{model},
            price = #{price},
            location = #{location},
            status = #{status},
            purchase_date = #{purchaseDate},
            description = #{description}
        WHERE id = #{id}
        """)
    int update(Equipment equipment);


    // 5. 删除设备
    @Delete("""
        DELETE FROM equipment
        WHERE id = #{id}
        """)
    int deleteById(@Param("id") Long id);


    // 6. 根据条件查询设备
    @Select("""
        <script>
        SELECT
            id,
            equipment_no,
            equipment_name,
            category_id,
            brand,
            model,
            price,
            location,
            status,
            purchase_date,
            description
        FROM equipment
        <where>
            <if test="keyword != null and keyword != ''">
                AND (
                    equipment_name LIKE CONCAT('%', #{keyword}, '%')
                    OR equipment_no LIKE CONCAT('%', #{keyword}, '%')
                )
            </if>

            <if test="categoryId != null">
                AND category_id = #{categoryId}
            </if>

            <if test="status != null and status != ''">
                AND status = #{status}
            </if>
        </where>
        ORDER BY id
        </script>
        """)
    List<Equipment> selectByCondition(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") String status
    );
}
