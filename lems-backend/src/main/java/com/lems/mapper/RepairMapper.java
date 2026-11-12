
package com.lems.mapper;

import com.lems.entity.Repair;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RepairMapper {

    // 统一查询字段
    String COLUMNS = """
        r.id,
        r.equipment_id,
        r.user_id,
        e.equipment_name,
        e.equipment_no,
        r.fault_description,
        r.repair_person,
        r.repair_status,
        r.repair_result,
        r.report_time,
        r.finish_time
        """;

    // 统一关联设备表
    String FROM_TABLES = """
        FROM equipment_repair r
        LEFT JOIN equipment e ON r.equipment_id = e.id
        """;

    // 1. 查询全部维修记录（管理员）
    @Select("""
        SELECT
            r.id,
            r.equipment_id,
            r.user_id,
            e.equipment_name,
            e.equipment_no,
            r.fault_description,
            r.repair_person,
            r.repair_status,
            r.repair_result,
            r.report_time,
            r.finish_time
        FROM equipment_repair r
        LEFT JOIN equipment e ON r.equipment_id = e.id
        ORDER BY r.report_time DESC
        """)
    List<Repair> selectAll();

    // 2. 根据ID查询维修记录
    @Select("""
        SELECT
            r.id,
            r.equipment_id,
            r.user_id,
            e.equipment_name,
            e.equipment_no,
            r.fault_description,
            r.repair_person,
            r.repair_status,
            r.repair_result,
            r.report_time,
            r.finish_time
        FROM equipment_repair r
        LEFT JOIN equipment e ON r.equipment_id = e.id
        WHERE r.id = #{id}
        """)
    Repair selectById(@Param("id") Long id);

    // 3. 根据用户ID查询该用户的维修记录
    @Select("""
        SELECT
            r.id,
            r.equipment_id,
            r.user_id,
            e.equipment_name,
            e.equipment_no,
            r.fault_description,
            r.repair_person,
            r.repair_status,
            r.repair_result,
            r.report_time,
            r.finish_time
        FROM equipment_repair r
        LEFT JOIN equipment e ON r.equipment_id = e.id
        WHERE r.user_id = #{userId}
        ORDER BY r.report_time DESC
        """)
    List<Repair> selectByUserId(@Param("userId") Long userId);

    // 4. 根据维修记录ID和用户ID查询
    @Select("""
        SELECT
            r.id,
            r.equipment_id,
            r.user_id,
            e.equipment_name,
            e.equipment_no,
            r.fault_description,
            r.repair_person,
            r.repair_status,
            r.repair_result,
            r.report_time,
            r.finish_time
        FROM equipment_repair r
        LEFT JOIN equipment e ON r.equipment_id = e.id
        WHERE r.id = #{id}
          AND r.user_id = #{userId}
        """)
    Repair selectByIdAndUserId(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    // 5. 新增维修记录
    @Insert("""
        INSERT INTO equipment_repair (
            equipment_id,
            user_id,
            fault_description,
            repair_person,
            repair_status,
            repair_result
        )
        VALUES (
            #{equipmentId},
            #{userId},
            #{faultDescription},
            #{repairPerson},
            #{repairStatus},
            #{repairResult}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Repair repair);

    // 6. 修改维修记录
    @Update("""
        UPDATE equipment_repair
        SET
            equipment_id = #{equipmentId},
            fault_description = #{faultDescription},
            repair_person = #{repairPerson},
            repair_status = #{repairStatus},
            repair_result = #{repairResult},
            finish_time = #{finishTime}
        WHERE id = #{id}
        """)
    int update(Repair repair);

    // 7. 删除维修记录
    @Delete("""
        DELETE FROM equipment_repair
        WHERE id = #{id}
        """)
    int deleteById(@Param("id") Long id);

    // 8. 根据条件查询维修记录（管理员）
    @Select("""
        <script>
        SELECT
            r.id,
            r.equipment_id,
            r.user_id,
            e.equipment_name,
            e.equipment_no,
            r.fault_description,
            r.repair_person,
            r.repair_status,
            r.repair_result,
            r.report_time,
            r.finish_time
        FROM equipment_repair r
        LEFT JOIN equipment e ON r.equipment_id = e.id
        <where>
            <if test="equipmentId != null">
                AND r.equipment_id = #{equipmentId}
            </if>

            <if test="repairStatus != null and repairStatus != ''">
                AND r.repair_status = #{repairStatus}
            </if>

            <if test="keyword != null and keyword != ''">
                AND (
                    e.equipment_name LIKE CONCAT('%', #{keyword}, '%')
                    OR e.equipment_no LIKE CONCAT('%', #{keyword}, '%')
                    OR r.fault_description LIKE CONCAT('%', #{keyword}, '%')
                )
            </if>
        </where>
        ORDER BY r.report_time DESC
        </script>
        """)
    List<Repair> selectByCondition(
            @Param("equipmentId") Long equipmentId,
            @Param("repairStatus") String repairStatus,
            @Param("keyword") String keyword
    );

    // 9. 检查设备是否存在且处于正常状态
    @Select("""
        SELECT COUNT(*)
        FROM equipment
        WHERE id = #{equipmentId}
          AND status = '正常'
        """)
    int countUsableEquipment(@Param("equipmentId") Long equipmentId);
}
