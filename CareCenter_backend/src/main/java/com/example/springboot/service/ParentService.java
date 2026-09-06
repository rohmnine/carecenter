package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.Parent;


public interface ParentService extends IService<Parent> {

    //家长登录
    Parent parentLogin(String username, String password);

    //家长注册
    int registerParent(Parent parent);

    //新增家长
    int addNewParent(Parent parent);

    //查询家长
    Page find(Integer pageNum, Integer pageSize, String search);

    //更新家长信息
    int updateNewParent(Parent parent);

    //删除家长信息
    int deleteParent(String username);

    //查询家长信息
    Parent parentInfo(String username);

    //根据学生用户名查询绑定的家长
    Parent findParentByStudentUsername(String studentUsername);

}
