
package com.lems.service.impl;

import com.lems.entity.SysUser;
import com.lems.entity.UserResponse;
import com.lems.mapper.UserMapper;
import com.lems.service.UserService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder) {

        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. 用户注册
    @Override
    public boolean register(SysUser user) {

        if (user == null) {
            throw new IllegalArgumentException("用户信息不能为空");
        }

        if (user.getUsername() == null
                || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        if (!user.getUsername().matches("^[a-zA-Z0-9_]{4,20}$")) {
            throw new IllegalArgumentException(
                    "用户名必须为4到20位字母、数字或下划线"
            );
        }

        if (user.getPassword() == null
                || user.getPassword().length() < 8
                || user.getPassword().length() > 72) {
            throw new IllegalArgumentException(
                    "密码长度必须为8到72个字符"
            );
        }

        if (userMapper.selectByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已经存在");
        }

        // 密码使用 BCrypt 加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 注册用户一律设置为普通用户
        user.setRole("USER");

        // 新用户默认启用
        user.setStatus(1);

        return userMapper.insert(user) > 0;
    }

    // 2. 用户登录
    @Override
    public UserResponse login(String username, String password) {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        SysUser user = userMapper.selectByUsername(username.trim());

        if (user == null
                || !passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new IllegalArgumentException("该用户已被禁用");
        }

        return new UserResponse(user);
    }

    // 3. 查询全部用户
    @Override
    public List<UserResponse> getAllUsers() {

        return userMapper.selectAll()
                .stream()
                .map(UserResponse::new)
                .toList();
    }

    // 4. 根据ID查询用户
    @Override
    public UserResponse getUserById(Long id) {

        checkId(id);

        SysUser user = userMapper.selectById(id);

        return user == null ? null : new UserResponse(user);
    }

    // 5. 修改用户基本信息
    @Override
    public boolean updateUser(Long id, SysUser user) {

        checkId(id);

        if (userMapper.selectById(id) == null) {
            return false;
        }

        if (user == null) {
            throw new IllegalArgumentException("用户信息不能为空");
        }

        if (user.getRealName() != null
                && user.getRealName().length() > 50) {
            throw new IllegalArgumentException("真实姓名不能超过50个字符");
        }

        if (user.getPhone() != null
                && user.getPhone().length() > 20) {
            throw new IllegalArgumentException("手机号不能超过20个字符");
        }

        user.setId(id);

        return userMapper.update(user) > 0;
    }

    // 6. 删除用户
    @Override
    public boolean deleteUser(Long id) {

        checkId(id);

        if (userMapper.selectById(id) == null) {
            return false;
        }

        return userMapper.deleteById(id) > 0;
    }

    // 7. 修改用户状态
    @Override
    public boolean updateUserStatus(Long id, Integer status) {

        checkId(id);

        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException(
                    "用户状态只能为0（禁用）或1（启用）"
            );
        }

        if (userMapper.selectById(id) == null) {
            return false;
        }

        return userMapper.updateStatus(id, status) > 0;
    }

    // 根据ID获取用户实体
    @Override
    public SysUser getUserEntityById(Long id) {
        checkId(id);
        return userMapper.selectById(id);
    }

    // 检查用户ID
    private void checkId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("用户ID不合法");
        }
    }
}
