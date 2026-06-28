package com.atguigu.java.gamerank.controller;

import com.atguigu.java.gamerank.service.RankService;
import com.atguigu.java.gamerank.vo.RankVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rank")
public class RankController {

    @Resource
    private RankService rankService;

    @PostMapping("/score")
    public void score(@RequestParam Long gameId,
                      @RequestParam Long uid,
                      @RequestParam int delta) {
        rankService.addScore(gameId, uid, delta);
    }

    @GetMapping("/top")
    public List<RankVO> top(@RequestParam Long gameId) {
        return rankService.realTimeTop(gameId);
    }

    @GetMapping("/week")
    public List<RankVO> week(@RequestParam Long gameId) {
        return rankService.weekTop(gameId);
    }

    @GetMapping("/self")
    public RankVO self(@RequestParam Long gameId, @RequestParam Long uid) {
        return rankService.self(gameId, uid);
    }
}