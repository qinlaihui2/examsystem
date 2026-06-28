package com.atguigu.exam.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * KimiApiproperties
 *
 * @author 李嘉宇
 * @date 2025/11/23 20:50
 * @version 1.0
 * @description
 */
@Data
@ConfigurationProperties(prefix = "kimi.api")
public class KimiApiproperties {
    private String model;
    private String uri;
    private String apiKey;
    private Integer maxTokens;
    private Double temperature;
}
