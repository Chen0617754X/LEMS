
package com.lems.entity;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String role;
    private Integer status;
    private LocalDateTime createTime;

    public UserResponse() {
    }

    public UserResponse(SysUser user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.realName = user.getRealName();
        this.phone = user.getPhone();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.createTime = user.getCreateTime();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRealName() {
        return realName;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public Integer getStatus() {
        return status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }
}
