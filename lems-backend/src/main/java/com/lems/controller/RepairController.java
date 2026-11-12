
package com.lems.controller;

import com.lems.common.ApiResponse;
import com.lems.entity.Repair;
import com.lems.service.RepairService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repair")
public class RepairController {

    private final RepairService repairService;

    public RepairController(RepairService repairService) {
        this.repairService = repairService;
    }

    // 1. 查询全部维修记录（仅管理员）
    @GetMapping
    public ResponseEntity<ApiResponse<List<Repair>>> getAllRepairs(
            HttpSession session) {

        if (!isAdmin(session)) {
            return forbidden();
        }

        return ResponseEntity.ok(
                ApiResponse.success(repairService.getAllRepairs())
        );
    }

    // 2. 查询当前用户自己的维修记录
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Repair>>> getMyRepairs(
            HttpSession session) {

        if (!isLoggedIn(session)) {
            return unauthorized();
        }

        if (!isUser(session)) {
            return forbidden();
        }

        Long userId = (Long) session.getAttribute("userId");

        return ResponseEntity.ok(
                ApiResponse.success(
                        repairService.getRepairsByUserId(userId)
                )
        );
    }

    // 3. 根据ID查询维修记录
    // 管理员可以查看全部，普通用户只能查看自己的
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Repair>> getRepairById(
            @PathVariable Long id,
            HttpSession session) {

        if (!isLoggedIn(session)) {
            return unauthorized();
        }

        Repair repair;

        if (isAdmin(session)) {

            repair = repairService.getRepairById(id);

        } else if (isUser(session)) {

            Long userId = (Long) session.getAttribute("userId");

            repair = repairService.getRepairByIdAndUserId(
                    id, userId
            );

        } else {
            return forbidden();
        }

        if (repair == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(
                            404, "维修记录不存在或无权查看"
                    ));
        }

        return ResponseEntity.ok(
                ApiResponse.success(repair)
        );
    }

    // 4. 提交维修申请
    // 管理员和普通用户均可提交
    @PostMapping
    public ResponseEntity<ApiResponse<Repair>> addRepair(
            @RequestBody Repair repair,
            HttpSession session) {

        if (!isLoggedIn(session)) {
            return unauthorized();
        }

        if (!isAdmin(session) && !isUser(session)) {
            return forbidden();
        }

        // 报修用户ID由服务器Session决定
        repair.setUserId(
                (Long) session.getAttribute("userId")
        );

        // 必须提供实体设备ID
        if (repair.getEquipmentId() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(
                            400, "请选择需要报修的设备"
                    ));
        }

        boolean success = repairService.addRepair(repair);

        if (success) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created(repair));
        }

        return ResponseEntity.internalServerError()
                .body(ApiResponse.failure(
                        500, "维修申请提交失败"
                ));
    }

    // 5. 修改维修记录（仅管理员）
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateRepair(
            @PathVariable Long id,
            @RequestBody Repair repair,
            HttpSession session) {

        if (!isLoggedIn(session)) {
            return unauthorized();
        }

        if (!isAdmin(session)) {
            return forbidden();
        }

        boolean success = repairService.updateRepair(id, repair);

        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(
                            404, "维修记录不存在或修改失败"
                    ));
        }

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }

    // 6. 删除维修记录（仅管理员）
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRepair(
            @PathVariable Long id,
            HttpSession session) {

        if (!isLoggedIn(session)) {
            return unauthorized();
        }

        if (!isAdmin(session)) {
            return forbidden();
        }

        boolean success = repairService.deleteRepair(id);

        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(
                            404, "维修记录不存在或删除失败"
                    ));
        }

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }

    // 7. 条件查询维修记录（仅管理员）
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Repair>>> searchRepairs(
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) String repairStatus,
            @RequestParam(required = false) String keyword,
            HttpSession session) {

        if (!isLoggedIn(session)) {
            return unauthorized();
        }

        if (!isAdmin(session)) {
            return forbidden();
        }

        List<Repair> list = repairService.searchRepairs(
                equipmentId,
                repairStatus,
                keyword
        );

        return ResponseEntity.ok(
                ApiResponse.success(list)
        );
    }

    // 判断是否已经登录
    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("userId") != null;
    }

    // 判断是否为管理员
    private boolean isAdmin(HttpSession session) {
        return "ADMIN".equals(session.getAttribute("role"));
    }

    // 判断是否为普通用户
    private boolean isUser(HttpSession session) {
        return "USER".equals(session.getAttribute("role"));
    }

    // 未登录返回401
    private <T> ResponseEntity<ApiResponse<T>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.failure(401, "请先登录"));
    }

    // 无权限返回403
    private <T> ResponseEntity<ApiResponse<T>> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure(403, "没有权限执行此操作"));
    }
}
