package com.atguigu.exam.mapper;


import com.atguigu.exam.entity.Question;
import com.atguigu.exam.vo.QuestionQueryVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 题目Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础的CRUD操作
 */
public interface QuestionMapper extends BaseMapper<Question> {

    @Select("SELECT  category_id,count(*) count FROM questions where is_deleted=0 GROUP BY category_id")
     List<Map<String,Long>> selectCategoryCount();


    //定义一个查询方法，还想使用mybatisplus分页插件
    //方法规则：返回值必须是IPage 方法名第一个参数一定是IPage[分页数据第几页，每页显示条件]，其他参数
    IPage<Question> selectQuestionByPage(IPage<Question> page, @Param("queryVo") QuestionQueryVo queryVo);

    List<Question> customQueryQuestionListByPaperId(Integer paperId);
} 