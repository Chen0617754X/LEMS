
package com.lems.controller;

import com.lems.common.ApiResponse;
import com.lems.entity.LoginRequest;
import com.lems.entity.SysUser;
import com.lems.entity.UserResponse;
import com.lems.service.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 1. 用户注册
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @RequestBody SysUser user) {

        userService.register(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(null));
    }

    // 2. 用户登录
    @PostMapping("/login")
    public ApiResponse<UserResponse> login(
            @RequestBody LoginRequest request,
            HttpSession session) {

        UserResponse user = userService.login(
                request.getUsername(),
                request.getPassword()
        );

        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole());

        return ApiResponse.success(user);
    }

    // 3. 用户退出
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {

        session.invalidate();

        return ApiResponse.success(null);
    }

    // 4. 查询当前登录用户信息
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure(401, "请先登录"));
        }

        UserResponse user = userService.getUserById(userId);

        if (user == null) {
            session.invalidate();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure(401, "用户不存在，请重新登录"));
        }

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    // 5. 查询全部用户（仅管理员）
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            HttpSession session) {

        if (!isAdmin(session)) {
            return forbidden();
        }

        return ResponseEntity.ok(
                ApiResponse.success(userService.getAllUsers())
        );
    }

    // 6. 根据ID查询用户（仅管理员）
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return forbidden();
        }

        UserResponse user = userService.getUserById(id);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(404, "用户不存在"));
        }

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    // 7. 修改用户基本信息（仅管理员）
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateUser(
            @PathVariable Long id,
            @RequestBody SysUser user,
            HttpSession session) {

        if (!isAdmin(session)) {
            return forbidden();
        }

        boolean success = userService.updateUser(id, user);

        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(404, "用户不存在或修改失败"));
        }

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 8. 删除用户（仅管理员）
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return forbidden();
        }

        Long currentUserId = (Long) session.getAttribute("userId");

        if (id.equals(currentUserId)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(400, "不能删除当前登录账号"));
        }

        boolean success = userService.deleteUser(id);

        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(404, "用户不存在或删除失败"));
        }

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 9. 修改用户状态（仅管理员）
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long id,
            @RequestBody SysUser user,
            HttpSession session) {

        if (!isAdmin(session)) {
            return forbidden();
        }

        Long currentUserId = (Long) session.getAttribute("userId");

        if (id.equals(currentUserId) && user.getStatus() != null
                && user.getStatus() == 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(400, "不能禁用当前登录账号"));
        }

        boolean success = userService.updateUserStatus(
                id,
                user.getStatus()
        );

        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(404, "用户不存在或状态修改失败"));
        }

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 检查当前用户是否为管理员
    private boolean isAdmin(HttpSession session) {

        return "ADMIN".equals(session.getAttribute("role"));
    }

    // 无管理员权限时返回403
    private <T> ResponseEntity<ApiResponse<T>> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure(403, "没有管理员操作权限"));
    }
}
