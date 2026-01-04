package com.supportbot.domain;

import com.supportbot.domain.enums.AdminRole;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "group_admins",
        uniqueConstraints = @UniqueConstraint(columnNames = {"admin_group_id", "telegram_user_id"}))
public class GroupAdmin {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_group_id", nullable = false)
    private AdminGroup adminGroup;

    @Column(name = "telegram_user_id", nullable = false)
    private Long telegramUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private AdminRole role;

    @Column(name = "rating_avg")
    private BigDecimal ratingAvg;

    @Column(name = "rating_count", nullable = false)
    private int ratingCount;

    public Long getId() { return id; }
    public AdminGroup getAdminGroup() { return adminGroup; }
    public void setAdminGroup(AdminGroup adminGroup) { this.adminGroup = adminGroup; }
    public Long getTelegramUserId() { return telegramUserId; }
    public void setTelegramUserId(Long telegramUserId) { this.telegramUserId = telegramUserId; }
    public AdminRole getRole() { return role; }
    public void setRole(AdminRole role) { this.role = role; }
    public BigDecimal getRatingAvg() { return ratingAvg; }
    public void setRatingAvg(BigDecimal ratingAvg) { this.ratingAvg = ratingAvg; }
    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
}