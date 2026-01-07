package com.supportbot.repo;

import com.supportbot.domain.AdminGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminGroupRepository extends JpaRepository<AdminGroup, Long> {
    Optional<AdminGroup> findByChatId(Long chatId);
    Optional<AdminGroup> findByPublicCode(String publicCode);

    @Query("SELECT g.title FROM UserProfile u JOIN u.activeAdminGroup g WHERE u.id = :userId")
    String findActiveGroupTitleByUserId(@Param("userId") Long userId);
}