package com.atguigu.exam.service;

import com.atguigu.exam.entity.ExamRecord;
import com.atguigu.exam.vo.ExamRankingVO;
import com.atguigu.exam.vo.StartExamVo;
import com.atguigu.exam.vo.SubmitAnswerVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 考试服务接口
 */
public interface ExamService extends IService<ExamRecord> {

    /**创建和保存考试记录业务
     *
     * @param startExamVo
     * @return
     */
    ExamRecord saveExam(StartExamVo startExamVo);

    /**
     * 获取考试详情业务
     * @param id
     * @return
     */
    ExamRecord getExamRecordDetail(Integer id);

    /**
     * 提交考试记录并进行判卷
     * @param examRecordId
     * @param answers
     */
    void submitExam(Integer examRecordId, List<SubmitAnswerVo> answers) throws InterruptedException;

    /**
     * ai之内判卷
     * @param examRecordId
     * @return
     */
    ExamRecord graderExam(Integer examRecordId) throws InterruptedException;

    void customRemoveById(Integer id);

    List<ExamRankingVO> customGetRanking(Integer paperId, Integer limit);
}

 