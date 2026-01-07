package com.supportbot.repo;

import com.supportbot.domain.AdminGroup;
import com.supportbot.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByTelegramUserId(Long telegramUserId);

    @Modifying
    @Query("UPDATE UserProfile u SET u.activeAdminGroup = :group WHERE u.id = :userId")
    void updateActiveGroup(@Param("userId") Long userId, @Param("group") AdminGroup group);
}