package com.atguigu.exam.mapper;

import com.atguigu.exam.entity.Notice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 公告Mapper接口
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {

    @Select("SELECT * FROM  notices WHERE is_active=1 and notices.is_deleted=0 ORDER BY create_time DESC LIMIT #{limit}")
    List<Notice> selectLatestNotices(@Param("limit") int limit);
}