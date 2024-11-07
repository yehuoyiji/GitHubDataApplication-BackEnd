package com.yehuo.githubdatabackend.entity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TokenBucket {
    private final int capacity; // 令牌桶的容量
    private final int refillRate; // 每秒生成的令牌数
    private AtomicInteger tokens; // 当前令牌数量
    private ScheduledExecutorService scheduler;

    public TokenBucket(int capacity, int refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = new AtomicInteger(capacity);

        // 定时任务：每秒添加一定数量的令牌
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.scheduler.scheduleAtFixedRate(this::refillTokens, 0, 1, TimeUnit.SECONDS);
    }

    // 定时为桶添加令牌
    private void refillTokens() {
        if (tokens.get() < capacity) {
            int newTokens = Math.min(capacity - tokens.get(), refillRate);
            tokens.addAndGet(newTokens);
        }
    }

    // 尝试获取令牌，返回是否成功
    public boolean tryConsume() {
        int currentTokens;
        do {
            currentTokens = tokens.get();
            if (currentTokens <= 0) {
                return false; // 无可用令牌，限流
            }
        } while (!tokens.compareAndSet(currentTokens, currentTokens - 1));
        return true;
    }
}
