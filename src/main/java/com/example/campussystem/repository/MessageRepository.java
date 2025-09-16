package com.example.campussystem.repository;

import com.example.campussystem.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息数据访问接口
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 查找两个用户之间的对话消息
     */
    @Query("SELECT m FROM Message m WHERE (m.fromUserId = :userId1 AND m.toUserId = :userId2) OR (m.fromUserId = :userId2 AND m.toUserId = :userId1) ORDER BY m.createTime ASC")
    List<Message> findConversationMessages(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 分页查找两个用户之间的对话消息
     */
    @Query("SELECT m FROM Message m WHERE (m.fromUserId = :userId1 AND m.toUserId = :userId2) OR (m.fromUserId = :userId2 AND m.toUserId = :userId1) ORDER BY m.createTime DESC")
    Page<Message> findConversationMessages(@Param("userId1") Long userId1, @Param("userId2") Long userId2, Pageable pageable);

    /**
     * 查找用户发送的消息
     */
    List<Message> findByFromUserIdOrderByCreateTimeDesc(Long fromUserId);

    /**
     * 查找用户接收的消息
     */
    List<Message> findByToUserIdOrderByCreateTimeDesc(Long toUserId);

    /**
     * 查找用户的所有消息（发送和接收）
     */
    @Query("SELECT m FROM Message m WHERE m.fromUserId = :userId OR m.toUserId = :userId ORDER BY m.createTime DESC")
    List<Message> findUserMessages(@Param("userId") Long userId);

    /**
     * 分页查找用户的所有消息
     */
    @Query("SELECT m FROM Message m WHERE m.fromUserId = :userId OR m.toUserId = :userId ORDER BY m.createTime DESC")
    Page<Message> findUserMessages(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查找用户未读消息
     */
    List<Message> findByToUserIdAndIsReadOrderByCreateTimeDesc(Long toUserId, Integer isRead);

    /**
     * 查找关于特定商品的消息
     */
    List<Message> findByProductIdOrderByCreateTimeDesc(Long productId);

    /**
     * 查找用户关于特定商品的对话消息
     */
    @Query("SELECT m FROM Message m WHERE m.productId = :productId AND ((m.fromUserId = :userId1 AND m.toUserId = :userId2) OR (m.fromUserId = :userId2 AND m.toUserId = :userId1)) ORDER BY m.createTime ASC")
    List<Message> findProductConversationMessages(@Param("productId") Long productId, @Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 统计用户未读消息数量
     */
    long countByToUserIdAndIsRead(Long toUserId, Integer isRead);

    /**
     * 统计两个用户之间的消息数量
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE (m.fromUserId = :userId1 AND m.toUserId = :userId2) OR (m.fromUserId = :userId2 AND m.toUserId = :userId1)")
    long countConversationMessages(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 统计用户发送的消息数量
     */
    long countByFromUserId(Long fromUserId);

    /**
     * 统计用户接收的消息数量
     */
    long countByToUserId(Long toUserId);

    /**
     * 批量标记消息为已读
     */
    @Modifying
    @Query("UPDATE Message m SET m.isRead = 1 WHERE m.toUserId = :toUserId AND m.fromUserId = :fromUserId AND m.isRead = 0")
    int markMessagesAsRead(@Param("toUserId") Long toUserId, @Param("fromUserId") Long fromUserId);

    /**
     * 标记单条消息为已读
     */
    @Modifying
    @Query("UPDATE Message m SET m.isRead = 1 WHERE m.id = :messageId AND m.toUserId = :toUserId")
    int markMessageAsRead(@Param("messageId") Long messageId, @Param("toUserId") Long toUserId);

    /**
     * 查找用户的对话列表（最近联系人）
     */
    @Query("SELECT DISTINCT CASE WHEN m.fromUserId = :userId THEN m.toUserId ELSE m.fromUserId END as contactUserId " +
           "FROM Message m WHERE m.fromUserId = :userId OR m.toUserId = :userId " +
           "ORDER BY MAX(m.createTime) DESC")
    List<Long> findUserContacts(@Param("userId") Long userId);

    /**
     * 查找用户与每个联系人的最新消息
     */
    @Query("SELECT m FROM Message m WHERE m.id IN (" +
           "SELECT MAX(m2.id) FROM Message m2 " +
           "WHERE (m2.fromUserId = :userId OR m2.toUserId = :userId) " +
           "GROUP BY CASE WHEN m2.fromUserId = :userId THEN m2.toUserId ELSE m2.fromUserId END" +
           ") ORDER BY m.createTime DESC")
    List<Message> findLatestMessagesWithContacts(@Param("userId") Long userId);

    /**
     * 查找指定时间范围内的消息
     */
    @Query("SELECT m FROM Message m WHERE m.createTime BETWEEN :startTime AND :endTime ORDER BY m.createTime DESC")
    List<Message> findMessagesByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查找包含关键词的消息
     */
    @Query("SELECT m FROM Message m WHERE m.content LIKE %:keyword% ORDER BY m.createTime DESC")
    List<Message> findMessagesByKeyword(@Param("keyword") String keyword);

    /**
     * 查找用户对话中包含关键词的消息
     */
    @Query("SELECT m FROM Message m WHERE ((m.fromUserId = :userId1 AND m.toUserId = :userId2) OR (m.fromUserId = :userId2 AND m.toUserId = :userId1)) AND m.content LIKE %:keyword% ORDER BY m.createTime DESC")
    List<Message> findConversationMessagesByKeyword(@Param("userId1") Long userId1, @Param("userId2") Long userId2, @Param("keyword") String keyword);

    /**
     * 删除指定时间之前的消息
     */
    @Modifying
    @Query("DELETE FROM Message m WHERE m.createTime < :beforeTime")
    int deleteMessagesBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 删除两个用户之间的所有消息
     */
    @Modifying
    @Query("DELETE FROM Message m WHERE (m.fromUserId = :userId1 AND m.toUserId = :userId2) OR (m.fromUserId = :userId2 AND m.toUserId = :userId1)")
    int deleteConversationMessages(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 查找最近的消息
     */
    @Query("SELECT m FROM Message m ORDER BY m.createTime DESC")
    Page<Message> findRecentMessages(Pageable pageable);

    /**
     * 统计指定类型的消息数量
     */
    long countByType(Integer type);

    /**
     * 查找指定类型的消息
     */
    List<Message> findByTypeOrderByCreateTimeDesc(Integer type);
}