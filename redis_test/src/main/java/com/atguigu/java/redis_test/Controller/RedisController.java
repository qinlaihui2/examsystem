package com.atguigu.java.redis_test.Controller;

import com.atguigu.java.redis_test.DAO.RedisDao;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redis")
public class RedisController {

    @Resource
    private RedisDao redisDao;

    @GetMapping("/str")
    public String str() {
        redisDao.setStr("k1", "hello");
        return redisDao.getStr("k1");
    }

    @GetMapping("/bitmap")
    public String bitmap() {
        redisDao.setBit("bm", 100, true);
        return "bm-100=" + redisDao.getBit("bm", 100)
             + ", count=" + redisDao.bitCount("bm");
    }
}