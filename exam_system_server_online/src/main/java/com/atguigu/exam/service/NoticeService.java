package com.atguigu.exam.service;

import com.atguigu.exam.common.Result;
import com.atguigu.exam.entity.Notice;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 公告服务接口
 */
public interface NoticeService extends IService<Notice> {


    List<Notice> getLatestNotice(int limit);
} 