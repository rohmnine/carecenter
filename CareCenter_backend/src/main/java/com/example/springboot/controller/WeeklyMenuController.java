package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Menu;
import com.example.springboot.entity.WeeklyMenu;
import com.example.springboot.service.MenuService;
import com.example.springboot.service.WeeklyMenuService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 每周菜谱Controller
 */
@RestController
@RequestMapping("/weeklyMenu")
public class WeeklyMenuController {

    @Resource
    private WeeklyMenuService weeklyMenuService;

    @Resource
    private MenuService menuService;
    /**
     * 添加每周菜谱
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody WeeklyMenu weeklyMenu) {
        boolean result = weeklyMenuService.save(weeklyMenu);
        if (result) {
            return Result.success();
        } else {
            return Result.error("-1", "添加失败");
        }
    }

    /**
     * 批量添加每周菜谱
     */
    @PostMapping("/batchAdd")
    public Result<?> batchAdd(@RequestBody List<WeeklyMenu> weeklyMenuList) {
        if (weeklyMenuList == null || weeklyMenuList.isEmpty()) {
            return Result.error("-1", "菜单不能为空");
        }

        QueryWrapper<Menu> riceQuery = new QueryWrapper<>();
        riceQuery.eq("dish_name", "米饭").eq("category", "主食").last("limit 1");
        Menu riceMenu = menuService.getOne(riceQuery);

        if (riceMenu != null && riceMenu.getId() != null) {
            Set<String> existingRiceKeys = new HashSet<>();
            Set<String> mealSlotKeys = new HashSet<>();

            for (WeeklyMenu item : weeklyMenuList) {
                if (item == null) {
                    continue;
                }
                String slotKey = buildMealSlotKey(item);
                mealSlotKeys.add(slotKey);

                if (riceMenu.getId().equals(item.getMenuId())) {
                    existingRiceKeys.add(slotKey);
                }
            }

            List<WeeklyMenu> riceItems = new ArrayList<>();
            for (String slotKey : mealSlotKeys) {
                if (existingRiceKeys.contains(slotKey)) {
                    continue;
                }

                String[] parts = slotKey.split("\\|", -1);
                if (parts.length != 4) {
                    continue;
                }

                WeeklyMenu riceItem = new WeeklyMenu();
                riceItem.setWeekStartDate(parseDate(parts[0]));
                riceItem.setWeekEndDate(parseDate(parts[1]));
                riceItem.setWeekDay(parts[2]);
                riceItem.setMealType(parts[3]);
                riceItem.setMenuId(riceMenu.getId());
                riceItems.add(riceItem);
            }

            weeklyMenuList.addAll(riceItems);
        }

        boolean result = weeklyMenuService.saveBatch(weeklyMenuList);
        if (result) {
            return Result.success();
        } else {
            return Result.error("-1", "批量添加失败");
        }
    }

    private String buildMealSlotKey(WeeklyMenu item) {
        String weekStartDate = formatDate(item.getWeekStartDate());
        String weekEndDate = formatDate(item.getWeekEndDate());
        String weekDay = item.getWeekDay() == null ? "" : item.getWeekDay();
        String mealType = item.getMealType() == null ? "" : item.getMealType();
        return weekStartDate + "|" + weekEndDate + "|" + weekDay + "|" + mealType;
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 更新每周菜谱
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody WeeklyMenu weeklyMenu) {
        boolean result = weeklyMenuService.updateById(weeklyMenu);
        if (result) {
            return Result.success();
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    /**
     * 删除每周菜谱
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        boolean result = weeklyMenuService.removeById(id);
        if (result) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 根据周范围删除每周菜谱
     */
    @DeleteMapping("/deleteByWeek")
    public Result<?> deleteByWeek(@RequestParam String weekStartDate, @RequestParam String weekEndDate) {
        QueryWrapper<WeeklyMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("week_start_date", weekStartDate);
        queryWrapper.eq("week_end_date", weekEndDate);
        boolean result = weeklyMenuService.remove(queryWrapper);
        if (result) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 分页查询每周菜谱
     */
    @GetMapping("/find")
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        QueryWrapper<WeeklyMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("week_start_date", "week_day", "meal_type");
        Page<WeeklyMenu> page = weeklyMenuService.page(new Page<>(pageNum, pageSize), queryWrapper);
        if (page != null) {
            return Result.success(page);
        } else {
            return Result.error("-1", "查询失败");
        }
    }

    /**
     * 获取当前周的菜谱
     */
    @GetMapping("/currentWeek")
    public Result<?> getCurrentWeekMenu() {
        try {
            // 获取当前周的开始和结束日期
            Calendar calendar = Calendar.getInstance();
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            Date weekStart = calendar.getTime();

            calendar.add(Calendar.DAY_OF_WEEK, 6);
            Date weekEnd = calendar.getTime();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String startDate = sdf.format(weekStart);
            String endDate = sdf.format(weekEnd);

            QueryWrapper<WeeklyMenu> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("week_start_date", startDate);
            queryWrapper.eq("week_end_date", endDate);
            queryWrapper.orderBy(true, true, "week_day", "meal_type");

            List<WeeklyMenu> weeklyMenuList = weeklyMenuService.list(queryWrapper);

            // 获取菜谱详细信息
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (WeeklyMenu wm : weeklyMenuList) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", wm.getId());
                map.put("weekDay", wm.getWeekDay());
                map.put("mealType", wm.getMealType());
                map.put("weekStartDate", wm.getWeekStartDate());
                map.put("weekEndDate", wm.getWeekEndDate());

                Menu menu = menuService.getById(wm.getMenuId());
                if (menu != null) {
                    map.put("menu", menu);
                }
                resultList.add(map);
            }

            return Result.success(resultList);
        } catch (Exception e) {
            return Result.error("-1", "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据日期范围获取菜谱
     */
    @GetMapping("/byDateRange")
    public Result<?> getMenuByDateRange(@RequestParam String startDate, @RequestParam String endDate) {
        QueryWrapper<WeeklyMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("week_start_date", startDate);
        queryWrapper.eq("week_end_date", endDate);
        queryWrapper.orderBy(true, true, "week_day", "meal_type");

        List<WeeklyMenu> weeklyMenuList = weeklyMenuService.list(queryWrapper);

        // 获取菜谱详细信息
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (WeeklyMenu wm : weeklyMenuList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", wm.getId());
            map.put("menuId", wm.getMenuId());
            map.put("weekDay", wm.getWeekDay());
            map.put("mealType", wm.getMealType());
            map.put("weekStartDate", wm.getWeekStartDate());
            map.put("weekEndDate", wm.getWeekEndDate());

            Menu menu = menuService.getById(wm.getMenuId());
            if (menu != null) {
                map.put("menu", menu);
            }
            resultList.add(map);
        }

        return Result.success(resultList);
    }
}
