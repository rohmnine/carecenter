package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.DishSuggestion;
import com.example.springboot.service.DishSuggestionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 菜品建议Controller
 */
@RestController
@RequestMapping("/dishSuggestion")
public class DishSuggestionController {
    
    @Resource
    private DishSuggestionService dishSuggestionService;
    
    /**
     * 添加建议
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody DishSuggestion dishSuggestion) {
        try {
            dishSuggestion.setStatus("pending");
            boolean result = dishSuggestionService.save(dishSuggestion);
            return result ? Result.success("建议已提交") : Result.error("-1", "提交失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("-1", "提交失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新建议（管理员回复信息
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody DishSuggestion dishSuggestion) {
        try {
            boolean result = dishSuggestionService.updateById(dishSuggestion);
            return result ? Result.success("更新成功") : Result.error("-1", "更新失败");
        } catch (Exception e) {
            return Result.error("-1", "更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除建议
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        boolean result = dishSuggestionService.removeById(id);
        return result ? Result.success() : Result.error("-1", "删除失败");
    }
    
    /**
     * 分页查询建议
     */
    @GetMapping("/find")
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) String userUsername,
                              @RequestParam(required = false) String userType) {
        QueryWrapper<DishSuggestion> queryWrapper = new QueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            if ("reviewed".equals(status)) {
                queryWrapper.in("status", "reviewed", "adopted");
            } else {
                queryWrapper.eq("status", status);
            }
        }
        if (userUsername != null) {
            queryWrapper.eq("user_username", userUsername);
        }
        if (userType != null && !userType.isEmpty()) {
            queryWrapper.eq("user_type", userType);
        }
        
        queryWrapper.orderByDesc("create_time");
        Page<DishSuggestion> page = dishSuggestionService.page(new Page<>(pageNum, pageSize), queryWrapper);
        return Result.success(page);
    }
    
    /**
     * 获取用户的建议列表
     */
    @GetMapping("/myList")
    public Result<?> getMyList(@RequestParam String userUsername,
                               @RequestParam String userType,
                               @RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize) {
        QueryWrapper<DishSuggestion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_username", userUsername);
        queryWrapper.eq("user_type", userType);
        queryWrapper.orderByDesc("create_time");
        
        Page<DishSuggestion> page = dishSuggestionService.page(new Page<>(pageNum, pageSize), queryWrapper);
        return Result.success(page);
    }
    
    /**
     * 获取待处理的建议数量
     */
    @GetMapping("/pendingCount")
    public Result<?> getPendingCount() {
        QueryWrapper<DishSuggestion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "pending");
        long count = dishSuggestionService.count(queryWrapper);
        return Result.success(count);
    }
}
