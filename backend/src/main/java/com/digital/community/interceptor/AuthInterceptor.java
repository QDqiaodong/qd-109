package com.digital.community.interceptor;

import com.digital.community.context.UserContext;
import com.digital.community.entity.User;
import com.digital.community.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Resource
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdStr = request.getHeader(USER_ID_HEADER);
        if (userIdStr == null || userIdStr.isEmpty() || "null".equalsIgnoreCase(userIdStr) || "undefined".equalsIgnoreCase(userIdStr)) {
            return true;
        }

        try {
            Long userId = Long.parseLong(userIdStr.trim());
            if (userId > 0) {
                User user = userService.getById(userId);
                if (user != null && user.getDeleted() == 0) {
                    UserContext.setUserId(userId);
                    UserContext.setUser(user);
                }
            }
        } catch (NumberFormatException e) {
            // ignore invalid user id
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
