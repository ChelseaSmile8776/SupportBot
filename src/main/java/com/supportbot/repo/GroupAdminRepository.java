package com.supportbot.repo;

import com.supportbot.domain.GroupAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupAdminRepository extends JpaRepository<GroupAdmin, Long> {
    Optional<GroupAdmin> findByAdminGroupIdAndTelegramUserId(Long adminGroupId, Long telegramUserId);
    List<GroupAdmin> findByAdminGroupIdOrderByRatingAvgDesc(Long adminGroupId);
}