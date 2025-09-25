package com.example.campussystem.service;

import com.example.campussystem.dto.ConversationResponse;
import com.example.campussystem.dto.MessageRequest;
import com.example.campussystem.dto.MessageResponse;
import com.example.campussystem.entity.Message;
import com.example.campussystem.entity.Product;
import com.example.campussystem.entity.User;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.repository.MessageRepository;
import com.example.campussystem.repository.ProductRepository;
import com.example.campussystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息服务类
 */
@Service
@Transactional
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * 发送消息
     */
    @Transactional
    public MessageResponse sendMessage(MessageRequest request, Long fromUserId) {
        // 添加调试日志
        System.out.println("发送消息调试: fromUserId=" + fromUserId + ", toUserId=" + request.getToUserId());
        
        // 验证接收者是否存在
        User toUser = userRepository.findById(request.getToUserId())
                .orElseThrow(() -> new BusinessException("接收者不存在"));

        // 验证发送者是否存在
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new BusinessException("发送者不存在"));

        System.out.println("用户信息调试: fromUser.studentId=" + fromUser.getStudentId() + 
                          ", toUser.studentId=" + toUser.getStudentId());

        // 验证不能给自己发消息 - 使用学号比较而不是ID比较
        if (fromUser.getStudentId().equals(toUser.getStudentId())) {
            System.out.println("检测到自发消息: fromUser.studentId=" + fromUser.getStudentId() + 
                              ", toUser.studentId=" + toUser.getStudentId());
            throw new BusinessException("不能给自己发送消息");
        }

        // 创建消息
        Message message = new Message();
        message.setFromUserId(fromUserId);
        message.setToUserId(request.getToUserId());
        message.setProductId(request.getProductId());
        message.setContent(request.getContent());
        message.setType(request.getType());
        message.setIsRead(0); // 未读

        Message savedMessage = messageRepository.save(message);
        MessageResponse response = convertToResponse(savedMessage);
        
        // 通过WebSocket发送实时通知
        try {
            webSocketService.sendNewMessageNotification(request.getToUserId(), response);
        } catch (Exception e) {
            // 记录错误但不影响消息发送的主流程
            System.err.println("发送WebSocket通知失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 获取对话消息列表
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getConversationMessages(Long userId, Long contactUserId) {
        // 验证联系人是否存在
        userRepository.findById(contactUserId)
                .orElseThrow(() -> new BusinessException("联系人不存在"));

        List<Message> messages = messageRepository.findConversationMessages(userId, contactUserId);
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 分页获取对话消息列表
     */
    @Transactional(readOnly = true)
    public Page<MessageResponse> getConversationMessagesPage(Long userId, Long contactUserId, int page, int size) {
        // 验证联系人是否存在
        userRepository.findById(contactUserId)
                .orElseThrow(() -> new BusinessException("联系人不存在"));

        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Message> messagePage = messageRepository.findConversationMessages(userId, contactUserId, pageable);
        return messagePage.map(this::convertToResponse);
    }

    /**
     * 获取用户的对话列表
     */
    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversationList(Long userId) {
        List<Message> latestMessages = messageRepository.findLatestMessagesWithContacts(userId);
        List<ConversationResponse> conversations = new ArrayList<>();

        for (Message message : latestMessages) {
            Long contactUserId = message.getFromUserId().equals(userId) ? 
                                message.getToUserId() : message.getFromUserId();
            
            // 获取联系人信息
            User contact = userRepository.findById(contactUserId).orElse(null);
            if (contact == null) continue;

            // 统计与该联系人的未读消息数量
            long unreadCount = messageRepository.countByFromUserIdAndToUserIdAndIsRead(contactUserId, userId, 0);

            ConversationResponse conversation = new ConversationResponse();
            conversation.setContactUserId(contactUserId);
            conversation.setContactUsername(contact.getUsername());
            conversation.setContactNickname(contact.getRealName());
            conversation.setContactAvatar(contact.getAvatar());
            conversation.setLastMessageContent(message.getContent());
            conversation.setLastMessageType(message.getType());
            conversation.setLastMessageTime(message.getCreateTime());
            conversation.setUnreadCount(unreadCount);

            // 如果消息关联商品，设置商品信息
            if (message.getProductId() != null) {
                Product product = productRepository.findById(message.getProductId()).orElse(null);
                if (product != null) {
                    conversation.setProductId(product.getId());
                    conversation.setProductTitle(product.getTitle());
                    conversation.setProductImage(product.getImages());
                }
            }

            conversations.add(conversation);
        }

        return conversations;
    }

    /**
     * 获取用户未读消息列表
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getUnreadMessages(Long userId) {
        List<Message> messages = messageRepository.findByToUserIdAndIsReadOrderByCreateTimeDesc(userId, 0);
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 标记消息为已读
     */
    public void markMessageAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException("消息不存在"));

        // 验证权限：只有接收者可以标记消息为已读
        if (!message.getToUserId().equals(userId)) {
            throw new BusinessException("无权限操作此消息");
        }

        message.markAsRead(); // 使用实体的方法，会同时设置isRead=1和readTime
        messageRepository.save(message);
    }

    /**
     * 批量标记对话消息为已读
     */
    public void markConversationAsRead(Long userId, Long contactUserId) {
        // 验证联系人是否存在
        userRepository.findById(contactUserId)
                .orElseThrow(() -> new BusinessException("联系人不存在"));

        messageRepository.markMessagesAsRead(userId, contactUserId);
    }

    /**
     * 获取用户未读消息数量
     */
    @Transactional(readOnly = true)
    public long getUnreadMessageCount(Long userId) {
        return messageRepository.countByToUserIdAndIsRead(userId, 0);
    }

    /**
     * 获取用户发送的消息列表
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getSentMessages(Long userId) {
        List<Message> messages = messageRepository.findByFromUserIdOrderByCreateTimeDesc(userId);
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 获取用户接收的消息列表
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getReceivedMessages(Long userId) {
        List<Message> messages = messageRepository.findByToUserIdOrderByCreateTimeDesc(userId);
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 获取用户所有消息列表
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getUserMessages(Long userId) {
        List<Message> messages = messageRepository.findUserMessages(userId);
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 分页获取用户所有消息列表
     */
    @Transactional(readOnly = true)
    public Page<MessageResponse> getUserMessagesPage(Long userId, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Message> messagePage = messageRepository.findUserMessages(userId, pageable);
        return messagePage.map(this::convertToResponse);
    }

    /**
     * 获取关于特定商品的消息列表
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getProductMessages(Long productId) {
        // 验证商品是否存在
        productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        List<Message> messages = messageRepository.findByProductIdOrderByCreateTimeDesc(productId);
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 获取用户关于特定商品的对话消息
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getProductConversationMessages(Long productId, Long userId, Long contactUserId) {
        // 验证商品是否存在
        productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        // 验证联系人是否存在
        userRepository.findById(contactUserId)
                .orElseThrow(() -> new BusinessException("联系人不存在"));

        List<Message> messages = messageRepository.findProductConversationMessages(productId, userId, contactUserId);
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 搜索消息
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> searchMessages(String keyword) {
        List<Message> messages = messageRepository.findMessagesByKeyword(keyword);
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    /**
     * 获取用户的会话更新（用于轮询）
     */
    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations(Long userId) {
        // 复用现有的获取对话列表方法
        return getConversationList(userId);
    }

    /**
     * 在对话中搜索消息
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> searchConversationMessages(Long userId, Long contactUserId, String keyword) {
        // 验证联系人是否存在
        userRepository.findById(contactUserId)
                .orElseThrow(() -> new BusinessException("联系人不存在"));

        List<Message> messages = messageRepository.findConversationMessagesByKeyword(userId, contactUserId, keyword);
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 删除对话
     */
    public void deleteConversation(Long userId, Long contactUserId) {
        // 验证联系人是否存在
        userRepository.findById(contactUserId)
                .orElseThrow(() -> new BusinessException("联系人不存在"));

        messageRepository.deleteConversationMessages(userId, contactUserId);
    }

    /**
     * 统计用户消息数量
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getUserMessageStats(Long userId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("sent", messageRepository.countByFromUserId(userId));
        stats.put("received", messageRepository.countByToUserId(userId));
        stats.put("unread", messageRepository.countByToUserIdAndIsRead(userId, 0));
        return stats;
    }

    /**
     * 转换为响应DTO
     */
    private MessageResponse convertToResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setFromUserId(message.getFromUserId());
        response.setToUserId(message.getToUserId());
        response.setProductId(message.getProductId());
        response.setContent(message.getContent());
        response.setType(message.getType());
        response.setTypeText(getTypeText(message.getType()));
        response.setIsRead(message.getIsRead());
        response.setCreateTime(message.getCreateTime());
        response.setReadTime(message.getReadTime());
        
        // 设置已读状态
        response.setIsUnread(message.getIsRead() == 0);
        response.setReadStatus(getReadStatusText(message.getIsRead(), message.getReadTime()));

        // 设置发送者信息
        if (message.getFromUser() != null) {
            response.setFromUsername(message.getFromUser().getUsername());
            response.setFromNickname(message.getFromUser().getRealName());
            response.setFromAvatar(message.getFromUser().getAvatar());
        } else {
            userRepository.findById(message.getFromUserId()).ifPresent(fromUser -> {
                response.setFromUsername(fromUser.getUsername());
                response.setFromNickname(fromUser.getRealName());
                response.setFromAvatar(fromUser.getAvatar());
            });
        }

        // 设置接收者信息
        if (message.getToUser() != null) {
            response.setToUsername(message.getToUser().getUsername());
            response.setToNickname(message.getToUser().getRealName());
            response.setToAvatar(message.getToUser().getAvatar());
        } else {
            userRepository.findById(message.getToUserId()).ifPresent(toUser -> {
                response.setToUsername(toUser.getUsername());
                response.setToNickname(toUser.getRealName());
                response.setToAvatar(toUser.getAvatar());
            });
        }

        // 设置商品信息
        if (message.getProductId() != null) {
            if (message.getProduct() != null) {
                response.setProductTitle(message.getProduct().getTitle());
                response.setProductImage(message.getProduct().getImages());
            } else {
                productRepository.findById(message.getProductId()).ifPresent(product -> {
                    response.setProductTitle(product.getTitle());
                    response.setProductImage(product.getImages());
                });
            }
        }

        return response;
    }

    /**
     * 获取消息类型文本
     */
    private String getTypeText(Integer type) {
        switch (type) {
            case 1:
                return "文本";
            case 2:
                return "图片";
            default:
                return "未知类型";
        }
    }

    /**
     * 获取已读状态文本
     */
    private String getReadStatusText(Integer isRead, LocalDateTime readTime) {
        if (isRead == 0) {
            return "未读";
        } else if (readTime != null) {
            return "已读";
        } else {
            return "已发送";
        }
    }
}