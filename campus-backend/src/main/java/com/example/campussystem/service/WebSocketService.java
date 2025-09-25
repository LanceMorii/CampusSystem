package com.example.campussystem.service;

import com.example.campussystem.dto.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * 基于 STOMP 的 WebSocket 推送服务。
 * 说明：后端已移除原生 WebSocket，统一通过 STOMP 进行消息推送。
 */
@Service
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    // 用户队列目的地（前端应订阅 /user/queue/new-message）
    public static final String USER_NEW_MESSAGE_QUEUE = "/queue/new-message";

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 向指定用户发送“新消息”通知。
     * 前端应通过 STOMP 订阅 /user/queue/new-message 接收。
     *
     * @param userId 目标用户ID
     * @param payload 要推送的消息内容
     */
    public void sendNewMessageNotification(Long userId, MessageResponse payload) {
        if (userId == null) {
            logger.warn("sendNewMessageNotification skipped: userId is null");
            return;
        }
        try {
            messagingTemplate.convertAndSendToUser(String.valueOf(userId), USER_NEW_MESSAGE_QUEUE, payload);
        } catch (Exception e) {
            logger.error("发送 STOMP 用户消息失败, userId={}", userId, e);
        }
    }

    /**
     * 通用方法：向指定用户发送任意目的地的消息。
     */
    public void sendToUser(Long userId, String destination, Object payload) {
        if (userId == null) {
            logger.warn("sendToUser skipped: userId is null");
            return;
        }
        try {
            messagingTemplate.convertAndSendToUser(String.valueOf(userId), destination, payload);
        } catch (Exception e) {
            logger.error("发送 STOMP 用户消息失败, userId={}, destination={}", userId, destination, e);
        }
    }
}