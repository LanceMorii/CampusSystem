package com.example.campussystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 消息实体类
 */
@Entity
@Table(name = "messages")
@EntityListeners(AuditingEntityListener.class)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_user_id", nullable = false)
    @NotNull(message = "发送者ID不能为空")
    private Long fromUserId;

    @Column(name = "to_user_id", nullable = false)
    @NotNull(message = "接收者ID不能为空")
    private Long toUserId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1000, message = "消息内容长度不能超过1000个字符")
    private String content;

    @Column(name = "type")
    private Integer type = 1; // 1文本,2图片

    @Column(name = "is_read")
    private Integer isRead = 0; // 0未读,1已读

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    // 多对一关系：消息的发送者
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", insertable = false, updatable = false)
    private User fromUser;

    // 多对一关系：消息的接收者
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", insertable = false, updatable = false)
    private User toUser;

    // 多对一关系：消息关联的商品
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    // 构造函数
    public Message() {}

    public Message(Long fromUserId, Long toUserId, String content) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.content = content;
    }

    public Message(Long fromUserId, Long toUserId, Long productId, String content) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.productId = productId;
        this.content = content;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public User getFromUser() {
        return fromUser;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }

    public User getToUser() {
        return toUser;
    }

    public void setToUser(User toUser) {
        this.toUser = toUser;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    // 业务方法
    public boolean isTextMessage() {
        return type == 1;
    }

    public boolean isImageMessage() {
        return type == 2;
    }

    public boolean isUnread() {
        return isRead == 0;
    }

    public void markAsRead() {
        this.isRead = 1;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", fromUserId=" + fromUserId +
                ", toUserId=" + toUserId +
                ", productId=" + productId +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", isRead=" + isRead +
                ", createTime=" + createTime +
                '}';
    }
}