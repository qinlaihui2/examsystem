package com.atguigu.exam.service.impl;


import com.atguigu.exam.entity.ExamRecord;
import com.atguigu.exam.entity.Paper;
import com.atguigu.exam.entity.PaperQuestion;
import com.atguigu.exam.entity.Question;
import com.atguigu.exam.mapper.ExamRecordMapper;
import com.atguigu.exam.mapper.PaperMapper;
import com.atguigu.exam.mapper.QuestionMapper;
import com.atguigu.exam.service.PaperQuestionService;
import com.atguigu.exam.service.PaperService;
import com.atguigu.exam.vo.AiPaperVo;
import com.atguigu.exam.vo.PaperVo;
import com.atguigu.exam.vo.RuleVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * 试卷服务实现类
 */
@Slf4j
@Transactional
@Service
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {

    @Autowired
    private PaperQuestionService paperQuestionService;


    @Autowired
    private ExamRecordMapper examRecordMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Override
    public Paper create(PaperVo paperVo) {
        //1.先完善试卷基本信息（默认状态，总分数，总题目数
        Paper paper=new Paper();
        BeanUtils.copyProperties(paperVo,paper);
        /**
         * Beanutils的拷贝属性方法就等于把Paper类和PaperVo两个类里面同名的方法拷贝过来了
         * 比如     paper.setDescription(paperVo.getDescription();等等
         */
        paper.setStatus("DRAFT");//设置试卷默认状态
        //检查是否传入题目信息
        if(ObjectUtils.isEmpty(paperVo.getQuestions())){
            paper.setTotalScore(BigDecimal.ZERO);//总分为0
            paper.setQuestionCount(0);//0个题目
            save(paper);//保存试卷信息对象
            log.warn("当前试卷{}没有题目，智能用于试卷编辑",paper);
            return paper;
        }
        //有题目
        paper.setQuestionCount(paperVo.getQuestions().size());//设置试卷总题目数
        //{题目id，题目真实分数}
        //设置试卷总分
        //总分数map->value->value进行累加（BigDecimal.add()）
        Optional<BigDecimal> totalScore = paperVo.getQuestions().values().stream().reduce(BigDecimal::add);
        paper.setTotalScore(totalScore.get());
        //2.保存试卷信息对象（试卷的主体）
        save(paper);
        log.debug("当前试卷勾选了题目，正常今昔计算和保存！试卷对象信息为{}",paper);
        //3.判断试卷是否携带题目集合，如果携带了就进行后续试卷题目关系表处理
        //4.题目集合的map->试卷题目关系表
        List<PaperQuestion> paperQuestionList = paperVo.getQuestions().entrySet().stream()
                .map(entry -> new PaperQuestion(paper.getId().intValue(), Long.valueOf(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList());
        //5.试卷题目中间表的业务批量插入和方法批量插入
        paperQuestionService.saveBatch(paperQuestionList);
        //6.返回试卷对象
        return paper;
    }

    /**
     * ai智能出试卷
     * @param aiPaperVo
     * @return
     */
    @Override
    public Paper aiCreatePaper(AiPaperVo aiPaperVo) {
        //1. 试卷的基本属性赋值并保存 （名字 描述 时间 状态）
        Paper paper = new Paper();
        BeanUtils.copyProperties(aiPaperVo,paper);
        paper.setStatus("DRAFT");
        save(paper);

        //2. 组卷规则下的试题选择和中间表的保存
        int questionCount = 0;
        BigDecimal totalScore = BigDecimal.ZERO;
        for (RuleVo rule : aiPaperVo.getRules()) {
            //步骤1：校验规则下的题目数量 = 0 跳过
            if (rule.getCount() == 0){
                log.warn("在：{}类型下，不需要出题！",rule.getType().name());
                continue;
            }
            //步骤2：查询当前规则下的所有题目集合 【type categoryIds】
            LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Question::getType, rule.getType().name());
            queryWrapper.in(!ObjectUtils.isEmpty(rule.getCategoryIds()),Question::getCategoryId, rule.getCategoryIds());
            List<Question> allQuestionList = questionMapper.selectList(queryWrapper);

            //步骤3：校验查询的题目集合，集合为空！跳过本次！
            if (ObjectUtils.isEmpty(allQuestionList)){
                log.warn("在：{}类型下，我们指定的分类：{},没有查询到题目信息！",rule.getType().name(),rule.getCategoryIds());
                continue;
            }

            //步骤4：判断下是否有规则下count数量！ 没有要全部了
            int realNumbers = Math.min(rule.getCount(), allQuestionList.size());

            //步骤5：本次规则下添加的数量和分数累加
            questionCount += realNumbers;
            totalScore =  totalScore.add(BigDecimal.valueOf((long) realNumbers * rule.getScore()));

            //步骤6：先打乱数据，再截取需要题目数量
            Collections.shuffle(allQuestionList);
            List<Question> realQuestionList = allQuestionList.subList(0, realNumbers);

            //步骤7：转成中间表并进行保存
            List<PaperQuestion> paperQuestionList = realQuestionList.stream().map(question ->
                    new PaperQuestion(paper.getId().intValue(), question.getId(), BigDecimal.valueOf(rule.getScore()))
            ).collect(Collectors.toList());
            paperQuestionService.saveBatch(paperQuestionList);
        }
        //3. 修改试卷信息（总题数，总分数）
        paper.setQuestionCount(questionCount);
        paper.setTotalScore(totalScore);
        updateById(paper);
        //4. 返回试卷对象
        return paper;
    }

    /**
     * 更新试卷信息
     * @param id
     * @param paperVo
     * @return
     */
    @Override
    public Paper updatePpaer(Integer id, PaperVo paperVo) {

        Paper paper = getById(id);
//        校验：
//        1.处于发布状态的试卷不可以更新
        if("PUBLISHED".equals(paper.getStatus())){
            throw new RuntimeException("处于发布状态的试卷不可以更新！");
        }
//        2.更新后的试卷不可以重名
        LambdaQueryWrapper<Paper> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(Paper::getId,id);
        queryWrapper.eq(Paper::getName,paperVo.getName());
        long count = count(queryWrapper);
        if(count>0){
            throw new RuntimeException("更新后的试卷名称：s%与其他试卷名称相同，更新失败".formatted(paperVo.getName()));
        }
        //新的覆盖原来的属性7
        BeanUtils.copyProperties(paperVo,paper);
        //总题目数和总分数要更新
        paper.setQuestionCount(paperVo.getQuestions().size());
        Optional<BigDecimal> totalScore = paperVo.getQuestions().values().stream().reduce(BigDecimal::add);
        paper.setTotalScore(totalScore.get());
        updateById(paper);

//        1.更新涉及的表包括试卷表和题目试卷关联表
//        2.试卷表是一对一的关系，直接更新就行了
//        3.试卷题目表是对多的关系，所以是先删除原来的题目然后插入新的题目
        //先删除再插入
        LambdaQueryWrapper<PaperQuestion> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(PaperQuestion::getPaperId,id);
        paperQuestionService.remove(queryWrapper1);

        List<PaperQuestion> paperQuestionList = paperVo.getQuestions().entrySet().stream()
                .map(entry -> new PaperQuestion(paper.getId().intValue(), Long.valueOf(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList());
        //5.试卷题目中间表的业务批量插入和方法批量插入
        paperQuestionService.saveBatch(paperQuestionList);
        return paper;
    }

    @Override
    public void customRemoveId(Integer id) {
        //1.不是发布状态
        Paper paper = getById(id);
        if (paper == null || "PUBLISHED".equals(paper.getStatus())){
            throw new RuntimeException("发布状态的试卷不能删除！");
        }
        //2.不能有关联的考试记录
        LambdaQueryWrapper<ExamRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ExamRecord::getExamId,id);
        Long count = examRecordMapper.selectCount(lambdaQueryWrapper);
        if (count > 0){
            throw new RuntimeException("当前试卷：%s 下面有关联 %s条考试记录！无法直接删除！".formatted(id,count));
        }
        //3.删除自身表
        removeById(Long.valueOf(id));
        //4.删除中间表
        paperQuestionService.remove(new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId,id));
    }

    @Override
    public Paper customPaperDetailById(Integer id) {
        //1. 单表java代码进行paper查询
        Paper paper = getById(id);
        //2. 校验paper == null -> 抛异常
        if (paper == null){
            throw new RuntimeException("指定id:%s试卷已经被删除，无法查看详情！".formatted(id));
        }
        //3. 根据paperid查询题目集合（中间，题目，答案，选项）
        List<Question> questionList = questionMapper.customQueryQuestionListByPaperId(id);
        //4. 校验题目集合 == null -> 赋空集合！ log->做好记录
        if (ObjectUtils.isEmpty(questionList)){
            paper.setQuestions(new ArrayList<Question>());
            log.warn("试卷中没有题目！可以进行试卷编辑！但是不能用于考试！！,对应试卷id：{}",id);
            return paper;
        }
        log.debug("题目信息排序前：{}",questionList);
        //对题目进行排序（选择 -> 判断 -> 简答）
        questionList.sort((o1, o2) -> Integer.compare(typeToInt(o1.getType()),typeToInt(o2.getType())));
        //注意：type排序，是字符类型 -》 字符 -》 对应 -》 固定的数字 1 2 3
        log.debug("题目信息排序后：{}",questionList);
        //进行paper题目集合赋值
        paper.setQuestions(questionList);
        return paper;
    }



    /**
     * 获取题目类型的排序顺序
     * @param type 题目类型
     * @return 排序序号
     */
    private int typeToInt(String type) {
        switch (type) {
            case "CHOICE": return 1; // 选择题
            case "JUDGE": return 2;  // 判断题
            case "TEXT": return 3;   // 简答题
            default: return 4;       // 其他类型
        }
    }

}