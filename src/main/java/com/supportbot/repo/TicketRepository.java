package com.supportbot.repo;

import com.supportbot.domain.Ticket;
import com.supportbot.domain.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findTop10ByClientTelegramUserIdOrderByIdDesc(Long clientTelegramUserId);
    List<Ticket> findByAdminGroupIdAndStatus(Long adminGroupId, TicketStatus status);
    Optional<Ticket> findByForumChatIdAndMessageThreadId(Long forumChatId, Integer messageThreadId);
}