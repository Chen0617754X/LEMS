
package com.lems.mapper;

import com.lems.entity.SysUser;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    // 1. 查询全部用户
    @Select("""
        SELECT id, username, password, real_name,
               phone, role, status, create_time
        FROM sys_user
        ORDER BY id DESC
        """)
    List<SysUser> selectAll();


    // 2. 根据ID查询用户
    @Select("""
        SELECT id, username, password, real_name,
               phone, role, status, create_time
        FROM sys_user
        WHERE id = #{id}
        """)
    SysUser selectById(@Param("id") Long id);


    // 3. 根据用户名查询用户
    @Select("""
        SELECT id, username, password, real_name,
               phone, role, status, create_time
        FROM sys_user
        WHERE username = #{username}
        """)
    SysUser selectByUsername(@Param("username") String username);


    // 4. 新增用户
    @Insert("""
        INSERT INTO sys_user (
            username,
            password,
            real_name,
            phone,
            role,
            status
        )
        VALUES (
            #{username},
            #{password},
            #{realName},
            #{phone},
            #{role},
            #{status}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUser user);


    // 5. 修改用户基本信息
    @Update("""
        UPDATE sys_user
        SET
            real_name = #{realName},
            phone = #{phone}
        WHERE id = #{id}
        """)
    int update(SysUser user);


    // 6. 修改用户状态
    @Update("""
        UPDATE sys_user
        SET status = #{status}
        WHERE id = #{id}
        """)
    int updateStatus(
            @Param("id") Long id,
            @Param("status") Integer status
    );


    // 7. 删除用户
    @Delete("""
        DELETE FROM sys_user
        WHERE id = #{id}
        """)
    int deleteById(@Param("id") Long id);
}
