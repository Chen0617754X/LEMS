
package com.lems.service;

import com.lems.entity.Equipment;

import java.util.List;

public interface EquipmentService {

    // 1. 查询全部设备
    List<Equipment> getAllEquipment();

    // 2. 根据ID查询设备详情
    Equipment getEquipmentById(Long id);

    // 3. 新增设备
    boolean addEquipment(Equipment equipment);

    // 4. 修改设备
    boolean updateEquipment(Long id, Equipment equipment);

    // 5. 删除设备
    boolean deleteEquipment(Long id);

    // 6. 根据条件查询设备
    List<Equipment> searchEquipment(
            String keyword,
            Long categoryId,
            String status
    );
}
