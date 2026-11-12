
package com.lems.service.impl;

import com.lems.entity.Equipment;
import com.lems.mapper.EquipmentMapper;
import com.lems.service.EquipmentService;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentMapper equipmentMapper;

    // 构造器注入
    public EquipmentServiceImpl(EquipmentMapper equipmentMapper) {
        this.equipmentMapper = equipmentMapper;
    }

    // 1. 查询全部设备
    @Override
    public List<Equipment> getAllEquipment() {
        return equipmentMapper.selectAll();
    }

    // 2. 根据ID查询设备
    @Override
    public Equipment getEquipmentById(Long id) {

        checkId(id);

        return equipmentMapper.selectById(id);
    }

    // 3. 新增设备
    @Override
    public boolean addEquipment(Equipment equipment) {

        // 检查设备基本信息
        validateEquipment(equipment);

        // 设置默认值
        setDefaultValues(equipment);

        // 执行新增操作
        return equipmentMapper.insert(equipment) > 0;
    }

    // 4. 修改设备
    @Override
    public boolean updateEquipment(Long id, Equipment equipment) {

        checkId(id);

        // 判断设备是否存在
        Equipment oldEquipment = equipmentMapper.selectById(id);

        if (oldEquipment == null) {
            return false;
        }

        // 检查修改后的设备信息
        validateEquipment(equipment);

        // 设置默认值
        setDefaultValues(equipment);

        // 将路径中的ID设置到设备对象中
        equipment.setId(id);

        // 执行修改操作
        return equipmentMapper.update(equipment) > 0;
    }

    // 5. 删除设备
    @Override
    public boolean deleteEquipment(Long id) {

        checkId(id);

        // 判断设备是否存在
        Equipment equipment = equipmentMapper.selectById(id);

        if (equipment == null) {
            return false;
        }

        // 执行删除操作
        return equipmentMapper.deleteById(id) > 0;
    }

    // 6. 条件查询
    @Override
    public List<Equipment> searchEquipment(
            String keyword,
            Long categoryId,
            String status) {

        // 去除关键词和状态前后的空格
        if (keyword != null) {
            keyword = keyword.trim();
        }

        if (status != null) {
            status = status.trim();
        }

        return equipmentMapper.selectByCondition(
                keyword,
                categoryId,
                status
        );
    }

    // 检查设备ID是否合法
    private void checkId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("设备ID不合法");
        }
    }

    // 检查设备信息
    private void validateEquipment(Equipment equipment) {

        if (equipment == null) {
            throw new IllegalArgumentException("设备信息不能为空");
        }

        if (equipment.getEquipmentNo() == null
                || equipment.getEquipmentNo().isBlank()) {
            throw new IllegalArgumentException("设备编号不能为空");
        }

        if (equipment.getEquipmentName() == null
                || equipment.getEquipmentName().isBlank()) {
            throw new IllegalArgumentException("设备名称不能为空");
        }

        if (equipment.getCategoryId() == null
                || equipment.getCategoryId() <= 0) {
            throw new IllegalArgumentException("请选择正确的设备分类");
        }

        if (equipment.getPrice() != null
                && equipment.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("设备价格不能小于0");
        }
    }

    // 设置默认值
    private void setDefaultValues(Equipment equipment) {

        if (equipment.getPrice() == null) {
            equipment.setPrice(BigDecimal.ZERO);
        }

        if (equipment.getStatus() == null
                || equipment.getStatus().isBlank()) {
            equipment.setStatus("正常");
        }
    }
}
