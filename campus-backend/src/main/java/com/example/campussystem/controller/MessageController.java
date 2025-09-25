package com.example.campussystem.controller;

import com.example.campussystem.dto.ConversationResponse;
import com.example.campussystem.dto.MessageRequest;
import com.example.campussystem.dto.MessageResponse;
import com.example.campussystem.service.MessageService;
import com.example.campussystem.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Result<MessageResponse> sendMessage(
            @Valid @RequestBody MessageRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            System.out.println("MessageController获取到的userId: " + userId);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            MessageResponse response = messageService.sendMessage(request, userId);
            return Result.success(response);
        } catch (Exception e) {
            System.out.println("发送消息异常: " + e.getMessage());
            return Result.error("发送消息失败: " + e.getMessage());
        }
    }

    /**
     * 获取对话消息列表
     */
    @GetMapping("/conversation/{contactUserId}")
    public Result<List<MessageResponse>> getConversationMessages(
            @PathVariable Long contactUserId,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            List<MessageResponse> messages = messageService.getConversationMessages(userId, contactUserId);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error("获取对话消息失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取对话消息列表
     */
    @GetMapping("/conversation/{contactUserId}/page")
    public Result<Page<MessageResponse>> getConversationMessagesPage(
            @PathVariable Long contactUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            Page<MessageResponse> messagePage = messageService.getConversationMessagesPage(userId, contactUserId, page, size);
            return Result.success(messagePage);
        } catch (Exception e) {
            return Result.error("分页获取对话消息失败: " + e.getMessage());
        }
    }

    /**
     * 获取对话列表
     */
    @GetMapping("/conversations")
    public Result<List<ConversationResponse>> getConversationList(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            List<ConversationResponse> conversations = messageService.getConversationList(userId);
            return Result.success(conversations);
        } catch (Exception e) {
            return Result.error("获取对话列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取未读消息列表
     */
    @GetMapping("/unread")
    public Result<List<MessageResponse>> getUnreadMessages(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            List<MessageResponse> messages = messageService.getUnreadMessages(userId);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error("获取未读消息失败: " + e.getMessage());
        }
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/{messageId}/read")
    public Result<Void> markMessageAsRead(
            @PathVariable Long messageId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        messageService.markMessageAsRead(messageId, userId);
        return Result.success();
    }

    /**
     * 批量标记对话消息为已读
     */
    @PutMapping("/conversation/{contactUserId}/read")
    public Result<Void> markConversationAsRead(
            @PathVariable Long contactUserId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        messageService.markConversationAsRead(userId, contactUserId);
        return Result.success();
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread/count")
    public Result<Long> getUnreadMessageCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        long count = messageService.getUnreadMessageCount(userId);
        return Result.success(count);
    }

    /**
     * 获取发送的消息列表
     */
    @GetMapping("/sent")
    public Result<List<MessageResponse>> getSentMessages(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<MessageResponse> messages = messageService.getSentMessages(userId);
        return Result.success(messages);
    }

    /**
     * 获取接收的消息列表
     */
    @GetMapping("/received")
    public Result<List<MessageResponse>> getReceivedMessages(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<MessageResponse> messages = messageService.getReceivedMessages(userId);
        return Result.success(messages);
    }

    /**
     * 获取用户所有消息列表
     */
    @GetMapping("/all")
    public Result<List<MessageResponse>> getUserMessages(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<MessageResponse> messages = messageService.getUserMessages(userId);
        return Result.success(messages);
    }

    /**
     * 分页获取用户所有消息列表
     */
    @GetMapping("/all/page")
    public Result<Page<MessageResponse>> getUserMessagesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Page<MessageResponse> messagePage = messageService.getUserMessagesPage(userId, page, size, sortBy, sortDir);
        return Result.success(messagePage);
    }

    /**
     * 获取关于特定商品的消息列表
     */
    @GetMapping("/product/{productId}")
    public Result<List<MessageResponse>> getProductMessages(
            @PathVariable Long productId) {
        List<MessageResponse> messages = messageService.getProductMessages(productId);
        return Result.success(messages);
    }

    /**
     * 获取用户关于特定商品的对话消息
     */
    @GetMapping("/product/{productId}/conversation/{contactUserId}")
    public Result<List<MessageResponse>> getProductConversationMessages(
            @PathVariable Long productId,
            @PathVariable Long contactUserId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<MessageResponse> messages = messageService.getProductConversationMessages(productId, userId, contactUserId);
        return Result.success(messages);
    }

    /**
     * 搜索消息
     */
    @GetMapping("/search")
    public Result<List<MessageResponse>> searchMessages(
            @RequestParam String keyword) {
        List<MessageResponse> messages = messageService.searchMessages(keyword);
        return Result.success(messages);
    }
    
    /**
     * 获取会话更新（用于轮询）
     */
    @GetMapping("/conversations/updates")
    public Result<List<ConversationResponse>> getConversationUpdates(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            List<ConversationResponse> conversations = messageService.getConversations(userId);
            return Result.success(conversations);
        } catch (Exception e) {
            return Result.error("获取会话更新失败: " + e.getMessage());
        }
    }

    /**
     * 在对话中搜索消息
     */
    @GetMapping("/conversation/{contactUserId}/search")
    public Result<List<MessageResponse>> searchConversationMessages(
            @PathVariable Long contactUserId,
            @RequestParam String keyword,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<MessageResponse> messages = messageService.searchConversationMessages(userId, contactUserId, keyword);
        return Result.success(messages);
    }

    /**
     * 删除对话
     */
    @DeleteMapping("/conversation/{contactUserId}")
    public Result<Void> deleteConversation(
            @PathVariable Long contactUserId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        messageService.deleteConversation(userId, contactUserId);
        return Result.success();
    }

    /**
     * 获取用户消息统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Long>> getUserMessageStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Long> stats = messageService.getUserMessageStats(userId);
        return Result.success(stats);
    }

    /**
     * 获取用户总未读消息数（用于导航栏红点提示）
     */
    @GetMapping("/unread/total")
    public Result<Map<String, Object>> getTotalUnreadCount(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            long totalUnread = messageService.getUnreadMessageCount(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("totalUnread", totalUnread);
            result.put("hasUnread", totalUnread > 0);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取未读消息数失败: " + e.getMessage());
        }
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/{messageId}/read")
    public Result<Void> markAsRead(
            @PathVariable Long messageId,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            messageService.markMessageAsRead(messageId, userId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("标记已读失败: " + e.getMessage());
        }
    }
}