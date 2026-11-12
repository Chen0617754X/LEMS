
package com.lems.service.impl;

import com.lems.entity.Repair;
import com.lems.mapper.RepairMapper;
import com.lems.service.RepairService;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class RepairServiceImpl implements RepairService {

    private final RepairMapper repairMapper;

    private static final Set<String> VALID_STATUSES =
            Set.of("待维修", "维修中", "已完成");

    public RepairServiceImpl(RepairMapper repairMapper) {
        this.repairMapper = repairMapper;
    }

    // 1. 查询全部维修记录（管理员）
    @Override
    public List<Repair> getAllRepairs() {
        return repairMapper.selectAll();
    }

    // 2. 根据ID查询维修记录
    @Override
    public Repair getRepairById(Long id) {
        checkId(id);
        return repairMapper.selectById(id);
    }

    // 3. 根据用户ID查询该用户自己的维修记录
    @Override
    public List<Repair> getRepairsByUserId(Long userId) {
        checkUserId(userId);
        return repairMapper.selectByUserId(userId);
    }

    // 4. 根据维修记录ID和用户ID查询
    @Override
    public Repair getRepairByIdAndUserId(Long id, Long userId) {
        checkId(id);
        checkUserId(userId);

        return repairMapper.selectByIdAndUserId(id, userId);
    }

    // 5. 新增维修记录
    @Override
    public boolean addRepair(Repair repair) {

        validateRepair(repair);

        // 检查设备是否存在且处于正常状态
        checkEquipment(repair.getEquipmentId());

        // 检查报修用户ID
        if (repair.getUserId() != null) {
            checkUserId(repair.getUserId());
        }

        // 新建维修记录默认处于待维修状态
        repair.setRepairStatus("待维修");

        // 新建记录时不设置维修人员和维修结果
        repair.setRepairPerson(null);
        repair.setRepairResult(null);
        repair.setFinishTime(null);

        // 直接通过equipmentId新增维修记录
        return repairMapper.insert(repair) > 0;
    }

    // 6. 修改维修记录
    @Override
    public boolean updateRepair(Long id, Repair repair) {

        checkId(id);

        Repair oldRepair = repairMapper.selectById(id);

        if (oldRepair == null) {
            return false;
        }

        validateRepair(repair);

        // 检查新的设备是否存在且处于正常状态
        checkEquipment(repair.getEquipmentId());

        if (repair.getRepairStatus() == null
                || repair.getRepairStatus().isBlank()) {
            throw new IllegalArgumentException("请选择维修状态");
        }

        checkStatus(repair.getRepairStatus());

        // 已完成时记录完成时间
        if ("已完成".equals(repair.getRepairStatus())) {

            if ("已完成".equals(oldRepair.getRepairStatus())
                    && oldRepair.getFinishTime() != null) {

                repair.setFinishTime(oldRepair.getFinishTime());

            } else {
                repair.setFinishTime(LocalDateTime.now());
            }

        } else {
            repair.setFinishTime(null);
        }

        repair.setId(id);

        return repairMapper.update(repair) > 0;
    }

    // 7. 删除维修记录
    @Override
    public boolean deleteRepair(Long id) {

        checkId(id);

        Repair repair = repairMapper.selectById(id);

        if (repair == null) {
            return false;
        }

        return repairMapper.deleteById(id) > 0;
    }

    // 8. 条件查询维修记录（管理员）
    @Override
    public List<Repair> searchRepairs(
            Long equipmentId,
            String repairStatus,
            String keyword) {

        if (equipmentId != null && equipmentId <= 0) {
            throw new IllegalArgumentException("设备ID不合法");
        }

        if (repairStatus != null) {
            repairStatus = repairStatus.trim();

            if (!repairStatus.isEmpty()) {
                checkStatus(repairStatus);
            }
        }

        if (keyword != null) {
            keyword = keyword.trim();
        }

        return repairMapper.selectByCondition(
                equipmentId,
                repairStatus,
                keyword
        );
    }

    // 检查维修记录ID
    private void checkId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("维修记录ID不合法");
        }
    }

    // 检查用户ID
    private void checkUserId(Long userId) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不合法");
        }
    }

    // 检查设备是否存在且处于正常状态
    private void checkEquipment(Long equipmentId) {

        if (equipmentId == null || equipmentId <= 0) {
            throw new IllegalArgumentException("请选择设备");
        }

        int count = repairMapper.countUsableEquipment(equipmentId);

        if (count <= 0) {
            throw new IllegalArgumentException(
                    "设备不存在或当前状态不可报修"
            );
        }
    }

    // 检查维修记录内容
    private void validateRepair(Repair repair) {

        if (repair == null) {
            throw new IllegalArgumentException("维修记录不能为空");
        }

        if (repair.getFaultDescription() == null
                || repair.getFaultDescription().isBlank()) {
            throw new IllegalArgumentException("故障描述不能为空");
        }

        if (repair.getFaultDescription().length() > 5000) {
            throw new IllegalArgumentException(
                    "故障描述不能超过5000个字符"
            );
        }

        if (repair.getRepairPerson() != null) {
            repair.setRepairPerson(repair.getRepairPerson().trim());
        }

        if (repair.getRepairResult() != null) {
            repair.setRepairResult(repair.getRepairResult().trim());
        }
    }

    // 检查维修状态是否合法
    private void checkStatus(String status) {

        if (!VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException(
                    "维修状态只能是：待维修、维修中或已完成"
            );
        }
    }
}
