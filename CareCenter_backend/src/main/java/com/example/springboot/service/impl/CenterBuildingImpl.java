package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.CenterBuilding;
import com.example.springboot.mapper.CenterBuildingMapper;
import com.example.springboot.service.CenterBuildingService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class CenterBuildingImpl extends ServiceImpl<CenterBuildingMapper, CenterBuilding> implements CenterBuildingService {


    /**
     * 注入DAO层     */
    @Resource
    private CenterBuildingMapper centerBuildingMapper;

    /**
     * 楼宇添加
     */
    @Override
    public int addNewBuilding(CenterBuilding centerBuilding) {
        int insert = centerBuildingMapper.insert(centerBuilding);
        return insert;
    }

    /**
     * 楼宇查找
     */
    @Override
    public Page find(Integer pageNum, Integer pageSize, String search) {
        Page page = new Page<>(pageNum, pageSize);
        QueryWrapper<CenterBuilding> qw = new QueryWrapper<>();
        qw.like("center_building_id", search);
        Page buildingPage = centerBuildingMapper.selectPage(page, qw);
        return buildingPage;
    }

    /**
     * 楼宇信息更新
     */
    @Override
    public int updateNewBuilding(CenterBuilding centerBuilding) {
        int i = centerBuildingMapper.updateById(centerBuilding);
        return i;
    }

    /**
     * 楼宇删除
     */
    @Override
    public int deleteBuilding(Integer id) {
        int i = centerBuildingMapper.deleteById(id);
        return i;
    }

    /**
     * 首页 获取建筑名称
     */
    @Override
    public List<CenterBuilding> getBuildingId() {
        QueryWrapper<CenterBuilding> qw = new QueryWrapper<>();
        qw.select("center_building_id");
        List<CenterBuilding> centerBuildings = centerBuildingMapper.selectList(qw);
        return centerBuildings;
    }

}

