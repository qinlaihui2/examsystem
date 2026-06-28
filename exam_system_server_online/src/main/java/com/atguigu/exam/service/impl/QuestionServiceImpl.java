package com.atguigu.exam.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.exam.common.CacheConstants;
import com.atguigu.exam.entity.PaperQuestion;
import com.atguigu.exam.entity.Question;
import com.atguigu.exam.entity.QuestionAnswer;
import com.atguigu.exam.entity.QuestionChoice;
import com.atguigu.exam.mapper.PaperQuestionMapper;
import com.atguigu.exam.mapper.QuestionAnswerMapper;
import com.atguigu.exam.mapper.QuestionChoiceMapper;
import com.atguigu.exam.mapper.QuestionMapper;
import com.atguigu.exam.service.KimiAiService;
import com.atguigu.exam.service.QuestionService;
import com.atguigu.exam.utils.ExcelUtil;
import com.atguigu.exam.utils.RedisUtils;
import com.atguigu.exam.vo.AiGenerateRequestVo;
import com.atguigu.exam.vo.QuestionImportVo;
import com.atguigu.exam.vo.QuestionQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.netty.util.internal.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import netscape.javascript.JSObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 题目Service实现类
 * 实现题目相关的业务逻辑
 */
@Slf4j
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {


    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionAnswerMapper questionAnswerMapper;

    @Autowired
    private QuestionChoiceMapper questionChoiceMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private KimiAiService kimiAiService;

    @Override
    public void queryQuestionListByPage(Page<Question> questionPage, QuestionQueryVo questionQueryVo) {
         questionMapper.selectQuestionByPage(questionPage, questionQueryVo);
    }

    /**
     * 查询题目的详情
     *   题目+答案+选项
     *   方法1：嵌套如果连表查询
     *   2.嵌套查询分布查询
     *   3.查询+java
     * @return
     */
    @Override
    public Question queryQuestionByid(Long id) {
        //1查询题目详情对象
        Question question = getById(id);
        if(question==null){
            //log.info("查询id={}的题目不存在");
            throw new RuntimeException("查询id=%s的题目不存在或者已近被删除".formatted(id));
        }
        //2.查询题目对应的答案
        QuestionAnswer questionAnswer = questionAnswerMapper.selectOne(new LambdaQueryWrapper<QuestionAnswer>().eq(QuestionAnswer::getQuestionId, id));
        question.setAnswer(questionAnswer);
        //3.查询题目对应的选项（选择题才有选项）
        if("CHOICE".equals(question.getType())){
            List<QuestionChoice> questionChoices = questionChoiceMapper.selectList(new LambdaQueryWrapper<QuestionChoice>().eq(QuestionChoice::getQuestionId, id));
            question.setChoices(questionChoices);
        }
        //4.题目详情赋值

        //5.预留：进行redis的数据缓存zset
        new Thread(()-> {
                incrementQuestionScore(question.getId());
        }).start();
        return question;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveQuestion(Question question) {
        //1.先判断有没有重复的题目，在同一个题目下有没有标题一样的题目，title不可以重复
        LambdaQueryWrapper<Question> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Question::getType, question.getType());
        lambdaQueryWrapper.eq(Question::getTitle, question.getTitle());
        long count = count(lambdaQueryWrapper);
        if(count>0){
            throw new RuntimeException("在%s的类型下，已近存在名为%s的题目信息保存失败".formatted(question.getType(), question.getTitle()));
        }
        //2.保存题目信息（先保存题目信息，有了题目id才可以进行答案和选项的保存
        save(question);
        //3.判断是不是选择题，是，根据选项的正确给答案赋值，同时加选项加入到选选项表
        QuestionAnswer answer = question.getAnswer();
        answer.setQuestionId(question.getId());
        if ("CHOICE".equals(question.getType())){
            //是 -》 循环 -》 选项 + 题目id -> 保存 -》 判断是不是正确 进行 AD
            List<QuestionChoice> choices = question.getChoices();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < choices.size(); i++) {
                //给每个选项匹配questionId
                // [0 [1] 2 [3] ]
                QuestionChoice choice = choices.get(i);
                //确保，正确顺序！ 否则默认是0 随机了
                choice.setSort(i);
                choice.setQuestionId(question.getId());
                questionChoiceMapper.insert(choice);
                if (choice.getIsCorrect()){
                    //true 本次是正确答案
                    if (sb.length() > 0){
                        sb.append(",");
                    }
                    //B,D
                    sb.append((char)('A'+i));
                }
            }

            //进行答案赋值
            answer.setAnswer(sb.toString());
        }
        //4.完成答案数据的插入
        questionAnswerMapper.insert(answer);
    }

    private void fillQuestionChoiceAndAnswer(List<Question> questionList) {
        //1. 非空判断
        if (questionList == null || questionList.size() == 0) {
            log.debug("没有查询对应的问题集合数据！！");
            return;
        }
        //2. 查询所有答案和选项
        //优化查询本次题目的答案和选项
        //查询本地题目集合对应的id集合！！
        List<Long> ids = questionList.stream().map(Question::getId).collect(Collectors.toList());
        //查询本次题目的选项集合
        List<QuestionChoice> questionChoiceList =
                questionChoiceMapper.selectList(new LambdaQueryWrapper<QuestionChoice>().in(QuestionChoice::getQuestionId,ids));
        //查询本次题目的答案
        List<QuestionAnswer> questionAnswers =
                questionAnswerMapper.selectList(new LambdaQueryWrapper<QuestionAnswer>().in(QuestionAnswer::getQuestionId,ids));
        //3. 答案和选项进行map转化
        Map<Long, List<QuestionChoice>> questionChoiceMap =
                questionChoiceList.stream().collect(Collectors.groupingBy(QuestionChoice::getQuestionId));
        Map<Long, QuestionAnswer> answerMap =
                questionAnswers.stream().collect(Collectors.toMap(QuestionAnswer::getQuestionId, a -> a));
        //4. 循环问题集合，进行选项和答案配置
        questionList.forEach(question -> {
            //每个题目一定答案
            question.setAnswer(answerMap.get(question.getId()));
            //选择题才有选项
            if ("CHOICE".equals(question.getType())){
                List<QuestionChoice> questionChoices = questionChoiceMap.get(question.getId());
                questionChoices.sort(Comparator.comparingInt(QuestionChoice::getSort));
                question.setChoices(questionChoices);
            }
        });
    }
    /**
     * 更新题目及其完整信息（包含选项和答案）
     * <p>
     * 业务复杂性：
     * - 需要处理选项的增删改：删除旧选项，添加新选项
     * - 答案更新：覆盖原有答案或新增答案
     * - 数据完整性：确保更新过程中数据一致
     * <p>
     * 实现策略：
     * 1. 更新题目主表信息
     * 2. 删除原有选项，重新插入新选项（简化逻辑）
     * 3. 更新或插入答案信息
     *
     * @param question 包含更新信息的题目对象
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void customUpdateQuestion(Question question) {
        //1. 题目的校验 （不同id不运行title重复）
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getTitle,question.getTitle());
        queryWrapper.ne(Question::getId,question.getId());
        boolean exists = baseMapper.exists(queryWrapper);
        if (exists) {
            throw new RuntimeException("修改：%s题目的新标题：%s和其他的题目重复了！修改失败！".formatted(question.getId(),question.getTitle()));
        }
        //2. 修改题目
        boolean updated = updateById(question);
        if (!updated){
            throw new RuntimeException("修改：%s题目失败！！".formatted(question.getId()));
        }
        //3. 获取答案对象
        QuestionAnswer answer = question.getAnswer();
        //4. 判断是选择题
        if ("CHOICE".equals(question.getType())){
            List<QuestionChoice> choiceList = question.getChoices();
            //删除题目对应的所有选项（原） [根据题目id删除]
            LambdaQueryWrapper<QuestionChoice> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(QuestionChoice::getQuestionId,question.getId());
            questionChoiceMapper.delete(lambdaQueryWrapper);
            //循环新增选项（选项上id == null）
            // 拼接正确的档案 a,b
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < choiceList.size(); i++) {
                QuestionChoice choice = choiceList.get(i);
                choice.setId(null);
                //确保，正确顺序！ 否则默认是0 随机了
                choice.setSort(i);
                choice.setCreateTime(null);
                choice.setUpdateTime(null);
                //新增选项需要！！
                choice.setQuestionId(question.getId());
                questionChoiceMapper.insert(choice);
                if (choice.getIsCorrect()){
                    if (sb.length() > 0){
                        sb.append(",");
                    }
                    sb.append((char)('A'+i));
                }
            }
            //答案对象赋值选择题答案
            answer.setAnswer(sb.toString());
        }
        //5. 进行答案的修改
        questionAnswerMapper.updateById(answer);
        //6. 保证一致性，添加事务
    }

    @Autowired
    private PaperQuestionMapper paperQuestionMapper;

    /**
     * 删除题目
     * 实现策略：
     * 1. 判断试卷是有有引用题目，有，删除失败！提示！
     * 2. 先删除子数据（选项和答案）
     * 3. 删除主数据题目表
     *
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void customRemoveQuestionById(Long id) {
        //1. 判断试卷题目表，存在删除失败！
        LambdaQueryWrapper<PaperQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaperQuestion::getQuestionId,id);
        Long count = paperQuestionMapper.selectCount(queryWrapper);
        if (count > 0){
            throw new RuntimeException("该题目：%s 被试卷表中引用%s次，删除失败！".formatted(id,count));
        }
        //2. 删除主表 题目表
        boolean removed = removeById(id);
        if (!removed){
            throw new RuntimeException("该题目：%s 信息删除失败！！");
        }
        //3. 删除子表 答案和选项表
        questionAnswerMapper.delete(new LambdaQueryWrapper<QuestionAnswer>().eq(QuestionAnswer::getQuestionId,id));
        questionChoiceMapper.delete(new LambdaQueryWrapper<QuestionChoice>().eq(QuestionChoice::getQuestionId,id));
    }

    @Override
    public List<Question> queryPopularQuestionList(Integer size) {

        //定义一个集合存储热门集合
        List<Question> popularQestionList=new ArrayList<>();

        Set<Object> popularIds = redisUtils.zReverseRange(CacheConstants.POPULAR_QUESTIONS_KEY, 0, size - 1);
        if(!ObjectUtils.isEmpty(popularIds)){
            //倒序
            List<Long> longList = popularIds.stream().map(id -> Long.valueOf(id.toString())).collect(Collectors.toList());
            //处理热门题目
            //List<Question> questionList =listByIds(longList);
            for (Long id : longList) {
                Question question = getById(id);
                //校验id,题目可能被删除
                if(question!=null){
                    popularQestionList.add(question);
                }
            }

        }
        int diff=size-popularQestionList.size();
        if(diff>0){
            LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(Question::getCreateTime);

            //排除已近选过的id
            List<Long> collect = popularQestionList.stream().map(Question::getId).collect(Collectors.toList());
            queryWrapper.notIn(!ObjectUtils.isEmpty(collect),Question::getId, collect);
            //切割指定的diff
            queryWrapper.last("limit "+diff);
            List<Question> newsQuestions = list(queryWrapper);
            popularQestionList.addAll(newsQuestions);
        }
        fillQuestionChoiceAndAnswer(popularQestionList);
        return popularQestionList;
    }

    @Override
    public List<QuestionImportVo> preViewExcel(MultipartFile file) throws IOException {
        //数据校验
        if (file == null || file.isEmpty()){
            throw new RuntimeException("预览数据的文件为空！");
        }
        String fileName = file.getOriginalFilename();
        //xls xlsx
        if (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx")){
            throw new RuntimeException("预览数据的文件格式错误，必须是 .xls或者.xlsx！");
        }
        //解析数据
        List<QuestionImportVo> questionImportVoList = ExcelUtil.parseExcel(file);
        //返回结果
        return questionImportVoList;
    }

    @Override
    public String importQuestion(List<QuestionImportVo> questions) {
        //传入预览数据集合非空判断
        if(ObjectUtils.isEmpty(questions)){
            return "批量导入失败，本次无题目导入！题目传递数据为空";
        }
        //定义服务降级的代码结构
        int successNumber =0;//保存成功的数量
        for (QuestionImportVo questionImportVo : questions) {
            try{
                //循环中进行vo->question以及题目保存业务的调用
                Question question=new Question();
                BeanUtils.copyProperties(questionImportVo,question);
                if("CHOICE".equals(question.getType())){
                    List<QuestionChoice> questionChoices=new ArrayList<>(questionImportVo.getChoices().size());
                    for (QuestionImportVo.ChoiceImportDto importVoChoice : questionImportVo.getChoices()) {
                        QuestionChoice questionChoice= new QuestionChoice();
                        questionChoice.setContent(importVoChoice.getContent());
                        questionChoice.setIsCorrect(importVoChoice.getIsCorrect());
                        questionChoice.setSort(importVoChoice.getSort());
                        questionChoices.add(questionChoice);
                    }
                    question.setChoices(questionChoices);
                }
                QuestionAnswer questionAnswer=new QuestionAnswer();
                if("JUDGE".equals(question.getType())){
                    questionAnswer.setAnswer(questionImportVo.getAnswer().toUpperCase());
                }else {
                    questionAnswer.setAnswer(questionImportVo.getAnswer());
                }
                questionAnswer.setKeywords(questionImportVo.getKeywords());
                question.setAnswer(questionAnswer);
                //1.questionImprtVo-question->属性->question对象
                //2.questionImportVo->List<ChoicesImportVO>->question对象-》List<QuestionChoice choices(选择题)>
                //3.questionImportVo->answer and keyword ANswer对象-》question对象->answer
                saveQuestion(question);
                successNumber++;
            }catch (Exception e){
              log.error("保存题目{}的时候失效了",questionImportVo.getTitle());
            }
        }


        String result="题目批量导入接口调用结束，共计导入%s条，数据共%s条".formatted(successNumber,questions.size());
        //拼接反馈结构，题目批量导入接口调用结束，共计导入x条
        return result;
    }

    /**
     * ai题目生成
     * @param request 生成题目的参数
     * @return 返回的预览数据
     */
    @Override
    public List<QuestionImportVo> aiGenerateQuestion(AiGenerateRequestVo request) throws InterruptedException {
        //1生成AI提示词
        String prompt=kimiAiService.buildPrompt(request);
        log.debug("ai出题的条件为{},生成的对应的提示词为{}",request,prompt);
        //2.调用ai模型获取结果
        String response=kimiAiService.callKimiAI(prompt);
        //3.进行结果解析
        //3.1判定开始（```json）和结束字符的位置(```)
        int startIndex = response.indexOf("```json") ;
        int endIndex = response.lastIndexOf("```");

        if(startIndex != -1 && endIndex != -1 && startIndex<endIndex){
            //数据结构是正常的
            String resultJson = response.substring(startIndex + 7, endIndex);
            JSONObject jsonObject=JSONObject.parseObject(resultJson);
            JSONArray questions = jsonObject.getJSONArray("questions");
            List<QuestionImportVo> questionImportVoList=new ArrayList<>();
            for (int i = 0; i < questions.size(); i++) {
                //循环解析内容{}
                //获取对象
                JSONObject questionJson = questions.getJSONObject(i);
                QuestionImportVo questionImportVo = new QuestionImportVo();
                questionImportVo.setTitle(questionJson.getString("title"));
                questionImportVo.setType(questionJson.getString("type"));
                questionImportVo.setMulti(questionJson.getBoolean("multi"));
                questionImportVo.setDifficulty(questionJson.getString("difficulty"));
                questionImportVo.setScore(questionJson.getInteger("score"));
                questionImportVo.setAnalysis(questionJson.getString("analysis"));
                questionImportVo.setCategoryId(request.getCategoryId());




                //选择题处理选项
                if ("CHOICE".equals(questionImportVo.getType())) {
                    JSONArray choices = questionJson.getJSONArray("choices");
                    List<QuestionImportVo.ChoiceImportDto> choiceImportDtoList = new ArrayList<>(choices.size());
                    for (int i1 = 0; i1 < choices.size(); i1++) {
                        JSONObject choicesJSONObject = choices.getJSONObject(i1);
                        QuestionImportVo.ChoiceImportDto choiceImportDto = new QuestionImportVo.ChoiceImportDto();
                        choiceImportDto.setContent(choicesJSONObject.getString("content"));
                        choiceImportDto.setIsCorrect(choicesJSONObject.getBoolean("isCorrect"));
                        choiceImportDto.setSort(choicesJSONObject.getInteger("sort"));
                        choiceImportDtoList.add(choiceImportDto);
                    }
                    questionImportVo.setChoices(choiceImportDtoList);
                }
                //答案 [判断题！ TRUE |FALSE  false true  f  t 是 否]
                questionImportVo.setAnswer(questionJson.getString("answer"));
                questionImportVoList.add(questionImportVo);
            }
         return questionImportVoList;

        }
        
        throw new RuntimeException("ai生成的数据结构无法解析，具体数据为:%s".formatted(response));
        
        
    }

    /**
     * 方法进行题目加分在排行榜1中 被异步调用
     * @param questionId
     */
    private void incrementQuestionScore(Long questionId) {
        Double score = redisUtils.zIncrementScore(CacheConstants.POPULAR_QUESTIONS_KEY, questionId, 1);
        log.debug("完成Id{}题目的热榜分数积累，累计分数为{}", questionId, score);
    }
}