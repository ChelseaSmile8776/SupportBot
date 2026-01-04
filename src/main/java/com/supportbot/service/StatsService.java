package com.supportbot.service;

import com.supportbot.domain.AdminGroup;
import com.supportbot.domain.enums.TicketStatus;
import com.supportbot.repo.TicketRepository;
import org.springframework.stereotype.Service;

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
        // MVP без фильтра по дате (добавим во втором проходе через created_at/closed_at + запросы)
        return "📅 <b>Статистика за сутки</b>\n\n" +
                "MVP: фильтр по времени добавим следующим шагом.\n";
    }
}