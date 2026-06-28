package com.atguigu.exam.mapper;


import com.atguigu.exam.entity.QuestionChoice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 题目选项
 */
public interface QuestionChoiceMapper extends BaseMapper<QuestionChoice> {

    //定义第二部查询方法
    @Select("select *  from question_choices WHERE question_id=#{questionId} and is_deleted=0 ORDER BY sort ASC;")
    List<QuestionChoice> selectListByQuestionId(Integer questionId);
} 