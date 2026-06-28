package com.atguigu.exam.service.impl;


import com.atguigu.exam.entity.Category;
import com.atguigu.exam.entity.Question;
import com.atguigu.exam.mapper.CategoryMapper;
import com.atguigu.exam.mapper.QuestionMapper;
import com.atguigu.exam.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.cache.CacheManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {


    @Autowired
    private QuestionMapper questionMapper;
    @Override
    public List<Category> findList() {

//        1.查询所有的分类信息集合（单表操作）-》list
//        2.QuestionMapper定义查询方法，category_id进行分组，并且统计每个分类下的题目数
//        list<<Map>
//                map->key->category||key->counut
//        2.题目查询的分类题目数量赋值给分类集合
//        4.返回查询集合
        LambdaQueryWrapper<Category> wrapper=new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        List<Category> categoryList = list(wrapper);//少了count
        //[map{category_id 15,count :6}]
        List<Map<String, Long>> listMap = questionMapper.selectCategoryCount();
        //listMap->map->14:1 15;2
        Map<Long, Long> countMap = listMap.stream().collect(Collectors.toMap(m -> m.get("category_id"), m -> m.get("count")));
        for (Category category : categoryList) {
            Long id = category.getId();
            category.setCount(countMap.getOrDefault(id,0L));
        }

        return categoryList;
    }

    @Override
    public List<Category> findCategoryTreeList() {
        LambdaQueryWrapper<Category> wrapper=new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        List<Category> categoryList = list(wrapper);//少了count
        //[map{category_id 15,count :6}]
        List<Map<String, Long>> listMap = questionMapper.selectCategoryCount();
        //listMap->map->14:1 15;2
        Map<Long, Long> countMap = listMap.stream().collect(Collectors.toMap(m -> m.get("category_id"), m -> m.get("count")));
        for (Category category : categoryList) {
            Long id = category.getId();
            category.setCount(countMap.getOrDefault(id,0L));
        }

        //分类信息进行分组（parent_id)
        Map<Long, List<Category>> longListMap = categoryList.stream().collect(Collectors.groupingBy(Category::getParentId));

        //筛选分类信息
        //获取一级分类
        List<Category> parentList = categoryList.stream().filter(c -> c.getParentId() == 0).collect(Collectors.toList());
        //给一级分类循环获取子分类，并计算count(父分类的count+说以子分类的count之和
        for (Category parentCategory : parentList) {
            List<Category> sonCategories = longListMap.getOrDefault(parentCategory.getId(),new ArrayList<>());
            parentCategory.setChildren(sonCategories);
            //count
            Long sonCount = sonCategories.stream().collect(Collectors.summingLong(Category::getCount));
            parentCategory.setCount(parentCategory.getCount() + sonCount);
        }

        return parentList;
    }

    @Override
    public void addCategory(Category category) {
        //1.判断同一个父类分类下不允许重名
        // parent_id = 传入 and name = 传入
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Category::getParentId, category.getParentId());
        lambdaQueryWrapper.eq(Category::getName,category.getName());
        long count = count(lambdaQueryWrapper);// count 查询存在的数量
        //知识点： 我们可以在自己的service获取自己的mapper -> CategoryMapper baseMapper = getBaseMapper();
        if (count > 0) {
            Category parent = getById(category.getParentId());
            //不能添加，同一个父类下名称重复了
            throw new RuntimeException("在%s父分类下，已经存在名为：%s的子分类，本次添加失败！".formatted(parent.getName(),category.getName()));
        }
        //2.保存
        save(category);

    }

    @Override
    public void updateCategory(Category category) {
        //1.先校验  同一父分类下！ 可以跟自己的name重复，不能跟其他的子分类name重复！
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Category::getParentId, category.getParentId()); // 同一父分类下！
        lambdaQueryWrapper.ne(Category::getId, category.getId());
        lambdaQueryWrapper.eq(Category::getName, category.getName());
        CategoryMapper categoryMapper = getBaseMapper();
        boolean exists = categoryMapper.exists(lambdaQueryWrapper);
        if (exists) {
            Category parent = getById(category.getParentId());
            //不能添加，同一个父类下名称重复了
            throw new RuntimeException("在%s父分类下，已经存在名为：%s的子分类，本次更新失败！".formatted(parent.getName(),category.getName()));
        }
        //2.再更新
        updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        //1.检查是否一级标题
        Category category = getById(id);
        if (category.getParentId() == 0){
            throw new RuntimeException("不能删除一级标题！");
        }
        //2.检查是否存在关联的题目
        LambdaQueryWrapper<Question> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Question::getCategoryId,id);
        long count = questionMapper.selectCount(lambdaQueryWrapper);
        if (count>0){
            throw new RuntimeException("当前的:%s分类，关联了%s道题目,无法删除！".formatted(category.getName(),count));
        }
        //3.以上不都不满足，删除即可【子关联数据，一并删除】
        removeById(id);
    }
}