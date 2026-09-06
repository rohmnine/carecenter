package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Menu;
import com.example.springboot.service.MenuService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;

/**
 * 菜谱Controller
 */
@RestController
@RequestMapping("/menu")
public class MenuController {

    @Resource
    MenuService menuService;

    /**
     * 添加菜谱
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody Menu menu) {
        boolean result = menuService.save(menu);
        if (result) {
            return Result.success();
        } else {
            return Result.error("-1", "添加失败");
        }
    }

    /**
     * 更新菜谱
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody Menu menu) {
        boolean result = menuService.updateById(menu);
        if (result) {
            return Result.success();
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    /**
     * 删除菜谱
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        boolean result = menuService.removeById(id);
        if (result) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 分页查询菜谱
     */
    @GetMapping("/find")
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search) {
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (!"".equals(search)) {
            queryWrapper.like("dish_name", search);
        }
        Page<Menu> page = menuService.page(new Page<>(pageNum, pageSize), queryWrapper);
        if (page != null) {
            return Result.success(page);
        } else {
            return Result.error("-1", "查询失败");
        }
    }

    /**
     * 获取所有菜谱
     */
    @GetMapping("/all")
    public Result<?> getAllMenus() {
        List<Menu> list = menuService.list();
        if (list != null) {
            return Result.success(list);
        } else {
            return Result.error("-1", "查询失败");
        }
    }

    /**
     * 随机获取菜谱（用于随机菜谱功能）
     * 规则1个汤2-3个荤菜，1-2个素菜，排除主食
     */
    @GetMapping("/random")
    public Result<?> getRandomMenu(@RequestParam(defaultValue = "5") Integer count) {
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        
        // 按分类获取菜品，排除主食
        queryWrapper.eq("category", "汤品");
        List<Menu> soups = menuService.list(queryWrapper);
        
        queryWrapper.clear();
        queryWrapper.eq("category", "荤菜");
        List<Menu> meats = menuService.list(queryWrapper);
        
        queryWrapper.clear();
        queryWrapper.eq("category", "素菜");
        List<Menu> vegetables = menuService.list(queryWrapper);
        
        List<Menu> randomMenus = new java.util.ArrayList<>();
        Random random = new Random();
        
        // 添加1个汤
        if (!soups.isEmpty()) {
            randomMenus.add(soups.get(random.nextInt(soups.size())));
        }
        
        // 添加2-3个荤菜
        int meatCount = meats.size() >= 3 ? (2 + random.nextInt(2)) : Math.min(2, meats.size());
        List<Menu> tempMeats = new java.util.ArrayList<>(meats);
        for (int i = 0; i < meatCount && !tempMeats.isEmpty(); i++) {
            int randomIndex = random.nextInt(tempMeats.size());
            randomMenus.add(tempMeats.remove(randomIndex));
        }
        
        // 添加1-2个素菜
        int vegCount = vegetables.size() >= 2 ? (1 + random.nextInt(2)) : Math.min(1, vegetables.size());
        List<Menu> tempVegs = new java.util.ArrayList<>(vegetables);
        for (int i = 0; i < vegCount && !tempVegs.isEmpty(); i++) {
            int randomIndex = random.nextInt(tempVegs.size());
            randomMenus.add(tempVegs.remove(randomIndex));
        }
        
        if (randomMenus.isEmpty()) {
            return Result.error("-1", "暂无可用菜谱数据");
        }
        
        return Result.success(randomMenus);
    }

    /**
     * 根据分类查询菜谱
     */
    @GetMapping("/category/{category}")
    public Result<?> getMenusByCategory(@PathVariable String category) {
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category", category);
        List<Menu> list = menuService.list(queryWrapper);
        if (list != null) {
            return Result.success(list);
        } else {
            return Result.error("-1", "查询失败");
        }
    }
}
