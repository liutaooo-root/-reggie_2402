package org.example.reggie_2402.ai;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie_2402.common.BaseContext;
import org.example.reggie_2402.common.R;
import org.example.reggie_2402.entity.Orders;
import org.example.reggie_2402.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Agent 主流程：用户自然语言问题 → 意图识别 → 工具路由 → 模型总结回答
 */
@Service
@Slf4j
public class AiAgentService {
    @Autowired
    private LlmClient llmClient;
    @Autowired
    private JsonOutputService jsonOutputService;
    @Autowired
    private AiToolsService aiToolsService;
    @Autowired
    private OrderService orderService;

    /** 意图识别提示词：四类意图，DISH_QUERY 时顺带提取菜品关键词 */
    private static final String INTENT_SYSTEM =
            "你是瑞吉外卖的意图识别引擎。请判断用户问题属于哪种意图：" +
            "ORDER_QUERY（查询我的订单/最近订单/配送状态）、" +
            "ORDER_DETAIL_QUERY（查询某个订单的明细/我上次点了什么）、" +
            "DISH_QUERY（查询菜品/价格/有什么菜）、" +
            "SMALL_TALK（其他闲聊）。" +
            "若为 DISH_QUERY，同时提取菜品关键词。仅输出 JSON：" +
            "{\"intent\":\"意图枚举值\",\"keyword\":\"菜品关键词或 null\"}，不要输出其他内容。";

    /** 回答提示词：仅根据查询结果回答，不编造数据 */
    private static final String ANSWER_SYSTEM =
            "你是瑞吉外卖的智能客服。请仅根据【查询结果】回答用户问题，语气简洁友好；" +
            "查询结果为空或无关时，礼貌告知用户暂时查不到相关信息。不要编造数据。";

    /** Agent 入口：用户自然语言问题 → 结构化回答 */
    public R<String> chat(String userQuestion) {
        //第一步：意图识别（结构化 JSON + 失败重试，由 JsonOutputService 保证）
        JSONObject intent = jsonOutputService.askForJson(INTENT_SYSTEM, userQuestion);
        String intentValue = intent.getString("intent");
        //第二步：按意图路由到工具（工具复用项目已有的 Service 查询能力）
        String toolResult;
        switch (intentValue) {
            case "ORDER_QUERY":
                toolResult = aiToolsService.queryLatestOrder();
                break;
            case "ORDER_DETAIL_QUERY":
                //先查最近订单拿到订单 id，再查订单详情
                toolResult = aiToolsService.queryOrderDetail(latestOrderId());
                break;
            case "DISH_QUERY":
                toolResult = aiToolsService.queryDish(intent.getString("keyword"));
                break;
            default:
                //SMALL_TALK：无工具，直接闲聊回答
                return R.success(chatWithRetry(ANSWER_SYSTEM, userQuestion));
        }
        //第三步：把工具查询结果作为上下文，交给模型生成最终自然语言回答
        String prompt = "【查询结果】" + toolResult + "\n【用户问题】" + userQuestion;
        return R.success(chatWithRetry(ANSWER_SYSTEM, prompt));
    }

    /** 模型调用失败重试：最多 3 次，指数退避（500ms → 1s → 2s），全部失败返回兜底话术，保证接口不报 500 */
    private String chatWithRetry(String system, String user) {
        long wait = 500;
        for (int i = 0; i < 3; i++) {
            try {
                return llmClient.chat(system, user);
            } catch (Exception e) {
                log.warn("模型调用失败，第 {} 次重试：{}", i + 1, e.getMessage());
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException ignored) {
                }
                wait *= 2;
            }
        }
        return "抱歉，智能客服暂时开小差了，请稍后再试～";
    }

    /** 查询当前用户最近一笔订单的 id（查询逻辑同工具 1，仅取订单 id） */
    private Long latestOrderId() {
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId())
                .orderByDesc(Orders::getOrderTime).last("limit 1");
        Orders order = orderService.getOne(queryWrapper);
        return order == null ? null : order.getId();
    }
}
