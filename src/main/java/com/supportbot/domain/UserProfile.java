package com.supportbot.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_user_id", nullable = false, unique = true)
    private Long telegramUserId;

    @Column(name = "username")
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_admin_group_id")
    private AdminGroup activeAdminGroup;

    @Column(name = "last_menu_message_id")
    private Integer lastMenuMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pending_switch_admin_group_id")
    private AdminGroup pendingSwitchAdminGroup;

    @Column(name = "pending_switch_until")
    private OffsetDateTime pendingSwitchUntil;

    public Long getId() { return id; }
    public Long getTelegramUserId() { return telegramUserId; }
    public void setTelegramUserId(Long telegramUserId) { this.telegramUserId = telegramUserId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public AdminGroup getActiveAdminGroup() { return activeAdminGroup; }
    public void setActiveAdminGroup(AdminGroup activeAdminGroup) { this.activeAdminGroup = activeAdminGroup; }
    public Integer getLastMenuMessageId() { return lastMenuMessageId; }
    public void setLastMenuMessageId(Integer lastMenuMessageId) { this.lastMenuMessageId = lastMenuMessageId; }
    public AdminGroup getPendingSwitchAdminGroup() { return pendingSwitchAdminGroup; }
    public void setPendingSwitchAdminGroup(AdminGroup g) { this.pendingSwitchAdminGroup = g; }
    public OffsetDateTime getPendingSwitchUntil() { return pendingSwitchUntil; }
    public void setPendingSwitchUntil(OffsetDateTime t) { this.pendingSwitchUntil = t; }
}