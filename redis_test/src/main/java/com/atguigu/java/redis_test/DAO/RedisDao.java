package com.atguigu.java.redis_test.DAO;

import jakarta.annotation.Resource;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class RedisDao {

    @Resource
    private StringRedisTemplate rt;   // k-v 都是 String

    /* ------------------- 1. String 增删改查 ------------------- */
    public void setStr(String k, String v) {
        rt.opsForValue().set(k, v);
    }
    public String getStr(String k) {
        return rt.opsForValue().get(k);
    }
    public void del(String k) {
        rt.delete(k);
    }

    /* ------------------- 2. Hash 增删改查 ------------------- */
    public void hset(String key, String field, String value) {
        rt.opsForHash().put(key, field, value);
    }
    public String hget(String key, String field) {
        return (String) rt.opsForHash().get(key, field);
    }
    public void hdel(String key, String... fields) {
        rt.opsForHash().delete(key, (Object[]) fields);
    }
    public Map<Object, Object> hgetAll(String key) {
        return rt.opsForHash().entries(key);
    }

    /* ------------------- 3. List 队列 ------------------- */
    public void lpush(String key, String v) {
        rt.opsForList().leftPush(key, v);
    }
    public String rpop(String key) {
        return rt.opsForList().rightPop(key);
    }
    public List<String> lrange(String key, long start, long end) {
        return rt.opsForList().range(key, start, end);
    }

    /* ------------------- 4. Set ------------------- */
    public void sadd(String key, String... members) {
        rt.opsForSet().add(key, members);
    }
    public Set<String> smembers(String key) {
        return rt.opsForSet().members(key);
    }
    public void srem(String key, String... members) {
        rt.opsForSet().remove(key, (Object[]) members);
    }

    /* ------------------- 5. ZSet ------------------- */
    public void zadd(String key, String member, double score) {
        rt.opsForZSet().add(key, member, score);
    }
    public Set<String> zrange(String key, long start, long end) {
        return rt.opsForZSet().range(key, start, end);
    }
    public void zrem(String key, String... members) {
        rt.opsForZSet().remove(key, (Object[]) members);
    }

    /* ------------------- 6. Bitmap ------------------- */
    public void setBit(String key, long offset, boolean value) {
        rt.opsForValue().setBit(key, offset, value);
    }
    public boolean getBit(String key, long offset) {
        return rt.opsForValue().getBit(key, offset);
    }
    public long bitCount(String key) {
        return rt.execute((RedisCallback<Long>) con -> con.bitCount(key.getBytes()));
    }

    /* ------------------- 7. Geo ------------------- */
    public void geoAdd(String key, double lng, double lat, String member) {
        rt.opsForGeo().add(key, new Point(lng, lat), member);
    }
    public List<Point> geoPos(String key, String... members) {
        return rt.opsForGeo().position(key, members);
    }
    public Distance geoDist(String key, String m1, String m2, Metrics unit) {
        return rt.opsForGeo().distance(key, m1, m2, unit);
    }

    /* ------------------- 8. HyperLogLog ------------------- */
    public void pfAdd(String key, String... elements) {
        rt.opsForHyperLogLog().add(key, elements);
    }
    public long pfCount(String key) {
        return rt.opsForHyperLogLog().size(key);
    }
}