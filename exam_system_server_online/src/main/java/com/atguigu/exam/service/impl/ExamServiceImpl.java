package com.atguigu.exam.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.exam.entity.AnswerRecord;
import com.atguigu.exam.entity.ExamRecord;
import com.atguigu.exam.entity.Paper;
import com.atguigu.exam.entity.Question;
import com.atguigu.exam.mapper.AnswerRecordMapper;
import com.atguigu.exam.mapper.ExamRecordMapper;
import com.atguigu.exam.service.AnswerRecordService;
import com.atguigu.exam.service.ExamService;
import com.atguigu.exam.service.KimiAiService;
import com.atguigu.exam.service.PaperService;
import com.atguigu.exam.vo.ExamRankingVO;
import com.atguigu.exam.vo.StartExamVo;
import com.atguigu.exam.vo.SubmitAnswerVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jboss.marshalling.ObjectInputStreamUnmarshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 考试服务实现类
 */
@Service
@Slf4j
public class ExamServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements ExamService {

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private PaperService paperService;

    @Autowired
    private AnswerRecordMapper answerRecordMapper;

    @Autowired
    private AnswerRecordService answerRecordService;

    @Autowired
    private KimiAiService kimiAiService;

    private PersistenceExceptionTranslationAutoConfiguration persistenceExceptionTranslationAutoConfiguration;

    @Override
    public ExamRecord saveExam(StartExamVo startExamVo) {
        //1.校验考生，在当前选择的试卷是否存在正在进行的考试
        LambdaQueryWrapper<ExamRecord> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamRecord::getStudentName,startExamVo.getStudentName());
        //试卷id
        queryWrapper.eq(ExamRecord::getExamId,startExamVo.getPaperId());
        queryWrapper.eq(ExamRecord::getStatus,"进行中");
        ExamRecord examRecord = getOne(queryWrapper);
        if(examRecord != null){
            log.debug("考生:{}在paperId={}的试卷中存在正在考试的记录{}",startExamVo.getStudentName(),startExamVo.getPaperId(),examRecord);
            return examRecord;
        }
        //2.补全考试记录对象的属性（进行中 已完成 已批阅）
        examRecord=new ExamRecord();
        examRecord.setExamId(startExamVo.getPaperId());
        examRecord.setStudentName(startExamVo.getStudentName());
        examRecord.setStatus("进行中");
        examRecord.setStartTime(LocalDateTime.now());
        examRecord.setWindowSwitches(0);
        //3.进行考试记录对象的保存
        save(examRecord);
        //4.返回对应的考试记录
        return examRecord;
    }

    /**
     * 获取考试记录详情
     * @param id
     * @return
     */
    @Override
    public ExamRecord getExamRecordDetail(Integer id) {
        //宏观：获取考试记录，考试记录对应的试卷对象，获取考试记录对应的答题记录集合
        //注意： 答题记录和顺序和考试记录的顺序相同！
        //1. 获取考试记录详情
        ExamRecord examRecord = getById(id);
        if (examRecord == null) {
            throw new RuntimeException("开始考试的记录已经被删除！");
        }
        //2. 获取考试记录对应试卷对象详情 【试卷 题目 选项 和 答案】
        Paper paper = paperService.customPaperDetailById(examRecord.getExamId());
        if (paper == null) {
            throw new RuntimeException("当前考试记录的试卷被删除！获取考试记录详情失败！");
        }
        //3. 获取考试记录对应的答题记录集合
        LambdaQueryWrapper<AnswerRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AnswerRecord::getExamRecordId, id);
        List<AnswerRecord> answerRecords = answerRecordMapper.selectList(lambdaQueryWrapper);
        if (!ObjectUtils.isEmpty(answerRecords)) {
            //[8,2,1,3,7,4] -> 题目id
            List<Long> questionIdList = paper.getQuestions().stream().map(Question::getId).collect(Collectors.toList());
            //[{questionId:1} -> 2 ,{questionId:2} -> 1 ,{questionId:3} -> 3,{questionId:4} ->5,{questionId:7} -> 4,{questionId:8} -> 0]
            answerRecords.sort((o1, o2) -> {
                int x = questionIdList.indexOf(o1.getQuestionId());
                int y = questionIdList.indexOf(o2.getQuestionId());
                return Integer.compare(o1.getQuestionId(), o2.getQuestionId());
            });
        }
        //4. 数据组装即可
        examRecord.setPaper(paper);
        examRecord.setAnswerRecords(answerRecords);
        return examRecord;
    }

    /**
     * 提交考试并进行判卷
     * @param examRecordId
     * @param answers
     */
    @Override
    public void submitExam(Integer examRecordId, List<SubmitAnswerVo> answers) throws InterruptedException {
        //1.1将集合转换成answeRecord对象集合
        if(!ObjectUtils.isEmpty(answers)){
            List<AnswerRecord> answerRecordList = answers.stream().map(vo -> new AnswerRecord(examRecordId, vo.getQuestionId(), vo.getUserAnswer()))
                    .collect(Collectors.toList());
            //1.2进行answerRecord集合的批量保存
            answerRecordService.saveBatch(answerRecordList);
        }
        //1.3修改考试记录对象（状态修改成已完成 2.endtime时间）
        ExamRecord examRecord = getById(examRecordId);
        examRecord.setEndTime(LocalDateTime.now());
        examRecord.setStatus("已完成");
        updateById(examRecord);
        //调用判卷业务的方法
        graderExam(examRecordId);
    }

    @Override
    public ExamRecord graderExam(Integer examRecordId) throws InterruptedException {
        //1.获取考生信息（对应考试答案，正确答案）答题记录合计
        ExamRecord examRecord = getExamRecordDetail(examRecordId);
        Paper paper =examRecord.getPaper();
        //2.校验考试记录对应的试卷是否被删除（正确答案可能不存在）
        if(paper==null){
            //试卷被删除了
            examRecord.setStatus("已批阅");
            examRecord.setAnswers("考试对应试卷被删除，无法判卷");
            examRecord.setScore(0);
            updateById(examRecord);
            log.warn("试卷没有正常判定考试记录被删除");
            return examRecord;
        }
        //3.校验考生提交的考试记录是否为空（为空直接改为0，改成已批阅）
        List<AnswerRecord> answerRecords = examRecord.getAnswerRecords();
        if(ObjectUtils.isEmpty(answerRecords)){
            //没有答题
            examRecord.setStatus("已批阅");
            examRecord.setAnswers("考生没有提交考生记录");
            examRecord.setScore(0);
            updateById(examRecord);
            log.warn("考生提交空卷或者未提交考生考试记录");
            return examRecord;
        }
        //4.声明两个变量，正确题目，以及分数
        int correctCount=0;
        int totalScore=0;
        //5.将试卷question题目集合转换成map（key：questionId value：question对象）方便根据答题记录的questionId快速获取题目对象
        Map<Long, Question> questionMap = paper.getQuestions().stream().collect(Collectors.toMap(Question::getId, question -> question));
        //6.遍历答题记录集合，在内部进行一一判题，同时进行正确题目数量和总分数的累加
        //容错处理，单体错了就是0分
        for (AnswerRecord answerRecord : answerRecords) {
            //6.1获取答题记录以及正确题目
            Question question = questionMap.get(answerRecord.getQuestionId().longValue());
            if(question==null) continue;
            //6.2获取正确答案
            String systemAnswer = question.getAnswer().getAnswer();//正确答案
            String userAnswer = answerRecord.getUserAnswer();//学生答案
            //如果判断题，用户提交的答案ＴＦ->ture false
            if("JUDGE".equals(question.getType())){
                 userAnswer = judgeToTrueOrFalse(userAnswer);
            }
            try{
               //1.非简答题
                if(!"TEXT".equals(question.getType())){
                 //判断题
                    if(userAnswer.equalsIgnoreCase(systemAnswer)){
                        answerRecord.setIsCorrect(1);
                        answerRecord.setScore(question.getPaperScore().intValue());
                    }else{
                        answerRecord.setIsCorrect(0);
                        answerRecord.setScore(0);
                    }
                 //选择题
                }else {
                    //todo :添加ai判卷
                    String prompt = kimiAiService.buildGradingPrompt(question, userAnswer, question.getPaperScore().intValue());
                    String result = kimiAiService.callKimiAI(prompt);
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    //ai给的分数
                    Integer aiScore = jsonObject.getInteger("score");
                    if(aiScore>question.getPaperScore().intValue()){
                        //答案完全正确
                        answerRecord.setScore(question.getPaperScore().intValue());
                        answerRecord.setIsCorrect(1);
                        answerRecord.setAiCorrection(jsonObject.getString("feedback"));
                    } else if (aiScore == 0 ) {
                        answerRecord.setScore(0);
                        answerRecord.setIsCorrect(0);
                        answerRecord.setAiCorrection(jsonObject.getString("reason"));
                        //答案完全步正确
                    }else{
                        answerRecord.setScore(aiScore);
                        answerRecord.setIsCorrect(2);
                        answerRecord.setAiCorrection(jsonObject.getString("reason"));
                        //答案部分正确
                    }
                }
            }catch (Exception e){
                //判断题目错了给0分
                answerRecord.setIsCorrect(0);
                answerRecord.setScore(0);
                answerRecord.setAiCorrection("题目类型错误，无法判卷");
            }
            //进行题目数量的累加和得分累加
            totalScore+=answerRecord.getScore();
            if(answerRecord.getIsCorrect()==1){
                correctCount++;
            }
        }
        //7.修改每一条学生的考试记录
        answerRecordService.updateBatchById(answerRecords);//答题记录的批量更新
        //8.ai调用Kimi模型生成ai点评设置给考试记录对象

        //todo:没有添加ai判卷
        String summaryPrompt = kimiAiService.buildSummaryPrompt(totalScore, paper.getTotalScore().intValue(), paper.getQuestionCount(), correctCount);
        String summary = kimiAiService.callKimiAI(summaryPrompt);
        //9.更新考试记录对象
        examRecord.setScore(totalScore);
        examRecord.setAnswers(summary);
        examRecord.setStatus("已批阅");
        updateById(examRecord);
        //10.返回考试记录对象
        return examRecord;
    }

    @Override
    public void customRemoveById(Integer id) {
        //重要的关联数据校验，有删除失败！
        //判断自身状态，进行中不能删除
        ExamRecord examRecord = getById(id);
        if ("进行中".equals(examRecord.getStatus())){
            throw new RuntimeException("正在考试中，无法直接删除！");
        }
        //删除自身数据，同时删除答题记录
        removeById(id);
        answerRecordService.remove(new LambdaQueryWrapper<AnswerRecord>().eq(AnswerRecord::getExamRecordId,id));
    }

    //转换判断题答案-》truefalse
    private String judgeToTrueOrFalse(String answer){
        String userAnser = answer.toUpperCase();
        switch (answer){
            case "T":
            case "正确":
            case "对":
            case "TRUE":
                return "TRUE";
            case "F":
            case "错误":
            case "错":
            case "不对":
            case "FALSE":
                return "FALSE";
            default:
                return userAnser;
        }
    }


    /**
     * 获取考试排行榜 - 优化版本
     * 使用SQL关联查询，一次性获取所有需要的数据，避免N+1查询问题
     * @param paperId 试卷ID，可选参数，不传则查询所有试卷
     * @param limit 显示数量限制，可选参数，不传则返回所有记录
     * @return 排行榜列表
     */
    @Override
    public List<ExamRankingVO> customGetRanking(Integer paperId, Integer limit) {
        return examRecordMapper.customQueryRanking(paperId,limit);
    }
}