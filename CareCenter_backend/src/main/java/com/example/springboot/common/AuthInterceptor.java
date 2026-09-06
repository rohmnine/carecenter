package com.example.springboot.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (!AuthUtil.isLoggedIn(session)) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, Result.error("401", "请先登录"));
            return false;
        }

        String uri = request.getRequestURI();

        if (PATH_MATCHER.match("/admin/**", uri) && !AuthUtil.isAdmin(session)) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, Result.error("403", "无管理员权限"));
            return false;
        }

        // parent 控制器内的管理员接口
        if (PATH_MATCHER.match("/parent/notifications/unauthorized", uri)
                || PATH_MATCHER.match("/parent/notifications/send", uri)
                || PATH_MATCHER.match("/parent/visitor/all", uri)
                || PATH_MATCHER.match("/parent/visitor/approve", uri)
                || PATH_MATCHER.match("/parent/bed/all", uri)
                || PATH_MATCHER.match("/parent/bed/approve", uri)) {
            if (!AuthUtil.isAdmin(session)) {
                writeJson(response, HttpServletResponse.SC_FORBIDDEN, Result.error("403", "无管理员权限"));
                return false;
            }
        }

        // 学生检查家长绑定：允许学生访问
        if (PATH_MATCHER.match("/parent/existByStudent/**", uri)) {
            if (!AuthUtil.isStudent(session) && !AuthUtil.isAdmin(session) && !AuthUtil.isParent(session)) {
                writeJson(response, HttpServletResponse.SC_FORBIDDEN, Result.error("403", "无权限"));
                return false;
            }
            return true;
        }

        // 普通parent 接口：家长或管理员可访问
        if (PATH_MATCHER.match("/parent/**", uri)
                && !AuthUtil.isParent(session)
                && !AuthUtil.isAdmin(session)) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, Result.error("403", "无家长权限"));
            return false;
        }

        // 学生搜索/查看：家长/管理员可访问（用于绑定学生）
        if (PATH_MATCHER.match("/stu/find", uri)) {
            if (!AuthUtil.isAdmin(session) && !AuthUtil.isParent(session)) {
                writeJson(response, HttpServletResponse.SC_FORBIDDEN, Result.error("403", "无权限"));
                return false;
            }
            return true;
        }
        if (PATH_MATCHER.match("/stu/exist/**", uri)) {
            if (!AuthUtil.isAdmin(session) && !AuthUtil.isParent(session) && !AuthUtil.isStudent(session)) {
                writeJson(response, HttpServletResponse.SC_FORBIDDEN, Result.error("403", "无权限"));
                return false;
            }
            return true;
        }

        // 学生统计（首页）：管理员/学生/家长均可访问
        if (PATH_MATCHER.match("/stu/stuNum", uri)) {
            if (!AuthUtil.isStudent(session)
                    && !AuthUtil.isAdmin(session)
                    && !AuthUtil.isParent(session)) {
                writeJson(response, HttpServletResponse.SC_FORBIDDEN, Result.error("403", "无权限"));
                return false;
            }
            return true;
        }

        // 学生接口：学生或管理员可访问
        if (PATH_MATCHER.match("/stu/**", uri)
                && !AuthUtil.isStudent(session)
                && !AuthUtil.isAdmin(session)) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, Result.error("403", "无学生权限"));
            return false;
        }

        return true;
    }

    private void writeJson(HttpServletResponse response, int status, Result<?> body) throws Exception {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(body));
    }
}
