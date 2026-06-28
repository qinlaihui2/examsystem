package com.atguigu.exam.config;

import com.atguigu.exam.config.properties.MinioProperties;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinioConfiguration
 *
 * @author 李嘉宇
 * @date 2025/10/31 12:59
 * @version 1.0
 * @description 将MinoClient加入到核心容器是实现复用
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfiguration {

    @Autowired
    private MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient = MinioClient.builder().endpoint(minioProperties.getEndPoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey()).build();
        log.info("minio文件服务器连接成功,连接对象信息为{}", minioClient);
        return minioClient;
    }
}
