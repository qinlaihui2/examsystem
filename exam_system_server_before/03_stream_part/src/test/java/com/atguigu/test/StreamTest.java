package com.atguigu.test;

import com.atguigu.java.pojo.Category;
import com.atguigu.java.pojo.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StreamTest {

    private List<Question> questions;
    private List<Category> categories;

    @BeforeEach
    public void init(){
        // 准备测试数据
        questions = Arrays.asList(
                new Question(1L, "Java基础题1", "CHOICE", "EASY", 1L, 5),
                new Question(2L, "Java基础题2", "CHOICE", "MEDIUM", 1L, 5),
                new Question(3L, "数据库题1", "JUDGE", "EASY", 2L, 3),
                new Question(4L, "数据库题2", "TEXT", "HARD", 2L, 10),
                new Question(5L, "算法题1", "CHOICE", "HARD", 3L, 8)
        );

        categories = Arrays.asList(
                new Category(1L, "Java基础"),
                new Category(2L, "数据库"),
                new Category(3L, "算法")
        );
    }


    @Test
    public void testFilter(){
        // 案例1：筛选选择题
        // 使用filter筛选出所有类型为"CHOICE"的题目
        List<Question> collect = questions.stream()
                .filter(question -> "CHOICE".equals(question.getType()))
                .collect(Collectors.toList());
        System.out.println("collect = " + collect);

        // 案例2：筛选难度为EASY且分值大于3的题目
        List<Question> collect1 = questions.stream()
                .filter(q -> "EASY".equals(q.getDifficulty()))
                .filter(q -> q.getScore() > 3)
                .collect(Collectors.toList());
        System.out.println("collect1 = " + collect1);
        // 多个filter可以链式调用，相当于AND条件


           // 案例3：复合条件筛选
           //查询选择题和判断题以及分类ID = 1L
           // 在一个filter中使用复杂的逻辑表达式
        List<Question> collect2 = questions.stream()
                .filter(q -> Arrays.asList("CHOICE", "JUDGE").contains(q.getType()))
                .filter(q -> q.getCategoryId() == 1L)
                .collect(Collectors.toList());
        System.out.println("collect2 = " + collect2);
    }

    @Test
    public void testMap() {
           // 案例1：提取题目ID列表
           // 将Question对象流转换为Long类型的ID流
        List<Long> collect = questions.stream()
                .map(Question::getId)
                .collect(Collectors.toList());
        System.out.println("collect = " + collect);
           // 案例2：提取题目标题列表
           // 将Question对象流转换为String类型的标题流
        List<String> collect1 = questions.stream()
                .map(Question::getTitle)
                .collect(Collectors.toList());
        System.out.println("collect1 = " + collect1);
           // 案例3：复杂对象转
           // 将Question对象转换为Map对象（常用于构建前端需要的数据格式）
       questions.stream()
               .map(q->{
                   Map<String ,Object> map=new HashMap<>();
                   map.put("id",q.getId());
                   map.put("title",q.getTitle());
                   return map;
               })
               .forEach(System.out::println);
    }

    @Test
    public void testCollect() {

        // 案例1：转换为List（最常用）
        // 将流中的元素收集到ArrayList中
        List<String> collect = questions.stream()
                .map(Question::getTitle)
                .collect(Collectors.toList());
        System.out.println("collect = " + collect);
        // 案例2：转换为Set（去重）
        // 将流中的元素(题目类型)收集到HashSet中，自动去除重复元素
        Set<String> collect1 = questions.stream()
                .map(Question::getType)
                .collect(Collectors.toSet());
        System.out.println("collect1 = " + collect1);
        // 案例3：转换为Map（ID作为key）
        // 将流中的元素收集为Map，指定key和value的提取方式
        //List<Question> ->Map<题目ID,Question>
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        // 案例4：转换为Map（自定义key-value）
        // 创建ID到标题的映射关系
        Map<Long, String> longStringMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, Question::getTitle));
        // 案例5：按分类ID分组（项目中常用）
        // 根据分类ID将题目分组，结果是Map<分类ID, 该分类下的题目列表>
        Map<Long, List<Question>> listMap = questions.stream()
                .collect(Collectors.groupingBy(Question::getCategoryId));
        System.out.println("listMap = " + listMap);
        // 案例6：按题目类型分组并统计数量
        // 分组的同时进行统计，结果是Map<题目类型, 该类型的题目数量>
        Map<Long, Long> longMap = questions.stream()
                .collect(Collectors.groupingBy(Question::getCategoryId, Collectors.counting()));
        System.out.println("longMap = " + longMap);
        // summarizingInt()：一次性获取总和、平均值、最大值、最小值等统计信息
        IntSummaryStatistics intSummaryStatistics = questions.stream()
                .collect(Collectors.summarizingInt(Question::getScore));
        System.out.println("最大值"+intSummaryStatistics.getMax());
        System.out.println("最小值"+intSummaryStatistics.getMin());
        System.out.println("总和"+intSummaryStatistics.getSum());
        System.out.println("平均数"+intSummaryStatistics.getAverage());
    }
    @Test
    public void testAll() {

        // 项目实战案例1：构建分类题目统计
        // 目标：统计每个分类名称下有多少道题目
        Map<String, Long> longMap = questions.stream()
                .collect(Collectors.groupingBy(
                        q->{
                            return categories.stream()
                                    .filter(c->c.getId()==q.getCategoryId())
                                    .findFirst()
                                    .map(Category::getName)
                                    .orElse("未知分类");
                                   // .get().getName();
                        }
                        , Collectors.counting()));
        System.out.println("longMap = " + longMap);
        // 结果示例：{"Java基础"=2, "数据库"=2, "算法"=1
        // 项目实战案例2：计算各分类平均分值
        // 目标：计算每个分类下题目的平均分值
        Map<Long, Double> map = questions.stream()
                .collect(Collectors.groupingBy(Question::getCategoryId, Collectors.averagingInt(Question::getScore)));
        System.out.println(map);
        // 结果示例：{1=5.0, 2=6.5, 3=8.0}

        // 项目实战案例3：构建试卷统计信息
        // 目标：一次性计算试卷的总分、题目数量和平均分
        //Collectors.teeing() 是 Java 12 引入的一个非常实用的收集器方法，它的核心功能是同时应用两个不同的收集器对同一流进行处理，然后将两个收集器的结果合并成一个最终结果。
        //可以把它理解为 “分流处理”：对同一个流，用两个不同的收集器分别收集数据，最后将两个结果 “合并” 成一个整体。
//        Integer total = questions.stream()
//                .collect(Collectors.summingInt(Question::getScore));
//        Long count = questions.stream()
//                .collect(Collectors.counting());
////        System.out.println("total = " + total);
//        System.out.println("count = " + count);
//        System.out.println("平均分 = "+total/count);
//        // 结果示例：{"totalScore"=31, "questionCount"=5, "avgScore"=6.2}

        Map<String, Object> objectMap = questions.stream()
                .collect(Collectors.teeing(Collectors.summingInt(Question::getScore), Collectors.counting(), (total, count) -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("total", total);
                    result.put("count", count);
                    result.put("avg", total.doubleValue() / count);
                    return result;
                }));
        System.out.println("objectMap = " + objectMap);
        // 项目实战案例4：多条件筛选并转换
        // 目标：筛选高分客观题，并转换为前端需要的VO格式(socre>3 type=text)
        // 结果：包含符合条件题目的VO（map）列表，每个VO包含id、title、type、categoryName字段
        List<Map<String, Object>> collect = questions.stream()
                .filter(q -> q.getScore() > 3)
                .filter(q -> "TEXT".equals(q.getType()))
                .map(q -> {
                    Map<String, Object> map2 = new HashMap<>();
                    map2.put("id", q.getId());
                    map2.put("title", q.getTitle());
                    map2.put("type", q.getType());
                    map2.put("categoryName", categories.stream().
                            filter(c -> c.getId() == q.getCategoryId()).findFirst()
                            .map(Category::getName).orElse("未知分类"));
                    return map2;
                }).collect(Collectors.toList());
        System.out.println("collect = " + collect);
    }

}