package com.atguigu.exam.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MybatisPlusConfiguration
 *
 * @author 李嘉宇
 * @date 2025/10/30 19:44
 * @version 1.0
 * @description
 */
@MapperScan(basePackages = "com.atguigu.exam.mapper")
@Configuration
public class MybatisPlusConfiguration {
    @Bean
    public MybatisPlusInterceptor plusInterceptor(){

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

}
