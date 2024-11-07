package com.yehuo.githubdatabackend.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;


public class ConfidenceScoreCalculator {


    double confidenceLevel; // 当前置信度值，范围在0到1之间

    @Autowired
    private RedisTemplate redisTemplate;
    // 初始化置信度
//    public ConfidenceScoreCalculator() {
//        redisTemplate.opsForValue().set("confidenceLevel", 0.5);
//        double redisConfidenceLevel = (double) redisTemplate.opsForValue().get("confidenceLevel");
//        if (redisConfidenceLevel == 0) {
//            redisConfidenceLevel = 0.5;
//            redisTemplate.opsForValue().set("confidenceLevel", 0.5);
//        }
//        this.confidenceLevel = redisConfidenceLevel;
//    }

    // 计算百分制分数


//    public double getConfidenceLevel() {
//        return this.confidenceLevel;
//    }

//    public static void main(String[] args) {
//        ConfidenceScoreCalculator calculator = new ConfidenceScoreCalculator(0.5); // 初始置信度50%
//
//        int[] scores = {256, 345, 258, 246, 332, 421, 158, 256}; // 示例分数列表
//        for (int score : scores) {
//            double percentageScore = calculator.calculatePercentageScore(score);
//            System.out.printf("Score: %d -> Percentage Score: %.2f%% (Confidence: %.2f)%n", score, percentageScore, calculator.getConfidenceLevel());
//        }
//    }
}
