package org.example.reggie_2402.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * 大模型调用客户端：OpenAI 兼容协议（POST /chat/completions），兼容绝大多数国产模型与本地模型服务。
 * 请求体：{"model":模型名,"messages":[{"role":"system","content":...},{"role":"user","content":...}],"temperature":...}
 * 响应体：取 choices[0].message.content 作为模型回复文本。
 */
@Component
public class LlmClient {
    @Autowired
    private LlmConfig config;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /** 调用大模型：system 为系统提示词（角色/任务/格式约束），user 为用户输入 */
    public String chat(String system, String user) throws Exception {
        //1.组装请求体：messages 包含 system（系统提示）与 user（用户输入）两条消息，用 fastjson 序列化
        JSONObject body = new JSONObject();
        body.put("model", config.getModel());
        JSONArray messages = new JSONArray();
        messages.add(new JSONObject().fluentPut("role", "system").fluentPut("content", system));
        messages.add(new JSONObject().fluentPut("role", "user").fluentPut("content", user));
        body.put("messages", messages);
        body.put("temperature", config.getTemperature());
        //2.发送 POST 请求到 baseUrl + "/chat/completions"，Header 携带 Authorization: Bearer apiKey
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getBaseUrl() + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        //3.非 200 状态码抛异常，交给上层重试逻辑处理；解析响应 JSON，返回 choices[0].message.content
        if (response.statusCode() != 200) {
            throw new RuntimeException("模型调用失败：" + response.body());
        }
        return JSON.parseObject(response.body())
                .getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").getString("content");
    }
}
