package com.atguigu.exam.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinioProperties
 *
 * @author 李嘉宇
 * @date 2025/10/31 12:53
 * @version 1.0
 * @description 读取minio中的配置参数
 */
@Data
@Component//将我们加入到核心容器，我们才可以读取配置参数
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {


    private String endPoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;
}
