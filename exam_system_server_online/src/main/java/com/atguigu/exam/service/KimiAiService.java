package com.atguigu.exam.service;


import com.atguigu.exam.entity.Question;
import com.atguigu.exam.vo.AiGenerateRequestVo;

/**
 * Kimi AI服务接口
 * 用于调用Kimi API生成题目
 */
public interface KimiAiService {


    String buildPrompt(AiGenerateRequestVo request);

    /**
     * 封装请求kimi模型方法
     * @param prompt 提示词
     * @return 模型反馈的结果
     */
    String callKimiAI(String prompt) throws InterruptedException;

    /**
     * ai判题,判断简答题的提示词
     * @param question
     * @param userAnswer
     * @param maxScore
     * @return
     */
    String buildGradingPrompt(Question question, String userAnswer, Integer maxScore);


    /**
     * ai判题，判断选择题和判断题的提示词
     * @param totalScore
     * @param maxScore
     * @param questionCount
     * @param correctCount
     * @return
     */
    String buildSummaryPrompt(Integer totalScore, Integer maxScore, Integer questionCount, Integer correctCount);
}