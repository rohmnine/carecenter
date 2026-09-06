package com.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.CenterBuilding;
import com.example.springboot.service.CenterBuildingService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/building")
public class CenterBuildingController {

    @Resource
    private CenterBuildingService centerBuildingService;

    /**
     * 楼宇添加
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody CenterBuilding centerBuilding) {
        int i = centerBuildingService.addNewBuilding(centerBuilding);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "添加失败");
        }
    }

    /**
     * 楼宇信息更新
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody CenterBuilding centerBuilding) {
        int i = centerBuildingService.updateNewBuilding(centerBuilding);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    /**
     * 楼宇删除
     */
    @DeleteMapping("/delete/{centerBuildingId}")
    public Result<?> delete(@PathVariable Integer centerBuildingId) {
        int i = centerBuildingService.deleteBuilding(centerBuildingId);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 楼宇查找
     */
    @GetMapping("/find")
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search) {
        Page page = centerBuildingService.find(pageNum, pageSize, search);
        if (page != null) {
            return Result.success(page);
        } else {
            return Result.error("-1", "查询失败");
        }
    }

    /**
     * 首页Echarts 获取楼宇信息
     */
    @GetMapping("/getBuildingName")
    public Result<?> getBuildingName() {
        List<CenterBuilding> buildingName = centerBuildingService.getBuildingId();
        List<Integer> buildingId = buildingName.stream()
                .map(centerBuildingId -> centerBuildingId.getCenterBuildingId())
                .collect(Collectors.toList());
        return !buildingId.isEmpty() ?
                Result.success(buildingId) : Result.error("-1", "查询失败");
    }
    //        if (!buildingId.isEmpty()) {
//            return Result.success(buildingId);
//        } else {
//            return Result.error("-1", "查询失败");
//        }
}

