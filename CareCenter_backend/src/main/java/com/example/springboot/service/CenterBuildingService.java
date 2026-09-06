package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.CenterBuilding;

import java.util.List;


public interface CenterBuildingService extends IService<CenterBuilding> {

    //新增楼宇
    int addNewBuilding(CenterBuilding centerBuilding);

    //查询楼宇
    Page find(Integer pageNum, Integer pageSize, String search);

    //更新楼宇信息
    int updateNewBuilding(CenterBuilding centerBuilding);

    //删除楼宇信息
    int deleteBuilding(Integer id);

    List<CenterBuilding> getBuildingId();
}

