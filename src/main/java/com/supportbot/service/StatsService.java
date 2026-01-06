package com.supportbot.service;

import com.supportbot.domain.AdminGroup;
import com.supportbot.domain.enums.TicketStatus;
import com.supportbot.repo.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class StatsService {
    private final TicketRepository tickets;

    public StatsService(TicketRepository tickets) {
        this.tickets = tickets;
    }

    public String buildAllTime(AdminGroup g) {
        int open = tickets.findByAdminGroupIdAndStatus(g.getId(), TicketStatus.NEW).size()
                + tickets.findByAdminGroupIdAndStatus(g.getId(), TicketStatus.ASSIGNED).size()
                + tickets.findByAdminGroupIdAndStatus(g.getId(), TicketStatus.RESOLVED).size();
        int closed = tickets.findByAdminGroupIdAndStatus(g.getId(), TicketStatus.CLOSED).size();

        return "📊 <b>Статистика за всё время</b>\n\n" +
                "• Открытые/в работе: <b>" + open + "</b>\n" +
                "• Закрытые: <b>" + closed + "</b>\n";
    }

    public String buildDaily(AdminGroup g) {
        OffsetDateTime dayAgo = OffsetDateTime.now().minusHours(24);

        int created = tickets.countByAdminGroupIdAndCreatedAtAfter(g.getId(), dayAgo);
        int closed = tickets.countByAdminGroupIdAndStatusAndClosedAtAfter(g.getId(), TicketStatus.CLOSED, dayAgo);

        return "📅 <b>Статистика за 24 часа</b>\n\n" +
                "• Новых тикетов: <b>" + created + "</b>\n" +
                "• Закрыто тикетов: <b>" + closed + "</b>\n";
    }
}