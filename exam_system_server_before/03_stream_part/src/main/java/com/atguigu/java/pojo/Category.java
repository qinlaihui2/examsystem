package com.atguigu.java.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

// 模拟分类实体
@Data
@AllArgsConstructor
public class Category {
    private Long id;
    private String name;
}