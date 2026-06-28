package com.atguigu.java.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

// 模拟题目实体
@Data
@AllArgsConstructor
public class Question {
    private Long id;
    private String title;
    private String type; // CHOICE, JUDGE, TEXT
    private String difficulty; // EASY, MEDIUM, HARD
    private Long categoryId;
    private Integer score;
}