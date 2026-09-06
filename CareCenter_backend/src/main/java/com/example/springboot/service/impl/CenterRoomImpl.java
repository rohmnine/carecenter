package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.CenterRoom;
import com.example.springboot.entity.CenterRoomBed;
import com.example.springboot.entity.UserEntity;
import com.example.springboot.mapper.CenterRoomBedMapper;
import com.example.springboot.mapper.CenterRoomMapper;
import com.example.springboot.service.CenterRoomService;
import com.example.springboot.service.UserEntityService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
public class CenterRoomImpl extends ServiceImpl<CenterRoomMapper, CenterRoom> implements CenterRoomService {

    @Resource
    private CenterRoomMapper centerRoomMapper;

    @Resource
    private CenterRoomBedMapper centerRoomBedMapper;

    @Resource
    private UserEntityService userEntityService;

    /**
     * 首页顶部：空床位统计
     */
    @Override
    public int notFullRoom() {
        if (hasRoomBedTable()) {
            try {
                Long count = centerRoomBedMapper.countAllEmptyBeds();
                if (count != null) {
                    return Math.toIntExact(count);
                }
            } catch (Exception ignored) {
            }
        }
        Long legacy = centerRoomMapper.countEmptyBedsLegacy();
        return legacy == null ? 0 : Math.toIntExact(legacy);
    }

    /**
     * 添加房间
     */
    @Override
    public int addNewRoom(CenterRoom centerRoom) {
        int insert = centerRoomMapper.insert(centerRoom);
        if (insert > 0) {
            syncRoomBeds(centerRoom.getCenterRoomId(), centerRoom.getMaxCapacity());
        }
        return insert;
    }

    /**
     * 查找房间
     */
    @Override
    public Page find(Integer pageNum, Integer pageSize, String search) {
        Page page = new Page<>(pageNum, pageSize);
        QueryWrapper<CenterRoom> qw = new QueryWrapper<>();
        // Apply filter only if search term provided, use correct column name
        if (StringUtils.hasLength(search)) {
            qw.like("center_room_id", search);
        }
        Page roomPage = centerRoomMapper.selectPage(page, qw);
        return roomPage;
    }

    /**
     * 更新房间
     */
    @Override
    public int updateNewRoom(CenterRoom centerRoom) {
        int i = centerRoomMapper.updateById(centerRoom);
        if (i > 0) {
            syncRoomBeds(centerRoom.getCenterRoomId(), centerRoom.getMaxCapacity());
        }
        return i;
    }

    /**
     * 删除房间
     */
    @Override
    public int deleteRoom(String centerRoomId) {
        int i = centerRoomMapper.deleteById(centerRoomId);
        if (i > 0 && hasRoomBedTable()) {
            try {
                QueryWrapper<CenterRoomBed> wrapper = new QueryWrapper<>();
                wrapper.eq("center_room_id", centerRoomId);
                centerRoomBedMapper.delete(wrapper);
            } catch (Exception ignored) {
            }
        }
        return i;
    }

    /**
     * 删除床位上的学生信息
     */
    @Override
    public int deleteBedInfo(String bedName, String centerRoomId, int calCurrentNum) {
        Integer bedNo = parseBedNo(bedName);
        if (bedNo == null) {
            return 0;
        }
        if (!hasRoomBedTable()) {
            return 0;
        }

        int clear = clearBedWithOptimisticLock(centerRoomId, bedNo);
        if (clear <= 0) {
            return 0;
        }
        refreshRoomCurrentCapacityLocal(centerRoomId);
        return 1;

    }

    /**
     * 床位信息，查询该学生是否已由床位
     */
    @Override
    public CenterRoom judgeHadBed(String username) {
        if (!StringUtils.hasLength(username)) {
            return null;
        }
        CenterRoomBed bed = null;
        if (hasRoomBedTable()) {
            try {
                bed = centerRoomBedMapper.selectByOccupant(username);
            } catch (Exception ignored) {
                bed = null;
            }
        }
        if (bed != null && StringUtils.hasLength(bed.getCenterRoomId())) {
            return centerRoomMapper.selectById(bed.getCenterRoomId());
        }
        return centerRoomMapper.selectStudentBedRowLegacy(username);
    }

    /**
     * 主页 住宿人数
     */
    @Override
    public Long selectHaveRoomStuNum() {
        if (hasRoomBedTable()) {
            try {
                Long count = centerRoomBedMapper.countAllOccupied();
                if (count != null) {
                    return count;
                }
            } catch (Exception ignored) {
            }
        }
        Long legacy = centerRoomMapper.countOccupiedBedsLegacy();
        return legacy == null ? 0L : legacy;
    }

    /**
     * 获取每个分店学生总人数     */
    @Override
    public Long getEachBuildingStuNum(int centerBuildingId) {
        if (hasRoomBedTable()) {
            try {
                Long count = centerRoomBedMapper.countOccupiedByBuilding(centerBuildingId);
                if (count != null) {
                    return count;
                }
            } catch (Exception ignored) {
            }
        }
        Long legacy = centerRoomMapper.countOccupiedByBuildingLegacy(centerBuildingId);
        return legacy == null ? 0L : legacy;
    }

    /**
     * 检查该房间是否满了
     */
    @Override
    public CenterRoom checkRoomState(String centerRoomId) {
        QueryWrapper<CenterRoom> qw = new QueryWrapper<>();
        qw.eq("center_room_id", centerRoomId);
        qw.apply("current_capacity < max_capacity");
        return centerRoomMapper.selectOne(qw);
    }

    /**
     * 检查该房间是否存在
     */
    @Override
    public CenterRoom checkRoomExist(String centerRoomId) {
        CenterRoom centerRoom = centerRoomMapper.selectById(centerRoomId);
        return centerRoom;
    }


    /**
     * 检查床位是否有空     */
    @Override
    public CenterRoom checkBedState(String centerRoomId, int bedNum) {
        CenterRoom room = centerRoomMapper.selectById(centerRoomId);
        if (room == null) {
            return null;
        }
        if (bedNum <= 0 || bedNum > room.getMaxCapacity()) {
            return null;
        }
        if (!hasRoomBedTable()) {
            return null;
        }
        CenterRoomBed bed = centerRoomBedMapper.selectByRoomAndBedNo(centerRoomId, bedNum);
        if (bed == null) {
            return null;
        }
        return StringUtils.hasLength(bed.getOccupantUsername()) ? null : room;
    }

    @Override
    public List<CenterRoomBed> listBeds(String centerRoomId) {
        if (!StringUtils.hasLength(centerRoomId)) {
            return new ArrayList<>();
        }
        if (hasRoomBedTable()) {
            List<CenterRoomBed> list = centerRoomBedMapper.selectByRoomId(centerRoomId);
            if (list == null || list.isEmpty()) {
                CenterRoom room = centerRoomMapper.selectById(centerRoomId);
                if (room != null) {
                    initBedsFromLegacy(room);
                    list = centerRoomBedMapper.selectByRoomId(centerRoomId);
                }
            }
            fillOccupantNames(list);
            return list == null ? new ArrayList<>() : list;
        }
        CenterRoom room = centerRoomMapper.selectById(centerRoomId);
        if (room == null) {
            return new ArrayList<>();
        }
        List<CenterRoomBed> legacyBeds = new ArrayList<>();
        int max = Math.min(4, Math.max(0, room.getMaxCapacity()));
        for (int i = 1; i <= max; i++) {
            CenterRoomBed bed = new CenterRoomBed();
            bed.setCenterRoomId(centerRoomId);
            bed.setBedNo(i);
            bed.setOccupantUsername(getLegacyBedOccupant(room, i));
            fillOccupantName(bed);
            legacyBeds.add(bed);
        }
        return legacyBeds;
    }

    @Override
    public int assignBed(String centerRoomId, Integer bedNo, String studentUsername) {
        if (!StringUtils.hasLength(centerRoomId) || bedNo == null || !StringUtils.hasLength(studentUsername)) {
            return 0;
        }
        CenterRoom room = centerRoomMapper.selectById(centerRoomId);
        if (room == null) {
            return 0;
        }
        if (bedNo <= 0 || bedNo > room.getMaxCapacity()) {
            return 0;
        }
        if (hasRoomBedTable()) {
            CenterRoomBed bed = centerRoomBedMapper.selectByRoomAndBedNo(centerRoomId, bedNo);
            if (bed == null) {
                return 0;
            }
            if (StringUtils.hasLength(bed.getOccupantUsername()) && !studentUsername.equals(bed.getOccupantUsername())) {
                return 0;
            }
            CenterRoomBed existing = centerRoomBedMapper.selectByOccupant(studentUsername);
            if (existing != null && StringUtils.hasLength(existing.getCenterRoomId())) {
                if (existing.getCenterRoomId().equals(centerRoomId) && bedNo.equals(existing.getBedNo())) {
                    refreshRoomCurrentCapacityLocal(centerRoomId);
                    return 1;
                }
                if (StringUtils.hasLength(bed.getOccupantUsername())) {
                    return 0;
                }
                int released = clearBedWithOptimisticLock(existing.getCenterRoomId(), existing.getBedNo());
                if (released <= 0) {
                    return 0;
                }
                refreshRoomCurrentCapacityLocal(existing.getCenterRoomId());
            }
            int assigned = assignBedWithOptimisticLock(centerRoomId, bedNo, studentUsername);
            if (assigned > 0) {
                refreshRoomCurrentCapacityLocal(centerRoomId);
            }
            return assigned;
        }
        String bedColumn = bedColumnName(bedNo);
        if (bedColumn == null) {
            return 0;
        }
        CenterRoom legacy = centerRoomMapper.selectStudentBedRowLegacy(studentUsername);
        if (legacy != null) {
            String existingColumn = getLegacyBedColumnByStudent(legacy, studentUsername);
            if (legacy.getCenterRoomId().equals(centerRoomId) && bedColumn.equals(existingColumn)) {
                return 1;
            }
            if (existingColumn != null) {
                centerRoomMapper.clearBedLegacy(legacy.getCenterRoomId(), existingColumn);
            }
        }
        return centerRoomMapper.assignBedIfEmpty(centerRoomId, bedColumn, studentUsername);
    }

    @Override
    public int clearBed(String centerRoomId, Integer bedNo) {
        if (!StringUtils.hasLength(centerRoomId) || bedNo == null) {
            return 0;
        }
        if (hasRoomBedTable()) {
            int cleared = clearBedWithOptimisticLock(centerRoomId, bedNo);
            if (cleared > 0) {
                refreshRoomCurrentCapacityLocal(centerRoomId);
            }
            return cleared;
        }
        String bedColumn = bedColumnName(bedNo);
        if (bedColumn == null) {
            return 0;
        }
        return centerRoomMapper.clearBedLegacy(centerRoomId, bedColumn);
    }

    @Override
    public CenterRoomBed findBedByStudentUsername(String studentUsername) {
        if (!StringUtils.hasLength(studentUsername)) {
            return null;
        }
        if (hasRoomBedTable()) {
            try {
                CenterRoomBed bed = centerRoomBedMapper.selectByOccupant(studentUsername);
                if (bed != null && StringUtils.hasLength(bed.getCenterRoomId())) {
                    return bed;
                }
            } catch (Exception ignored) {
            }
        }

        CenterRoom legacyRoom = centerRoomMapper.selectStudentBedRowLegacy(studentUsername);
        if (legacyRoom == null || !StringUtils.hasLength(legacyRoom.getCenterRoomId())) {
            return null;
        }
        CenterRoomBed legacyBed = new CenterRoomBed();
        legacyBed.setCenterRoomId(legacyRoom.getCenterRoomId());
        legacyBed.setOccupantUsername(studentUsername);
        if (studentUsername.equals(legacyRoom.getFirstBed())) {
            legacyBed.setBedNo(1);
            return legacyBed;
        }
        if (studentUsername.equals(legacyRoom.getSecondBed())) {
            legacyBed.setBedNo(2);
            return legacyBed;
        }
        if (studentUsername.equals(legacyRoom.getThirdBed())) {
            legacyBed.setBedNo(3);
            return legacyBed;
        }
        if (studentUsername.equals(legacyRoom.getFourthBed())) {
            legacyBed.setBedNo(4);
            return legacyBed;
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> recommendBeds(Map<String, Object> params) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (params == null) {
            return result;
        }

        String studentUsername = toText(params.get("studentUsername"));
        Integer centerBuildingId = toInt(params.get("centerBuildingId"));
        if (!StringUtils.hasLength(studentUsername) || centerBuildingId == null) {
            return result;
        }

        UserEntity student = userEntityService.getUserInfo(studentUsername);
        String targetGender = toText(params.get("gender"));
        if (!StringUtils.hasLength(targetGender) && student != null) {
            targetGender = student.getGender();
        }
        String scheduleType = toText(params.get("scheduleType"));
        String allergyPreference = toText(params.get("allergyPreference"));
        String dietPreference = toText(params.get("dietPreference"));

        List<CenterRoom> roomList = centerRoomMapper.selectList(new QueryWrapper<>());
        boolean useRoomBed = hasRoomBedTable();
        if (roomList == null || roomList.isEmpty()) {
            return result;
        }

        for (CenterRoom room : roomList) {
            if (room == null || room.getCenterBuildingId() != centerBuildingId) {
                continue;
            }

            int maxBed = Math.max(0, room.getMaxCapacity());
            if (!useRoomBed) {
                maxBed = Math.min(maxBed, 4);
            }
            if (maxBed <= 0) {
                continue;
            }

            Map<Integer, String> bedOccupancy = new LinkedHashMap<>();
            if (useRoomBed) {
                List<CenterRoomBed> beds = centerRoomBedMapper.selectByRoomId(room.getCenterRoomId());
                if (beds == null || beds.isEmpty()) {
                    initBedsFromLegacy(room);
                    beds = centerRoomBedMapper.selectByRoomId(room.getCenterRoomId());
                }
                if (beds != null) {
                    for (CenterRoomBed bed : beds) {
                        if (bed == null || bed.getBedNo() == null) {
                            continue;
                        }
                        if (bed.getBedNo() >= 1 && bed.getBedNo() <= maxBed) {
                            bedOccupancy.put(bed.getBedNo(), bed.getOccupantUsername());
                        }
                    }
                }
                for (int i = 1; i <= maxBed; i++) {
                    bedOccupancy.putIfAbsent(i, null);
                }
            } else {
                for (int i = 1; i <= maxBed; i++) {
                    bedOccupancy.put(i, getLegacyBedOccupant(room, i));
                }
            }

            for (Map.Entry<Integer, String> entry : bedOccupancy.entrySet()) {
                Integer bedNo = entry.getKey();
                String occupant = entry.getValue();
                if (bedNo == null || StringUtils.hasLength(occupant)) {
                    continue;
                }

                List<String> roommateUsernames = new ArrayList<>();
                for (Map.Entry<Integer, String> mate : bedOccupancy.entrySet()) {
                    if (mate.getKey().equals(bedNo)) {
                        continue;
                    }
                    if (StringUtils.hasLength(mate.getValue())) {
                        roommateUsernames.add(mate.getValue());
                    }
                }

                List<String> reasons = new ArrayList<>();
                int score = 60;

                boolean genderConflict = false;
                int knownGenderRoommates = 0;
                if (StringUtils.hasLength(targetGender)) {
                    for (String mateUsername : roommateUsernames) {
                        UserEntity mate = userEntityService.getUserInfo(mateUsername);
                        if (mate == null || !StringUtils.hasLength(mate.getGender())) {
                            continue;
                        }
                        knownGenderRoommates++;
                        if (!targetGender.equalsIgnoreCase(mate.getGender())) {
                            genderConflict = true;
                            break;
                        }
                    }
                }
                if (genderConflict) {
                    continue;
                }
                if (StringUtils.hasLength(targetGender) && knownGenderRoommates > 0) {
                    score += 20;
                    reasons.add("与现有室友性别一致");
                }

                if (roommateUsernames.isEmpty()) {
                    score += 8;
                    reasons.add("房间当前安静，便于适应");
                } else {
                    score += 6;
                    reasons.add("已有室友，便于融入");
                }

                double occupancyRate = room.getMaxCapacity() == 0
                        ? 0
                        : ((double) roommateUsernames.size() / (double) room.getMaxCapacity());
                if (occupancyRate >= 0.25 && occupancyRate <= 0.75) {
                    score += 6;
                    reasons.add("房间入住率适中");
                }

                if (StringUtils.hasLength(scheduleType)) {
                    reasons.add("已记录作息偏好：" + scheduleType);
                    score += 2;
                }
                if (StringUtils.hasLength(allergyPreference)) {
                    reasons.add("已记录过敏偏好：" + allergyPreference);
                    score += 2;
                }
                if (StringUtils.hasLength(dietPreference)) {
                    reasons.add("已记录饮食偏好：" + dietPreference);
                    score += 2;
                }

                if (!StringUtils.hasLength(targetGender)) {
                    reasons.add("未提供性别，按空床位优先推荐");
                }

                Map<String, Object> item = new HashMap<>();
                item.put("centerRoomId", room.getCenterRoomId());
                item.put("centerBuildingId", room.getCenterBuildingId());
                item.put("floorNum", room.getFloorNum());
                item.put("bedNum", bedNo);
                item.put("score", score);
                item.put("reasons", reasons);
                result.add(item);
            }
        }

        result.sort(Comparator.comparingInt(o -> -toIntOrZero(o.get("score"))));
        if (result.size() > 20) {
            return new ArrayList<>(result.subList(0, 20));
        }
        return result;
    }

    private boolean hasRoomBedTable() {
        try {
            Long exists = centerRoomMapper.existsRoomBedTable();
            return exists != null && exists > 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void fillOccupantNames(List<CenterRoomBed> beds) {
        if (beds == null || beds.isEmpty()) {
            return;
        }
        for (CenterRoomBed bed : beds) {
            fillOccupantName(bed);
        }
    }

    private void fillOccupantName(CenterRoomBed bed) {
        if (bed == null || !StringUtils.hasLength(bed.getOccupantUsername())) {
            return;
        }
        UserEntity user = userEntityService.getUserInfo(bed.getOccupantUsername());
        if (user != null && StringUtils.hasLength(user.getName())) {
            bed.setOccupantName(user.getName());
        }
    }

    private void syncRoomBeds(String centerRoomId, Integer maxCapacity) {
        if (!StringUtils.hasLength(centerRoomId) || maxCapacity == null) {
            return;
        }
        if (!hasRoomBedTable()) {
            return;
        }
        int max = Math.max(0, maxCapacity);
        if (max == 0) {
            return;
        }
        try {
            List<CenterRoomBed> existing = centerRoomBedMapper.selectByRoomId(centerRoomId);
            boolean[] found = new boolean[max + 1];
            if (existing != null) {
                for (CenterRoomBed bed : existing) {
                    if (bed == null || bed.getBedNo() == null) {
                        continue;
                    }
                    int bedNo = bed.getBedNo();
                    if (bedNo >= 1 && bedNo <= max) {
                        found[bedNo] = true;
                    }
                }
            }
            for (int i = 1; i <= max; i++) {
                if (!found[i]) {
                    CenterRoomBed newBed = new CenterRoomBed();
                    newBed.setCenterRoomId(centerRoomId);
                    newBed.setBedNo(i);
                    newBed.setOccupantUsername(null);
                    centerRoomBedMapper.insert(newBed);
                }
            }
            centerRoomBedMapper.deleteEmptyBedsAbove(centerRoomId, max);
            refreshRoomCurrentCapacityLocal(centerRoomId);
        } catch (Exception ignored) {
        }
    }

    private void refreshRoomCurrentCapacityLocal(String centerRoomId) {
        if (!StringUtils.hasLength(centerRoomId)) {
            return;
        }
        Long occupied = null;
        if (hasRoomBedTable()) {
            try {
                occupied = centerRoomBedMapper.countOccupiedByRoom(centerRoomId);
            } catch (Exception ignored) {
                occupied = null;
            }
        }
        int occupiedCount = occupied == null ? 0 : occupied.intValue();
        CenterRoom room = centerRoomMapper.selectById(centerRoomId);
        if (room == null) {
            return;
        }
        room.setCurrentCapacity(occupiedCount);
        centerRoomMapper.updateById(room);
    }

    private int assignBedWithOptimisticLock(String centerRoomId, Integer bedNo, String studentUsername) {
        CenterRoomBed bed = centerRoomBedMapper.selectByRoomAndBedNo(centerRoomId, bedNo);
        if (bed == null) {
            return 0;
        }
        if (StringUtils.hasLength(bed.getOccupantUsername()) && !studentUsername.equals(bed.getOccupantUsername())) {
            return 0;
        }
        if (studentUsername.equals(bed.getOccupantUsername())) {
            return 1;
        }

        bed.setOccupantUsername(studentUsername);
        int updated = centerRoomBedMapper.updateById(bed);
        if (updated == 0) {
            throw new RuntimeException("当前网络拥挤，请重试");
        }
        return 1;
    }

    private int clearBedWithOptimisticLock(String centerRoomId, Integer bedNo) {
        CenterRoomBed bed = centerRoomBedMapper.selectByRoomAndBedNo(centerRoomId, bedNo);
        if (bed == null) {
            return 0;
        }
        if (!StringUtils.hasLength(bed.getOccupantUsername())) {
            return 1;
        }
        bed.setOccupantUsername(null);
        return centerRoomBedMapper.updateById(bed) > 0 ? 1 : 0;
    }

    private String bedColumnName(Integer bedNo) {
        if (bedNo == null) {
            return null;
        }
        if (bedNo == 1) return "first_bed";
        if (bedNo == 2) return "second_bed";
        if (bedNo == 3) return "third_bed";
        if (bedNo == 4) return "fourth_bed";
        return null;
    }

    private String getLegacyBedColumnByStudent(CenterRoom room, String studentUsername) {
        if (room == null || !StringUtils.hasLength(studentUsername)) {
            return null;
        }
        if (studentUsername.equals(room.getFirstBed())) return "first_bed";
        if (studentUsername.equals(room.getSecondBed())) return "second_bed";
        if (studentUsername.equals(room.getThirdBed())) return "third_bed";
        if (studentUsername.equals(room.getFourthBed())) return "fourth_bed";
        return null;
    }

    private void initBedsFromLegacy(CenterRoom room) {
        if (room == null || !StringUtils.hasLength(room.getCenterRoomId())) {
            return;
        }
        int max = Math.min(4, Math.max(0, room.getMaxCapacity()));
        if (max == 0) {
            return;
        }
        for (int i = 1; i <= max; i++) {
            CenterRoomBed bed = new CenterRoomBed();
            bed.setCenterRoomId(room.getCenterRoomId());
            bed.setBedNo(i);
            bed.setOccupantUsername(getLegacyBedOccupant(room, i));
            centerRoomBedMapper.insert(bed);
        }
        refreshRoomCurrentCapacityLocal(room.getCenterRoomId());
    }

    private String getLegacyBedOccupant(CenterRoom room, int bedNo) {
        if (room == null) {
            return null;
        }
        if (bedNo == 1) return room.getFirstBed();
        if (bedNo == 2) return room.getSecondBed();
        if (bedNo == 3) return room.getThirdBed();
        if (bedNo == 4) return room.getFourthBed();
        return null;
    }

    private Integer parseBedNo(String bedName) {
        if (!StringUtils.hasLength(bedName)) {
            return null;
        }
        String normalized = bedName.trim().toLowerCase();
        if ("first_bed".equals(normalized)) return 1;
        if ("second_bed".equals(normalized)) return 2;
        if ("third_bed".equals(normalized)) return 3;
        if ("fourth_bed".equals(normalized)) return 4;

        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (Character.isDigit(ch)) {
                digits.append(ch);
            }
        }
        if (digits.length() == 0) {
            return null;
        }
        try {
            return Integer.parseInt(digits.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private String toText(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return StringUtils.hasLength(text) ? text : null;
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        String text = String.valueOf(value).trim();
        if (!StringUtils.hasLength(text)) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (Exception ignored) {
            return null;
        }
    }

    private int toIntOrZero(Object value) {
        Integer parsed = toInt(value);
        return parsed == null ? 0 : parsed;
    }

}

