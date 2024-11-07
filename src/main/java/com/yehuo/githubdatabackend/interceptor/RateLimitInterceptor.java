package com.yehuo.githubdatabackend.interceptor;

import com.yehuo.githubdatabackend.entity.TokenBucket;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final TokenBucket tokenBucket = new TokenBucket(10, 5); // 容量为10，每秒生成5个令牌

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!tokenBucket.tryConsume()) {
            response.setStatus(429); // 请求过于频繁
            return false; // 拒绝请求
        }
        return true; // 允许请求
    }
}
