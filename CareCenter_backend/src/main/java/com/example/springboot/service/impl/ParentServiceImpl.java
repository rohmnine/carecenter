package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Parent;
import com.example.springboot.mapper.ParentMapper;
import com.example.springboot.service.ParentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
public class ParentServiceImpl extends ServiceImpl<ParentMapper, Parent> implements ParentService {

    /**
     * 注入DAO层对接
     */
    @Resource
    private ParentMapper parentMapper;

    /**
     * 家长登录
     */
    @Override
    public Parent parentLogin(String username, String password) {
        QueryWrapper<Parent> qw = new QueryWrapper<>();
        qw.eq("username", username);
        qw.eq("password", password);
        qw.eq("user_type", "parent");
        Parent parent = parentMapper.selectOne(qw);
        if (parent != null) {
            return parent;
        } else {
            return null;
        }
    }

    /**
     * 家长注册
     */
    @Override
    public int registerParent(Parent parent) {
        // 检查用户名是否已存在
        QueryWrapper<Parent> qw = new QueryWrapper<>();
        qw.eq("username", parent.getUsername());
        qw.eq("user_type", "parent");
        Parent existParent = parentMapper.selectOne(qw);
        if (existParent != null) {
            return -1; // 用户名已存在
        }
        int insert = parentMapper.insert(parent);
        return insert;
    }

    /**
     * 家长新增
     */
    @Override
    public int addNewParent(Parent parent) {
        parent.setUserType("parent");
        int insert = parentMapper.insert(parent);
        return insert;
    }

    /**
     * 分页查询家长
     */
    @Override
    public Page find(Integer pageNum, Integer pageSize, String search) {
        Page page = new Page<>(pageNum, pageSize);
        QueryWrapper<Parent> qw = new QueryWrapper<>();
        qw.eq("user_type", "parent");
        qw.like("name", search);
        Page parentPage = parentMapper.selectPage(page, qw);
        return parentPage;
    }

    /**
     * 更新家长信息
     */
    @Override
    public int updateNewParent(Parent parent) {
        int i = parentMapper.updateById(parent);
        return i;
    }

    /**
     * 删除家长信息
     */
    @Override
    public int deleteParent(String username) {
        int i = parentMapper.deleteById(username);
        return i;
    }

    /**
     * 查询家长信息
     */
    @Override
    public Parent parentInfo(String username) {
        QueryWrapper<Parent> qw = new QueryWrapper<>();
        qw.eq("username", username);
        qw.eq("user_type", "parent");
        Parent parent = parentMapper.selectOne(qw);
        return parent;
    }

    /**
     * 根据学生用户名查询绑定的家长
     */
    @Override
    public Parent findParentByStudentUsername(String studentUsername) {
        QueryWrapper<Parent> qw = new QueryWrapper<>();
        qw.eq("student_username", studentUsername);
        qw.eq("user_type", "parent");
        Parent parent = parentMapper.selectOne(qw);
        return parent;
    }
}
