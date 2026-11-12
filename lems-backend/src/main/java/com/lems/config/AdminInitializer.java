
package com.lems.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder) {

        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {

        // 检查管理员账号是否已经存在
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE username = ?",
                Integer.class,
                "admin"
        );

        if (count != null && count == 0) {

            // 设置初始管理员密码
            String rawPassword = "Admin@123456";

            // 使用 BCrypt 加密
            String encodedPassword =
                    passwordEncoder.encode(rawPassword);

            // 创建管理员账号
            jdbcTemplate.update("""
                INSERT INTO sys_user (
                    username,
                    password,
                    real_name,
                    phone,
                    role,
                    status,
                    create_time
                )
                VALUES (?, ?, ?, ?, ?, ?, NOW())
                """,
                    "admin",
                    encodedPassword,
                    "系统管理员",
                    "",
                    "ADMIN",
                    1
            );

            System.out.println("================================");
            System.out.println("LEMS 管理员账号初始化成功！");
            System.out.println("用户名：admin");
            System.out.println("初始密码：Admin@123456");
            System.out.println("请在首次登录后更换密码！");
            System.out.println("================================");

        } else {

            System.out.println("LEMS 管理员账号已经存在，无需重复初始化。");
        }
    }
}
