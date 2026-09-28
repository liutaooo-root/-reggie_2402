package org.example.reggie_2402.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 结构化 JSON 输出服务：调用模型后提取并解析 JSON，解析失败自动追加纠正提示重试（最多 3 次）
 */
@Component
public class JsonOutputService {
    @Autowired
    private LlmClient llmClient;

    /** 稳定输出 JSON：systemPrompt 中包含格式约束，返回值一定是解析成功的 JSONObject */
    public JSONObject askForJson(String systemPrompt, String userPrompt) {
        String prompt = userPrompt;
        for (int i = 0; i < 3; i++) {
            try {
                String content = llmClient.chat(systemPrompt, prompt);
                //去掉可能的 ```json ``` 包裹：截取第一个 { 到最后一个 } 之间的内容再解析
                content = content.trim();
                content = content.substring(content.indexOf('{'), content.lastIndexOf('}') + 1);
                return JSON.parseObject(content);
            } catch (Exception e) {
                //解析失败（或调用失败）：追加纠正提示后重试
                prompt = userPrompt + "\n注意：上次输出不是合法 JSON，请严格按要求仅输出 JSON 对象，不要包含任何解释文字。";
            }
        }
        throw new RuntimeException("模型多次输出均不合法");
    }
}
