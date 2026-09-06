package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.UserEntity;

import java.util.List;

public interface UserEntityService extends IService<UserEntity> {
    
    // 统一登录
    UserEntity login(String username, String password, String userType);
    
    // 按用户类型分页查询?
    Page<UserEntity> findByUserType(Integer pageNum, Integer pageSize, String userType, String search);
    
    // 新增用户
    int addUser(UserEntity user);
    
    // 更新用户
    int updateUser(UserEntity user);
    
    // 删除用户
    int deleteUser(String username);
    
    // 统计用户数量
    int countByUserType(String userType);
    
    // 查询用户信息
    UserEntity getUserInfo(String username);

    // 忘记密码：通过用户名和姓名重置密码
    int resetPasswordByUsernameAndName(String username, String name, String newPassword, String userType);
    
    // 学生特有方法
    int updateStudentBoardingType(String username, String boardingType);
    
    // 家长特有方法（兼容旧逻辑：返回第一个匹配家长）
    UserEntity getParentByStudentUsername(String studentUsername);

    // 家长特有方法（多孩子绑定）
    List<UserEntity> getParentsByStudentUsername(String studentUsername);

    // 获取家长绑定的全部孩子用户名
    List<String> getBoundStudentUsernames(String parentUsername);

    // 覆盖家长绑定的孩子列表
    int bindStudents(String parentUsername, List<String> studentUsernames);

    // 查询所有指定类型的用户（不分页）
    List<UserEntity> findAllByUserType(String userType, String search);
}
