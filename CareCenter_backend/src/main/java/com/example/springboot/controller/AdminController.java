package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.User;
import com.example.springboot.entity.UserEntity;
import com.example.springboot.service.UserEntityService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private UserEntityService userEntityService;

    /**
     * 管理员登录    */
    @PostMapping("/login")
    public Result<?> login(@RequestBody User user, HttpSession session) {
        UserEntity userEntity = userEntityService.login(user.getUsername(), user.getPassword(), "admin");
        if (userEntity != null) {
            // 存入session
            session.setAttribute("Identity", "admin");
            session.setAttribute("User", userEntity);
            return Result.success(userEntity);
        } else {
            return Result.error("-1", "用户名或密码错误");
        }
    }

    /**
     * 忘记密码（管理员）：通过用户名+姓名重置
     */
    @PostMapping("/resetPassword")
    public Result<?> resetPassword(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String name = params.get("name");
        String newPassword = params.get("newPassword");

        if (!StringUtils.hasLength(username) || !StringUtils.hasLength(name) || !StringUtils.hasLength(newPassword)) {
            return Result.error("-1", "参数不完整");
        }

        int result = userEntityService.resetPasswordByUsernameAndName(username, name, newPassword, "admin");
        if (result > 0) {
            return Result.success("密码修改成功");
        }
        return Result.error("-1", "用户名或姓名不匹配");
    }

    /**
     * 管理员信息更改    */
    @PutMapping("/update")
    public Result<?> update(@RequestBody UserEntity admin) {
        int i = userEntityService.updateUser(admin);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "更新失败");
        }
    }
}

