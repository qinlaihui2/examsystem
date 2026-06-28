package com.atguigu.java.gamerank.dao;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Set;

@Repository
public class RankDao {

    @Resource
    private StringRedisTemplate rt;

    /* ==================== 实时榜 ==================== */
    public void updateScore(Long gameId, Long uid, int delta) {
        String key = "game:score:" + gameId;
        rt.opsForZSet().incrementScore(key, uid.toString(), delta);
        // 默认永不过期，若需自动清理可加过期时间
    }

    public Set<ZSetOperations.TypedTuple<String>> top(Long gameId, int n) {
        return rt.opsForZSet()
                 .reverseRangeWithScores("game:score:" + gameId, 0, n - 1);
    }

    public Long rank(Long gameId, Long uid) {
        return rt.opsForZSet().reverseRank("game:score:" + gameId, uid.toString());
    }

    public Double score(Long gameId, Long uid) {
        return rt.opsForZSet().score("game:score:" + gameId, uid.toString());
    }

    /* ==================== 7 天榜 ==================== */
    private String weekKey(Long gameId) {
        // 2025-06-23 是第 26 周
        int week = LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear());
        return "game:week:" + gameId + ":" + week;
    }

    public void updateWeekScore(Long gameId, Long uid, int delta) {
        String key = weekKey(gameId);
        rt.opsForZSet().incrementScore(key, uid.toString(), delta);
        // 设置 7 天过期，自然淘汰
        rt.expire(key, Duration.ofDays(7));
    }

    public Set<ZSetOperations.TypedTuple<String>> weekTop(Long gameId, int n) {
        return rt.opsForZSet().reverseRangeWithScores(weekKey(gameId), 0, n - 1);
    }
}