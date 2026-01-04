package com.supportbot.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "support_memberships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_profile_id", "admin_group_id"}))
public class SupportMembership {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_group_id", nullable = false)
    private AdminGroup adminGroup;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "last_used_at", nullable = false)
    private OffsetDateTime lastUsedAt = OffsetDateTime.now();

    public Long getId() { return id; }
    public UserProfile getUserProfile() { return userProfile; }
    public void setUserProfile(UserProfile userProfile) { this.userProfile = userProfile; }
    public AdminGroup getAdminGroup() { return adminGroup; }
    public void setAdminGroup(AdminGroup adminGroup) { this.adminGroup = adminGroup; }
    public OffsetDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(OffsetDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }
}