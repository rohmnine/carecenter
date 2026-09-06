package com.example.springboot.common;

import com.example.springboot.entity.UserEntity;

import javax.servlet.http.HttpSession;

public class AuthUtil {

    private AuthUtil() {
    }

    public static String getIdentity(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object identity = session.getAttribute("Identity");
        return identity == null ? null : String.valueOf(identity);
    }

    public static UserEntity getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute("User");
        if (user instanceof UserEntity) {
            return (UserEntity) user;
        }
        return null;
    }

    public static boolean isLoggedIn(HttpSession session) {
        return getCurrentUser(session) != null && getIdentity(session) != null;
    }

    public static boolean hasRole(HttpSession session, String role) {
        String identity = getIdentity(session);
        return identity != null && identity.equals(role);
    }

    public static boolean isAdmin(HttpSession session) {
        return hasRole(session, "admin");
    }

    public static boolean isParent(HttpSession session) {
        return hasRole(session, "parent");
    }

    public static boolean isStudent(HttpSession session) {
        return hasRole(session, "stu");
    }

    public static String getCurrentUsername(HttpSession session) {
        UserEntity user = getCurrentUser(session);
        return user == null ? null : user.getUsername();
    }

    /**
     * - admin 始终允许
     * - parent 仅允许访问自身用户名
     */
    public static boolean isParentSelfOrAdmin(HttpSession session, String parentUsername) {
        if (isAdmin(session)) {
            return true;
        }
        if (!isParent(session)) {
            return false;
        }
        String currentUsername = getCurrentUsername(session);
        return currentUsername != null && currentUsername.equals(parentUsername);
    }
}
