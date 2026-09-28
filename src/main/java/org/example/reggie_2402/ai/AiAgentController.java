package org.example.reggie_2402.ai;

import lombok.extern.slf4j.Slf4j;
import org.example.reggie_2402.common.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 知识问答入口：用户端登录后调用，从 ThreadLocal（BaseContext）获取当前用户，
 * 实现"我的订单到哪了""我上次点了什么""有什么辣菜"等问答。
 * 注意：/ai/chat 不在 LoginCheckFilter 白名单里，未登录会被拦截返回 NOTLOGIN——这是设计预期：
 * 问答需要知道"当前用户是谁"才能查到他的订单。
 */
@RestController
@RequestMapping("/ai")
@Slf4j
public class AiAgentController {
    @Autowired
    private AiAgentService aiAgentService;

    @PostMapping("/chat")
    public R<String> chat(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        log.info("AI 问答：{}", question);
        return aiAgentService.chat(question);
    }
}
