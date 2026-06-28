package com.atguigu.java.gamerank.service;

import com.atguigu.java.gamerank.dao.RankDao;
import com.atguigu.java.gamerank.vo.RankVO;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class RankService {

    @Resource
    private RankDao rankDao;

    public void addScore(Long gameId, Long uid, int delta) {
        rankDao.updateScore(gameId, uid, delta);
        rankDao.updateWeekScore(gameId, uid, delta);
    }

    public List<RankVO> realTimeTop(Long gameId) {
        return toVO(rankDao.top(gameId, 100));
    }

    public List<RankVO> weekTop(Long gameId) {
        return toVO(rankDao.weekTop(gameId, 100));
    }

    public RankVO self(Long gameId, Long uid) {
        Long rank = rankDao.rank(gameId, uid);
        Double score = rankDao.score(gameId, uid);

        // 修复类型转换
        Integer finalRank = rank == null ? -1 : rank.intValue() + 1;
        Integer finalScore = score == null ? 0 : score.intValue();

        return new RankVO(uid, finalScore, finalRank);
    }

    private List<RankVO> toVO(Set<ZSetOperations.TypedTuple<String>> set) {
        List<RankVO> list = new ArrayList<>();
        if (set == null || set.isEmpty()) {
            return list;
        }

        int pos = 1;
        for (ZSetOperations.TypedTuple<String> t : set) {
            if (t != null && t.getValue() != null && t.getScore() != null) {
                Long uid = Long.valueOf(t.getValue());
                Integer score = t.getScore().intValue();
                list.add(new RankVO(uid, score, pos++));
            }
        }
        return list;
    }
}