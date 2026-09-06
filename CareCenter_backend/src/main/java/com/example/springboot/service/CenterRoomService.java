package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.CenterRoom;
import com.example.springboot.entity.CenterRoomBed;

import java.util.List;
import java.util.Map;


public interface CenterRoomService extends IService<CenterRoom> {
    //统计没有住满的房间数量
    int notFullRoom();

    //新增房间
    int addNewRoom(CenterRoom centerRoom);

    //查询房间
    Page find(Integer pageNum, Integer pageSize, String search);

    //更新房间信息
    int updateNewRoom(CenterRoom centerRoom);

    //删除房间信息
    int deleteRoom(String centerRoomId);

    //删除床位上的学生信息
    int deleteBedInfo(String bedName, String centerRoomId, int calCurrentNum);

    //床位信息，查询该学生是否已有床位
    CenterRoom judgeHadBed(String username);

    //主页 住宿人数
    Long selectHaveRoomStuNum();

    //获取每个分店学生总人数
    Long getEachBuildingStuNum(int centerBuildingId);

    // 检查该房间是否满了
    CenterRoom checkRoomState(String centerRoomId);

    // 检查房间是否存在
    CenterRoom checkRoomExist(String centerRoomId);

    // 检查床位是否有人
    CenterRoom checkBedState(String centerRoomId, int bedNum);

    // 查询房间床位列表
    List<CenterRoomBed> listBeds(String centerRoomId);

    // 分配床位
    int assignBed(String centerRoomId, Integer bedNo, String studentUsername);

    // 清空床位
    int clearBed(String centerRoomId, Integer bedNo);

    // 根据学生学号查询床位信息
    CenterRoomBed findBedByStudentUsername(String studentUsername);

    // 智能推荐床位
    List<Map<String, Object>> recommendBeds(Map<String, Object> params);
}

