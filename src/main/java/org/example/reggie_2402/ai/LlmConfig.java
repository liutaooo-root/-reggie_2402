package org.example.reggie_2402.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 大模型配置：把 application.yml 的 llm 配置段映射为 Java 对象，实现"切换模型不改代码"
 */
@Component
@ConfigurationProperties(prefix = "llm")
@Data
public class LlmConfig {
    /** OpenAI 兼容端点，如 https://api.deepseek.com/v1 ，切换模型只改配置不改代码 */
    private String baseUrl;
    private String model;
    private String apiKey;
    private Double temperature = 0.2;
}
