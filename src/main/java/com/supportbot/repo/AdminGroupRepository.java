package com.supportbot.repo;

import com.supportbot.domain.AdminGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminGroupRepository extends JpaRepository<AdminGroup, Long> {
    Optional<AdminGroup> findByChatId(Long chatId);
    Optional<AdminGroup> findByPublicCode(String publicCode);
}