package com.digital.community.context;

import com.digital.community.entity.User;

public class UserContext {

    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUserId(Long userId) {
        currentUserId.set(userId);
    }

    public static Long getUserId() {
        return currentUserId.get();
    }

    public static void setUser(User user) {
        currentUser.set(user);
    }

    public static User getUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUserId.remove();
        currentUser.remove();
    }
}
