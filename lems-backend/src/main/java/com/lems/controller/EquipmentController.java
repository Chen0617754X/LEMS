
package com.lems.controller;

import com.lems.common.ApiResponse;
import com.lems.entity.Equipment;
import com.lems.service.EquipmentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    // 构造器注入 Service
    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    // 1. 查询全部设备
    @GetMapping
    public ApiResponse<List<Equipment>> getAllEquipment() {

        List<Equipment> list = equipmentService.getAllEquipment();

        return ApiResponse.success(list);
    }

    // 2. 根据ID查询设备详情
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Equipment>> getEquipmentById(
            @PathVariable Long id) {

        Equipment equipment = equipmentService.getEquipmentById(id);

        if (equipment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(404, "设备不存在"));
        }

        return ResponseEntity.ok(
                ApiResponse.success(equipment)
        );
    }

    // 3. 新增设备
    @PostMapping
    public ResponseEntity<ApiResponse<Equipment>> addEquipment(
            @RequestBody Equipment equipment) {

        boolean success = equipmentService.addEquipment(equipment);

        if (success) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created(equipment));
        }

        return ResponseEntity.internalServerError()
                .body(ApiResponse.failure(500, "设备新增失败"));
    }

    // 4. 修改设备
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateEquipment(
            @PathVariable Long id,
            @RequestBody Equipment equipment) {

        boolean success =
                equipmentService.updateEquipment(id, equipment);

        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(404, "设备不存在或修改失败"));
        }

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }

    // 5. 删除设备
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEquipment(
            @PathVariable Long id) {

        boolean success = equipmentService.deleteEquipment(id);

        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(404, "设备不存在或删除失败"));
        }

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }

    // 6. 根据条件查询设备
    @GetMapping("/search")
    public ApiResponse<List<Equipment>> searchEquipment(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status) {

        List<Equipment> list = equipmentService.searchEquipment(
                keyword,
                categoryId,
                status
        );

        return ApiResponse.success(list);
    }
}
