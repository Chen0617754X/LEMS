
package com.lems.service;

import com.lems.entity.SysUser;
import com.lems.entity.UserResponse;

import java.util.List;

public interface UserService {

    // 用户注册
    boolean register(SysUser user);

    // 用户登录
    UserResponse login(String username, String password);

    // 查询全部用户
    List<UserResponse> getAllUsers();

    // 根据ID查询用户
    UserResponse getUserById(Long id);

    // 修改用户信息
    boolean updateUser(Long id, SysUser user);

    // 删除用户
    boolean deleteUser(Long id);

    // 修改用户状态
    boolean updateUserStatus(Long id, Integer status);

    // 根据ID获取用户信息
    SysUser getUserEntityById(Long id);
}
