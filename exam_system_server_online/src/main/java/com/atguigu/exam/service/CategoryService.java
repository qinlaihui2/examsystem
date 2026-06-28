package com.atguigu.exam.service;

import com.atguigu.exam.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CategoryService extends IService<Category> {


    /**
     * 查询分类列表同时查询分类的题目数量
     * @return
     */
    List<Category> findList();

    /**
     * 查询分类树状列表
     * @return
     */
    List<Category> findCategoryTreeList();

    void addCategory(Category category);

    void updateCategory(Category category);

    void deleteCategory(Long id);
}