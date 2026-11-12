
package com.lems.service;

import com.lems.entity.Repair;

import java.util.List;

public interface RepairService {

    // 查询全部维修记录
    List<Repair> getAllRepairs();

    // 根据ID查询维修记录
    Repair getRepairById(Long id);

    // 新增维修记录
    boolean addRepair(Repair repair);

    // 修改维修记录
    boolean updateRepair(Long id, Repair repair);

    // 删除维修记录
    boolean deleteRepair(Long id);

    // 条件查询维修记录
    List<Repair> searchRepairs(
            Long equipmentId,
            String repairStatus,
            String keyword
    );

    // 根据用户ID查询该用户自己的维修记录
    List<Repair> getRepairsByUserId(Long userId);

    // 根据维修记录ID和用户ID查询记录
    Repair getRepairByIdAndUserId(Long id, Long userId);
}
