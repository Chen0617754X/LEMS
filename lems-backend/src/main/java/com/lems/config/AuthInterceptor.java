
package com.lems.config;

import tools.jackson.databind.ObjectMapper;
import com.lems.common.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    public AuthInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 允许跨域预检请求
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // 注册和登录接口允许未登录访问
        if ("/api/user/register".equals(uri)
                || "/api/user/login".equals(uri)) {
            return true;
        }

        // 检查登录状态
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            return writeError(response, 401, "请先登录");
        }

        String role = (String) session.getAttribute("role");

        boolean isAdmin = "ADMIN".equals(role);

        // 退出登录和个人信息接口允许所有已登录用户
        if ("/api/user/logout".equals(uri)
                || "/api/user/profile".equals(uri)) {
            return true;
        }

        // 用户管理接口仅管理员可以访问
        if (uri.equals("/api/user")
                || uri.startsWith("/api/user/")) {

            if (!isAdmin) {
                return writeError(response, 403, "没有管理员操作权限");
            }

            return true;
        }

        // 设备查询允许所有登录用户，设备修改仅管理员允许
        if (uri.startsWith("/api/equipment")) {

            if ("GET".equalsIgnoreCase(method)) {
                return true;
            }

            if (!isAdmin) {
                return writeError(response, 403, "只有管理员可以管理设备");
            }

            return true;
        }

        // 分类查询允许所有登录用户，分类修改仅管理员允许
        if (uri.startsWith("/api/category")) {

            if ("GET".equalsIgnoreCase(method)) {
                return true;
            }

            if (!isAdmin) {
                return writeError(response, 403, "只有管理员可以管理设备分类");
            }

            return true;
        }

        // 维修接口统一放行至 RepairController
        // 登录状态已经在前面检查，具体角色权限由 Controller 判断
        if (uri.equals("/api/repair")
                || uri.startsWith("/api/repair/")) {
            return true;
        }



        return true;
    }

    // 统一返回 JSON 错误信息
    private boolean writeError(
            HttpServletResponse response,
            int code,
            String message) throws Exception {

        response.setStatus(code);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        response.getWriter().write(
                objectMapper.writeValueAsString(
                        ApiResponse.failure(code, message)
                )
        );

        return false;
    }
}
