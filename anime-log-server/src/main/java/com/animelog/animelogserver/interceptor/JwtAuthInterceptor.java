package com.animelog.animelogserver.interceptor;

import com.animelog.animelogserver.common.UserContext;
import com.animelog.animelogserver.exception.BusinessException;
import com.animelog.animelogserver.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthInterceptor implements HandlerInterceptor {
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.warn("JWT 鉴权失败, 缺少或非法 Authorization 头, uri={}", request.getRequestURI());
            throw new BusinessException(401, "未登录或登录已失效");
        }
        String token = authorization.substring(7);
        Claims claims = jwtTokenUtil.parseToken(token);
        Long userId = claims.get("userId", Long.class);
        if (userId == null) {
            log.warn("JWT 鉴权失败, token 中缺少 userId, uri={}", request.getRequestURI());
            throw new BusinessException(401, "未登录或登录已失效");
        }
        UserContext.setUserId(userId);
        log.info("JWT 鉴权成功, userId={}, uri={}", userId, request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
