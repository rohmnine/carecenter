  package com.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.CenterRoom;
import com.example.springboot.service.CenterRoomService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/room")
public class CenterRoomController {

    @Resource
    private CenterRoomService centerRoomService;

    /**
     * 添加房间
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody CenterRoom centerRoom) {
        int i = centerRoomService.addNewRoom(centerRoom);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "添加失败");
        }
    }

    /**
     * 更新房间
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody CenterRoom centerRoom) {
        int i = centerRoomService.updateNewRoom(centerRoom);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    /**
     * 删除房间
     */
    @DeleteMapping("/delete/{centerRoomId}")
    public Result<?> delete(@PathVariable String centerRoomId) {
        int i = centerRoomService.deleteRoom(centerRoomId);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 查找房间
     */
    @GetMapping("/find")
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search) {
        Page page = centerRoomService.find(pageNum, pageSize, search);
        if (page != null) {
            return Result.success(page);
        } else {
            return Result.error("-1", "查询失败");
        }
    }

    /**
     * 首页顶部：空房间统计
     */
    @GetMapping("/noFullRoom")
    public Result<?> noFullRoom() {
        int num = centerRoomService.notFullRoom();
        if (num >= 0) {
            return Result.success(num);
        } else {
            return Result.error("-1", "空房间查询失败");
        }
    }

    /**
     * 删除床位学生信息
     */
    @DeleteMapping("/delete/{bedName}/{centerRoomId}/{calCurrentNum}")
    public Result<?> deleteBedInfo(@PathVariable String bedName, @PathVariable String centerRoomId, @PathVariable int calCurrentNum) {
        int i = centerRoomService.deleteBedInfo(bedName, centerRoomId, calCurrentNum);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 房间床位列表
     */
    @GetMapping("/beds/{centerRoomId}")
    public Result<?> listBeds(@PathVariable String centerRoomId) {
        return Result.success(centerRoomService.listBeds(centerRoomId));
    }

    /**
     * 分配床位
     */
    @PutMapping("/beds/assign")
    public Result<?> assignBed(@RequestParam String centerRoomId,
                               @RequestParam Integer bedNo,
                               @RequestParam String studentUsername) {
        int i = centerRoomService.assignBed(centerRoomId, bedNo, studentUsername);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "床位分配失败");
        }
    }

    /**
     * 清空床位
     */
    @PutMapping("/beds/clear")
    public Result<?> clearBed(@RequestParam String centerRoomId,
                              @RequestParam Integer bedNo) {
        int i = centerRoomService.clearBed(centerRoomId, bedNo);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "床位清空失败");
        }
    }

    /**
     * 智能推荐床位
     */
    @PostMapping("/recommendBed")
    public Result<?> recommendBed(@RequestBody Map<String, Object> params) {
        List<Map<String, Object>> list = centerRoomService.recommendBeds(params);
        return Result.success(list);
    }

    /**
     * 床位信息，查询该学生是否已有床位
     */
    @GetMapping("/judgeHadBed/{value}")
    public Result<?> judgeHadBed(@PathVariable String value) {
        CenterRoom centerRoom = centerRoomService.judgeHadBed(value);
        if (centerRoom == null) {
            return Result.success();
        } else {
            return Result.error("-1", "该学生已有床位");
        }
    }

    /**
     * 主页 住宿人数
     */
    @GetMapping("/selectHaveRoomStuNum")
    public Result<?> selectHaveRoomStuNum() {
        Long count = centerRoomService.selectHaveRoomStuNum();
        if (count >= 0) {
            return Result.success(count);
        } else {
            return Result.error("-1", "查询首页住宿人数失败");
        }
    }

    /**
     * 住宿分布人数
     */
    @GetMapping("/getEachBuildingStuNum/{num}")
    public Result<?> getEachBuildingStuNum(@PathVariable int num) {
        ArrayList<Long> arrayList = new ArrayList();
        for (int i = 1; i <= num; i++) {
            Long eachBuildingStuNum = centerRoomService.getEachBuildingStuNum(i);
            arrayList.add(eachBuildingStuNum);
        }

        if (!arrayList.isEmpty()) {
            return Result.success(arrayList);
        } else {
            return Result.error("-1", "获取人数失败");
        }
    }

    /**
     * 学生功能： 我的房间
     */
    @GetMapping("/getMyRoom/{name}")
    public Result<?> getMyRoom(@PathVariable String name) {
        CenterRoom centerRoom = centerRoomService.judgeHadBed(name);
        if (centerRoom != null) {
            return Result.success(centerRoom);
        } else {
            return Result.error("-1", "不存在该生");
        }
    }

    /**
     * 检查房间是否满员
     */
    @GetMapping("/checkRoomState/{centerRoomId}")
    public Result<?> checkRoomState(@PathVariable String centerRoomId) {
        CenterRoom centerRoom = centerRoomService.checkRoomState(centerRoomId);
        if (centerRoom != null) {
            return Result.success(centerRoom);
        } else {
            return Result.error("-1", "该房间人满了");
        }
    }

    /**
     * 检查床位是否已经有人
     */
    @GetMapping("/checkBedState/{centerRoomId}/{bedNum}")
    public Result<?> getMyRoom(@PathVariable String centerRoomId, @PathVariable int bedNum) {
        CenterRoom centerRoom = centerRoomService.checkBedState(centerRoomId, bedNum);
        if (centerRoom != null) {
            return Result.success(centerRoom);
        } else {
            return Result.error("-1", "该床位已有人");
        }
    }

    /**
     * 检查房间是否满员
     */
    @GetMapping("/checkRoomExist/{centerRoomId}")
    public Result<?> checkRoomExist(@PathVariable String centerRoomId) {
        CenterRoom centerRoom = centerRoomService.checkRoomExist(centerRoomId);
        if (centerRoom != null) {
            return Result.success(centerRoom);
        } else {
            return Result.error("-1", "不存在该房间");
        }
    }
}
