package com.lookforbest.controller;

import com.lookforbest.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "AI 对话", description = "AI 机器人选型助手")
public class ChatController {

    private final ChatService chatService;

    @PostMapping(value = "/message", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "发送消息（SSE 流式响应）")
    public SseEmitter sendMessage(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String sessionKey = body.getOrDefault("sessionKey", java.util.UUID.randomUUID().toString());
        String message = body.getOrDefault("message", "");
        Long userId = null; // 可从 userDetails 解析
        return chatService.chat(sessionKey, message, userId);
    }

    @GetMapping("/sessions/{sessionKey}/history")
    @Operation(summary = "获取对话历史")
    public ResponseEntity<List<Map<String, String>>> getHistory(@PathVariable String sessionKey) {
        return ResponseEntity.ok(chatService.getHistory(sessionKey));
    }

    @DeleteMapping("/sessions/{sessionKey}")
    @Operation(summary = "清除对话会话")
    public ResponseEntity<Map<String, String>> clearSession(@PathVariable String sessionKey) {
        chatService.clearSession(sessionKey);
        return ResponseEntity.ok(Map.of("message", "会话已清除"));
    }
}
