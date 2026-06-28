package com.atguigu.java.gamerank.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RankVO {
    private Long uid;
    private Integer score;
    private Integer rank;

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    // 手动添加全参构造器
    public RankVO(Long uid, Integer score, Integer rank) {
        this.uid = uid;
        this.score = score;
        this.rank = rank;
    }
}