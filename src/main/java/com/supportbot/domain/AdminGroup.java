package com.supportbot.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "admin_groups")
public class AdminGroup {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false, unique = true)
    private Long chatId;

    @Column(name = "title")
    private String title;

    @Column(name = "owner_telegram_user_id")
    private Long ownerTelegramUserId;

    @Column(name = "public_code", nullable = false, unique = true, length = 32)
    private String publicCode;

    @Column(name = "admin_chat_topic_thread_id")
    private Integer adminChatTopicThreadId;

    @Column(name = "admins_topic_thread_id")
    private Integer adminsTopicThreadId;

    @Column(name = "stats_topic_thread_id")
    private Integer statsTopicThreadId;

    @Column(name = "pinned_admins_message_id")
    private Integer pinnedAdminsMessageId;

    @Column(name = "pinned_stats_message_id")
    private Integer pinnedStatsMessageId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Long getId() { return id; }
    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getOwnerTelegramUserId() { return ownerTelegramUserId; }
    public void setOwnerTelegramUserId(Long ownerTelegramUserId) { this.ownerTelegramUserId = ownerTelegramUserId; }
    public String getPublicCode() { return publicCode; }
    public void setPublicCode(String publicCode) { this.publicCode = publicCode; }
    public Integer getAdminChatTopicThreadId() { return adminChatTopicThreadId; }
    public void setAdminChatTopicThreadId(Integer v) { this.adminChatTopicThreadId = v; }
    public Integer getAdminsTopicThreadId() { return adminsTopicThreadId; }
    public void setAdminsTopicThreadId(Integer v) { this.adminsTopicThreadId = v; }
    public Integer getStatsTopicThreadId() { return statsTopicThreadId; }
    public void setStatsTopicThreadId(Integer v) { this.statsTopicThreadId = v; }
    public Integer getPinnedAdminsMessageId() { return pinnedAdminsMessageId; }
    public void setPinnedAdminsMessageId(Integer v) { this.pinnedAdminsMessageId = v; }
    public Integer getPinnedStatsMessageId() { return pinnedStatsMessageId; }
    public void setPinnedStatsMessageId(Integer v) { this.pinnedStatsMessageId = v; }
}