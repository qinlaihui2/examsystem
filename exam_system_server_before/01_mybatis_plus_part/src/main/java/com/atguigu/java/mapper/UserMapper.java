package com.atguigu.java.mapper;

import com.atguigu.java.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {
    //查询年龄大于传入参数的用户集合
    IPage<User> queryUserByAge(IPage<User> page, int age);

}
