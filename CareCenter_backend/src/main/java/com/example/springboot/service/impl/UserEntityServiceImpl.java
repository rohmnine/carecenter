package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.UserEntity;
import com.example.springboot.mapper.UserEntityMapper;
import com.example.springboot.service.UserEntityService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserEntityServiceImpl extends ServiceImpl<UserEntityMapper, UserEntity> implements UserEntityService {

    @Resource
    private UserEntityMapper userEntityMapper;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserEntity login(String username, String password, String userType) {
        QueryWrapper<UserEntity> qw = new QueryWrapper<>();
        qw.eq("user_type", userType)
          .eq("username", username);

        java.util.List<UserEntity> users = userEntityMapper.selectList(qw);
        if (users == null || users.isEmpty()) {
            return null;
        }

        for (UserEntity user : users) {
            String storedPassword = user.getPassword();
            if (!StringUtils.hasLength(storedPassword)) {
                continue;
            }
            // 兼容历史明文密码，同时支持新加密密码
            if (passwordEncoder.matches(password, storedPassword) || storedPassword.equals(password)) {
                // 若为历史明文，登录成功后自动升级为 BCrypt
                if (!storedPassword.startsWith("$2a$") && !storedPassword.startsWith("$2b$") && !storedPassword.startsWith("$2y$")) {
                    UpdateWrapper<UserEntity> uw = new UpdateWrapper<>();
                    uw.eq("username", user.getUsername())
                      .set("password", passwordEncoder.encode(password));
                    userEntityMapper.update(null, uw);
                }
                if ("parent".equals(user.getUserType())) {
                    user.setStudentUsernames(parseStudentUsernames(user.getStudentUsername()));
                }
                return user;
            }
        }
        return null;
    }

    @Override
    public Page<UserEntity> findByUserType(Integer pageNum, Integer pageSize, String userType, String search) {
        Page<UserEntity> page = new Page<>(pageNum, pageSize);
        QueryWrapper<UserEntity> qw = new QueryWrapper<>();
        qw.eq("user_type", userType);
        if (search != null && !search.isEmpty()) {
            qw.and(w -> w.like("name", search).or().like("username", search));
        }
        Page<UserEntity> result = userEntityMapper.selectPage(page, qw);
        if ("parent".equals(userType) && result != null && result.getRecords() != null) {
            result.getRecords().forEach(parent -> parent.setStudentUsernames(parseStudentUsernames(parent.getStudentUsername())));
        }
        return result;
    }

    @Override
    public int addUser(UserEntity user) {
        if (user != null && StringUtils.hasLength(user.getPassword())) {
            String raw = user.getPassword();
            if (!raw.startsWith("$2a$") && !raw.startsWith("$2b$") && !raw.startsWith("$2y$")) {
                user.setPassword(passwordEncoder.encode(raw));
            }
        }
        if (user != null && "parent".equals(user.getUserType())) {
            if (user.getStudentUsernames() != null) {
                user.setStudentUsername(joinStudentUsernames(normalizeStudentUsernames(user.getStudentUsernames())));
            } else if (StringUtils.hasLength(user.getStudentUsername())) {
                user.setStudentUsername(joinStudentUsernames(parseStudentUsernames(user.getStudentUsername())));
            }
        }
        return userEntityMapper.insert(user);
    }

    @Override
    public int updateUser(UserEntity user) {
        if (user != null && StringUtils.hasLength(user.getPassword())) {
            String raw = user.getPassword();
            if (!raw.startsWith("$2a$") && !raw.startsWith("$2b$") && !raw.startsWith("$2y$")) {
                user.setPassword(passwordEncoder.encode(raw));
            }
        }
        if (user != null && "parent".equals(user.getUserType())) {
            if (user.getStudentUsernames() != null) {
                user.setStudentUsername(joinStudentUsernames(normalizeStudentUsernames(user.getStudentUsernames())));
            } else if (StringUtils.hasLength(user.getStudentUsername())) {
                user.setStudentUsername(joinStudentUsernames(parseStudentUsernames(user.getStudentUsername())));
            }
        }
        return userEntityMapper.updateById(user);
    }

    @Override
    public int deleteUser(String username) {
        return userEntityMapper.deleteById(username);
    }

    @Override
    public int countByUserType(String userType) {
        QueryWrapper<UserEntity> qw = new QueryWrapper<>();
        qw.eq("user_type", userType);
        return Math.toIntExact(userEntityMapper.selectCount(qw));
    }

    @Override
    public UserEntity getUserInfo(String username) {
        UserEntity user = userEntityMapper.selectById(username);
        if (user != null && "parent".equals(user.getUserType())) {
            user.setStudentUsernames(parseStudentUsernames(user.getStudentUsername()));
        }
        return user;
    }

    @Override
    public int resetPasswordByUsernameAndName(String username, String name, String newPassword, String userType) {
        if (!StringUtils.hasLength(username) || !StringUtils.hasLength(name) || !StringUtils.hasLength(newPassword) || !StringUtils.hasLength(userType)) {
            return 0;
        }

        QueryWrapper<UserEntity> qw = new QueryWrapper<>();
        qw.eq("username", username)
          .eq("name", name)
          .eq("user_type", userType);
        UserEntity user = userEntityMapper.selectOne(qw);
        if (user == null) {
            return 0;
        }

        UpdateWrapper<UserEntity> uw = new UpdateWrapper<>();
        uw.eq("username", username)
          .eq("name", name)
          .eq("user_type", userType)
          .set("password", passwordEncoder.encode(newPassword));
        return userEntityMapper.update(null, uw);
    }

    @Override
    public int updateStudentBoardingType(String username, String boardingType) {
        UpdateWrapper<UserEntity> uw = new UpdateWrapper<>();
        uw.eq("username", username)
          .eq("user_type", "student")
          .set("boarding_type", boardingType);
        return userEntityMapper.update(null, uw);
    }

    @Override
    public UserEntity getParentByStudentUsername(String studentUsername) {
        List<UserEntity> parents = getParentsByStudentUsername(studentUsername);
        return parents.isEmpty() ? null : parents.get(0);
    }

    @Override
    public List<UserEntity> getParentsByStudentUsername(String studentUsername) {
        if (!StringUtils.hasLength(studentUsername)) {
            return new ArrayList<>();
        }
        QueryWrapper<UserEntity> qw = new QueryWrapper<>();
        qw.eq("user_type", "parent")
          .and(w -> w.eq("student_username", studentUsername)
                     .or().like("student_username", studentUsername + ",%")
                     .or().like("student_username", "%," + studentUsername + ",%")
                     .or().like("student_username", "%," + studentUsername));

        // Windows 输入法可能替换成中文逗号，做一层兜底
        List<UserEntity> coarse = userEntityMapper.selectList(qw);
        return coarse.stream()
                .filter(p -> parseStudentUsernames(p.getStudentUsername()).contains(studentUsername))
                .peek(p -> p.setStudentUsernames(parseStudentUsernames(p.getStudentUsername())))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getBoundStudentUsernames(String parentUsername) {
        UserEntity parent = userEntityMapper.selectById(parentUsername);
        if (parent == null || !"parent".equals(parent.getUserType())) {
            return new ArrayList<>();
        }
        return parseStudentUsernames(parent.getStudentUsername());
    }

    @Override
    public List<UserEntity> findAllByUserType(String userType, String search) {
        QueryWrapper<UserEntity> qw = new QueryWrapper<>();
        qw.eq("user_type", userType);
        if (search != null && !search.isEmpty()) {
            qw.and(w -> w.like("name", search).or().like("username", search));
        }
        return userEntityMapper.selectList(qw);
    }

    @Override
    public int bindStudents(String parentUsername, List<String> studentUsernames) {
        UserEntity parent = userEntityMapper.selectById(parentUsername);
        if (parent == null || !"parent".equals(parent.getUserType())) {
            return 0;
        }

        List<String> normalized = normalizeStudentUsernames(studentUsernames);
        UpdateWrapper<UserEntity> uw = new UpdateWrapper<>();
        uw.eq("username", parentUsername)
          .eq("user_type", "parent")
          .set("student_username", joinStudentUsernames(normalized));
        return userEntityMapper.update(null, uw);
    }

    private List<String> parseStudentUsernames(String raw) {
        if (!StringUtils.hasLength(raw)) {
            return new ArrayList<>();
        }
        return Arrays.stream(raw.replace('，', ',').split(","))
                .map(String::trim)
                .filter(StringUtils::hasLength)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> normalizeStudentUsernames(List<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> unique = new LinkedHashSet<>();
        for (String username : usernames) {
            if (!StringUtils.hasLength(username)) {
                continue;
            }
            unique.add(username.trim());
        }
        return new ArrayList<>(unique);
    }

    private String joinStudentUsernames(List<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return null;
        }
        return String.join(",", usernames);
    }
}
