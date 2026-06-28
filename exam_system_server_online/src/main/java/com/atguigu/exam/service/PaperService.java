package com.atguigu.exam.service;

import com.atguigu.exam.entity.Paper;
import com.atguigu.exam.vo.AiPaperVo;
import com.atguigu.exam.vo.PaperVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 试卷服务接口
 */
public interface PaperService extends IService<Paper> {

    /**
     *
     * @param paperVo
     * @return
     */
    Paper create(PaperVo paperVo);

    /**
     * 人工智能生成试卷
     * @param aiPaperVo
     * @return
     */
    Paper aiCreatePaper(AiPaperVo aiPaperVo);

    Paper updatePpaer(Integer id, PaperVo paperVo);

    void customRemoveId(Integer id);

    /**
     * 查询试卷详情
     * @param id
     * @return
     */
    Paper customPaperDetailById(Integer id);

}