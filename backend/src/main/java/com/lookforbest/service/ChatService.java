package com.lookforbest.service;

import com.lookforbest.entity.ChatMessage;
import com.lookforbest.entity.ChatSession;
import com.lookforbest.entity.Robot;
import com.lookforbest.repository.ChatMessageRepository;
import com.lookforbest.repository.ChatSessionRepository;
import com.lookforbest.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    @Value("${app.anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${app.anthropic.model:claude-sonnet-4-6}")
    private String claudeModel;

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final RobotRepository robotRepository;
    private final RestTemplate restTemplate;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";

    /** 获取或创建会话 */
    @Transactional
    public ChatSession getOrCreateSession(String sessionKey, Long userId) {
        return sessionRepository.findBySessionKey(sessionKey).orElseGet(() -> {
            ChatSession s = new ChatSession();
            s.setSessionKey(sessionKey);
            s.setUserId(userId);
            return sessionRepository.save(s);
        });
    }

    /** 获取会话历史 */
    public List<Map<String, String>> getHistory(String sessionKey) {
        return sessionRepository.findBySessionKey(sessionKey)
                .map(s -> messageRepository.findBySessionIdOrderByCreatedAtAsc(s.getId())
                        .stream()
                        .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    /** 清除会话 */
    @Transactional
    public void clearSession(String sessionKey) {
        sessionRepository.findBySessionKey(sessionKey).ifPresent(s -> {
            messageRepository.deleteAll(messageRepository.findBySessionIdOrderByCreatedAtAsc(s.getId()));
            sessionRepository.delete(s);
        });
    }

    /** 流式对话（SSE） */
    public SseEmitter chat(String sessionKey, String userMessage, Long userId) {
        SseEmitter emitter = new SseEmitter(60_000L);

        executor.submit(() -> {
            try {
                // 1. 获取/创建会话
                ChatSession session = getOrCreateSession(sessionKey, userId);

                // 2. 保存用户消息
                ChatMessage userMsg = new ChatMessage();
                userMsg.setSessionId(session.getId());
                userMsg.setRole("user");
                userMsg.setContent(userMessage);
                messageRepository.save(userMsg);

                // 3. 构建历史消息
                List<ChatMessage> history = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
                List<Map<String, String>> messages = history.stream()
                        .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                        .collect(Collectors.toList());

                // 4. 构建系统提示词（含机器人摘要）
                String systemPrompt = buildSystemPrompt(userMessage);

                // 5. 调用 Claude API
                String fullResponse = callClaude(systemPrompt, messages, emitter);

                // 6. 保存 assistant 消息
                ChatMessage assistantMsg = new ChatMessage();
                assistantMsg.setSessionId(session.getId());
                assistantMsg.setRole("assistant");
                assistantMsg.setContent(fullResponse);
                messageRepository.save(assistantMsg);

                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                log.error("Chat error: {}", e.getMessage());
                try {
                    emitter.send(SseEmitter.event().name("error").data("对话服务异常：" + e.getMessage()));
                } catch (IOException ignored) {}
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private String buildSystemPrompt(String userMessage) {
        // 从数据库获取最相关的机器人摘要（简单关键词匹配）
        List<Robot> robots;
        try {
            robots = robotRepository.findAll(PageRequest.of(0, 30)).getContent();
        } catch (Exception e) {
            robots = List.of();
        }

        StringBuilder robotCatalog = new StringBuilder();
        for (Robot r : robots) {
            robotCatalog.append(String.format("- %s（%s）：%s，价格：%s，类别：%s\n",
                    r.getName(),
                    r.getNameEn() != null ? r.getNameEn() : "",
                    r.getDescription() != null ? r.getDescription().substring(0, Math.min(100, r.getDescription().length())) : "",
                    r.getPriceUsdFrom() != null ? r.getPriceUsdFrom() + "元" : "面议",
                    r.getCategory() != null ? r.getCategory().getName() : "未分类"
            ));
        }

        return """
                你是 LookForBest 机器人选型助手，专门帮助用户找到最适合的机器人产品。

                平台当前机器人产品摘要：
                %s

                请根据用户需求：
                1. 理解用户的使用场景和需求
                2. 从上述产品中推荐最合适的1-3款机器人
                3. 说明推荐理由（价格、功能、适用场景）
                4. 用简洁、专业的中文回答
                5. 如果推荐了具体产品，在回复末尾用JSON格式列出推荐产品名称：{"recommendations": ["产品名1", "产品名2"]}
                """.formatted(robotCatalog);
    }

    /**
     * 调用 Anthropic Messages API（非流式，简化实现）
     * 生产环境建议使用官方 Anthropic Java SDK 做真正的流式响应
     */
    @SuppressWarnings("unchecked")
    private String callClaude(String systemPrompt, List<Map<String, String>> messages, SseEmitter emitter) throws IOException {
        if (anthropicApiKey == null || anthropicApiKey.isBlank()) {
            String mock = "您好！我是 LookForBest AI 助手。请配置 ANTHROPIC_API_KEY 环境变量以启用真实 AI 对话。目前您可以通过搜索功能浏览我们的机器人产品目录。";
            emitter.send(SseEmitter.event().name("token").data(mock));
            return mock;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", anthropicApiKey);
        headers.set("anthropic-version", "2023-06-01");

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", claudeModel);
        requestBody.put("max_tokens", 1024);
        requestBody.put("system", systemPrompt);
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(ANTHROPIC_API_URL, HttpMethod.POST, entity, Map.class);
            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("content")) {
                List<Map<String, Object>> content = (List<Map<String, Object>>) body.get("content");
                if (!content.isEmpty()) {
                    String text = (String) content.get(0).get("text");
                    // 模拟流式输出：按句子分段发送
                    String[] chunks = text.split("(?<=。|！|？|\\n)");
                    for (String chunk : chunks) {
                        if (!chunk.isBlank()) {
                            emitter.send(SseEmitter.event().name("token").data(chunk));
                            Thread.sleep(30);
                        }
                    }
                    return text;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Claude API 调用失败: {}", e.getMessage());
            String errMsg = "AI 服务暂时不可用，请稍后重试。";
            emitter.send(SseEmitter.event().name("token").data(errMsg));
            return errMsg;
        }

        return "";
    }
}
