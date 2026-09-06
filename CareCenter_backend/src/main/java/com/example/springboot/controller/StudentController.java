package com.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.AuthUtil;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Student;
import com.example.springboot.entity.User;
import com.example.springboot.entity.UserEntity;
import com.example.springboot.service.CenterRoomService;
import com.example.springboot.service.StudentService;
import com.example.springboot.service.UserEntityService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/stu")
public class StudentController {

    @Resource
    private StudentService studentService;
    
    @Resource
    private UserEntityService userEntityService;

    @Resource
    private CenterRoomService centerRoomService;

    /**
     * 添加学生信息（管理员）
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody UserEntity student, HttpSession session) {
        if (!AuthUtil.isAdmin(session)) {
            return Result.error("403", "无权限");
        }
        student.setUserType("student");
        int i = userEntityService.addUser(student);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "添加失败");
        }

    }

    /**
     * 学生自助注册（公开接口）
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody UserEntity student) {
        // 检查用户名是否已存在
        UserEntity existing = userEntityService.getUserInfo(student.getUsername());
        if (existing != null) {
            return Result.error("-1", "用户名已存在");
        }
        student.setUserType("student");
        int i = userEntityService.addUser(student);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "注册失败");
        }
    }

    /**
     * 更新学生信息
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody UserEntity student, HttpSession session) {
        if (!AuthUtil.isAdmin(session)) {
            return Result.error("403", "无权限");
        }
        int i = userEntityService.updateUser(student);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    /**
     * 删除学生信息
     */
    @DeleteMapping("/delete/{username}")
    public Result<?> delete(@PathVariable String username, HttpSession session) {
        if (!AuthUtil.isAdmin(session)) {
            return Result.error("403", "无权限");
        }
        int i = userEntityService.deleteUser(username);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 查找学生信息
     */
    @GetMapping("/find")
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search,
                              HttpSession session) {
        if (!AuthUtil.isAdmin(session) && !AuthUtil.isParent(session)) {
            return Result.error("403", "无权限");
        }
        Page<UserEntity> page = userEntityService.findByUserType(pageNum, pageSize, "student", search);
        if (page != null) {
            if (page.getRecords() != null) {
                page.getRecords().forEach(student -> {
                    if (student == null || !StringUtils.hasLength(student.getUsername())) {
                        return;
                    }
                    com.example.springboot.entity.CenterRoomBed bed = centerRoomService.findBedByStudentUsername(student.getUsername());
                    if (bed != null) {
                        student.setCenterRoomId(bed.getCenterRoomId());
                        student.setBedNo(bed.getBedNo());
                    } else {
                        student.setCenterRoomId(null);
                        student.setBedNo(null);
                    }
                });
            }
            return Result.success(page);
        } else {
            return Result.error("-1", "查询失败");
        }
    }

    /**
     * 学生登录
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody User user, HttpSession session) {
        UserEntity userEntity = userEntityService.login(user.getUsername(), user.getPassword(), "student");
        if (userEntity != null) {
            //存入session
            session.setAttribute("Identity", "stu");
            session.setAttribute("User", userEntity);
            return Result.success(userEntity);
        } else {
            return Result.error("-1", "用户名或密码错误");
        }
    }

    /**
     * 忘记密码（学生）：通过用户名+姓名重置
     */
    @PostMapping("/resetPassword")
    public Result<?> resetPassword(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String name = params.get("name");
        String newPassword = params.get("newPassword");

        if (!StringUtils.hasLength(username) || !StringUtils.hasLength(name) || !StringUtils.hasLength(newPassword)) {
            return Result.error("-1", "参数不完整");
        }

        int result = userEntityService.resetPasswordByUsernameAndName(username, name, newPassword, "student");
        if (result > 0) {
            return Result.success("密码修改成功");
        }
        return Result.error("-1", "用户名或姓名不匹配");
    }

    /**
     * 主页顶部：学生统计
     */
    @GetMapping("/stuNum")
    public Result<?> stuNum(HttpSession session) {
        if (!AuthUtil.isAdmin(session) && !AuthUtil.isParent(session) && !AuthUtil.isStudent(session)) {
            return Result.error("403", "无权限");
        }
        int num = userEntityService.countByUserType("student");
        return Result.success(num);
    }


    /**
     * 查询所有学生（不分页，用于批量选择）
     */
    @GetMapping("/findAll")
    public Result<?> findAll(@RequestParam(defaultValue = "") String search,
                             HttpSession session) {
        if (!AuthUtil.isAdmin(session)) {
            return Result.error("403", "无权限");
        }
        java.util.List<UserEntity> list = userEntityService.findAllByUserType("student", search);
        return Result.success(list);
    }

    /**
     * 床位信息，查询是否存在该学生
     * 床位信息，查询床位上的学生信息
     */
    @GetMapping("/exist/{value}")
    public Result<?> exist(@PathVariable String value, HttpSession session) {
        if (!AuthUtil.isStudent(session) && !AuthUtil.isAdmin(session) && !AuthUtil.isParent(session)) {
            return Result.error("403", "无权限");
        }
        Student student = studentService.stuInfo(value);
        if (student != null) {
            return Result.success(student);
        } else {
            return Result.error("-1", "不存在该学生");
        }
    }
}

