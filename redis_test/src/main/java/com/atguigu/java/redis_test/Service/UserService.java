package com.atguigu.java.redis_test.Service;

import com.atguigu.java.redis_test.DAO.RedisDao;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Resource
    private RedisDao redisDao;

    public void online(Long userId) {
        redisDao.setBit("online:20250515", userId, true);
    }

    public long onlineCount() {
        return redisDao.bitCount("online:20250515");
    }
}