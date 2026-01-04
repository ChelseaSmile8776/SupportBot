package com.supportbot.domain;

import com.supportbot.domain.enums.TicketCategory;
import com.supportbot.domain.enums.TicketStatus;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_group_id", nullable = false)
    private AdminGroup adminGroup;

    @Column(name = "client_telegram_user_id", nullable = false)
    private Long clientTelegramUserId;

    @Column(name = "assigned_admin_telegram_user_id")
    private Long assignedAdminTelegramUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private TicketCategory category;

    @Column(name = "forum_chat_id", nullable = false)
    private Long forumChatId;

    @Column(name = "message_thread_id")
    private Integer messageThreadId;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    public Long getId() { return id; }
    public AdminGroup getAdminGroup() { return adminGroup; }
    public void setAdminGroup(AdminGroup adminGroup) { this.adminGroup = adminGroup; }
    public Long getClientTelegramUserId() { return clientTelegramUserId; }
    public void setClientTelegramUserId(Long clientTelegramUserId) { this.clientTelegramUserId = clientTelegramUserId; }
    public Long getAssignedAdminTelegramUserId() { return assignedAdminTelegramUserId; }
    public void setAssignedAdminTelegramUserId(Long v) { this.assignedAdminTelegramUserId = v; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public TicketCategory getCategory() { return category; }
    public void setCategory(TicketCategory category) { this.category = category; }
    public Long getForumChatId() { return forumChatId; }
    public void setForumChatId(Long forumChatId) { this.forumChatId = forumChatId; }
    public Integer getMessageThreadId() { return messageThreadId; }
    public void setMessageThreadId(Integer messageThreadId) { this.messageThreadId = messageThreadId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public OffsetDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(OffsetDateTime closedAt) { this.closedAt = closedAt; }
}