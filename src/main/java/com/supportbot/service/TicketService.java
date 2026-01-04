package com.supportbot.service;

import com.supportbot.domain.AdminGroup;
import com.supportbot.domain.Ticket;
import com.supportbot.domain.UserProfile;
import com.supportbot.domain.enums.TicketCategory;
import com.supportbot.domain.enums.TicketStatus;
import com.supportbot.repo.GroupAdminRepository;
import com.supportbot.repo.TicketRepository;
import com.supportbot.telegram.TelegramApiClient;
import com.supportbot.telegram.TelegramUi;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class TicketService {
    private final TicketRepository tickets;
    private final GroupAdminRepository groupAdmins;
    private final TelegramApiClient api;

    public TicketService(TicketRepository tickets, GroupAdminRepository groupAdmins, TelegramApiClient api) {
        this.tickets = tickets;
        this.groupAdmins = groupAdmins;
        this.api = api;
    }

    public void createTicket(UserProfile user) {
        AdminGroup g = user.getActiveAdminGroup();
        if (g == null) {
            api.sendMessage(user.getTelegramUserId(), null,
                    "🏢 Сначала выбери поддержку.\nОткрой ссылку поддержки или введи код (кнопка в меню).",
                    null).block();
            return;
        }

        Ticket t = new Ticket();
        t.setAdminGroup(g);
        t.setClientTelegramUserId(user.getTelegramUserId());
        t.setStatus(TicketStatus.NEW);
        t.setCategory(TicketCategory.SUPPORT);
        t.setForumChatId(g.getChatId());
        t = tickets.save(t);

        String topicName = "🎫 Тикет #" + t.getId();
        Integer threadId = extractThreadId(api.createForumTopic(g.getChatId(), topicName).block());
        t.setMessageThreadId(threadId);
        t = tickets.save(t);

        var kb = TelegramUi.inlineKeyboard(TelegramUi.rows(
                TelegramUi.row(
                        TelegramUi.btn("✅ Взять", "T:TAKE:" + t.getId()),
                        TelegramUi.btn("🧾 Закрыть", "T:CLOSE:" + t.getId())
                )
        ));

        api.sendMessage(g.getChatId(), threadId,
                "🆕 <b>Новый тикет #" + t.getId() + "</b>\n" +
                        "Клиент: <code>" + user.getTelegramUserId() + "</code>\n\n" +
                        "Админы, нажмите «Взять», чтобы начать работу 👇",
                kb).block();

        api.sendMessage(user.getTelegramUserId(), null,
                "✅ Тикет создан: <b>#" + t.getId() + "</b>\n" +
                        "Ожидай ответа администратора 🙌\n\n" +
                        "Можешь писать сюда сообщения — они будут отправлены в тикет.",
                null).block();
    }

    public void onClientMessage(long clientUserId, String text) {
        var last = tickets.findTop10ByClientTelegramUserIdOrderByIdDesc(clientUserId)
                .stream()
                .filter(t -> t.getStatus() != TicketStatus.CLOSED)
                .findFirst()
                .orElse(null);

        if (last == null || last.getMessageThreadId() == null) {
            api.sendMessage(clientUserId, null,
                    "ℹ️ Нет активного тикета.\nНажми «➕ Создать тикет» в меню.",
                    null).block();
            return;
        }

        api.sendMessage(last.getForumChatId(), last.getMessageThreadId(),
                "👤 <b>Клиент</b>: " + escape(text),
                null).block();
    }

    public void onAdminMessage(long forumChatId, int messageThreadId, long fromUserId, String text) {
        var ticketOpt = tickets.findByForumChatIdAndMessageThreadId(forumChatId, messageThreadId);
        if (ticketOpt.isEmpty()) return;
        var t = ticketOpt.get();

        if (t.getAssignedAdminTelegramUserId() != null && !t.getAssignedAdminTelegramUserId().equals(fromUserId)) {
            return;
        }

        api.sendMessage(t.getClientTelegramUserId(), null,
                "👨‍💻 <b>Админ</b>: " + escape(text),
                null).block();
    }

    public void takeTicket(long adminUserId, long ticketId) {
        var t = tickets.findById(ticketId).orElse(null);
        if (t == null || t.getStatus() == TicketStatus.CLOSED) return;

        if (t.getAssignedAdminTelegramUserId() == null) {
            t.setAssignedAdminTelegramUserId(adminUserId);
            t.setStatus(TicketStatus.ASSIGNED);
            tickets.save(t);

            api.sendMessage(t.getForumChatId(), t.getMessageThreadId(),
                    "✅ Тикет взят админом: <code>" + adminUserId + "</code>",
                    null).block();

            api.sendMessage(t.getClientTelegramUserId(), null,
                    "✅ Ваш тикет <b>#" + t.getId() + "</b> взят в работу администратором 👨‍💻",
                    null).block();
        }
    }

    public void closeTicketAskRating(long adminUserId, long ticketId) {
        var t = tickets.findById(ticketId).orElse(null);
        if (t == null || t.getStatus() == TicketStatus.CLOSED) return;

        if (t.getAssignedAdminTelegramUserId() == null) {
            t.setAssignedAdminTelegramUserId(adminUserId);
        }
        t.setStatus(TicketStatus.RESOLVED);
        tickets.save(t);

        var kb = TelegramUi.inlineKeyboard(TelegramUi.rows(
                TelegramUi.row(
                        TelegramUi.btn("1", "T:RATE:" + t.getId() + ":1"),
                        TelegramUi.btn("2", "T:RATE:" + t.getId() + ":2"),
                        TelegramUi.btn("3", "T:RATE:" + t.getId() + ":3"),
                        TelegramUi.btn("4", "T:RATE:" + t.getId() + ":4"),
                        TelegramUi.btn("5", "T:RATE:" + t.getId() + ":5")
                )
        ));

        api.sendMessage(t.getClientTelegramUserId(), null,
                "🧾 Тикет <b>#" + t.getId() + "</b> закрыт.\n" +
                        "Поставь оценку от 1 до 5 ⭐ (без оценки тикет не закрывается полностью):",
                kb).block();

        api.sendMessage(t.getForumChatId(), t.getMessageThreadId(),
                "🧾 Запрошена оценка у клиента. Ждём ⭐",
                null).block();
    }

    public void rateAndFinish(long clientUserId, long ticketId, int rating) {
        var t = tickets.findById(ticketId).orElse(null);
        if (t == null) return;
        if (!t.getClientTelegramUserId().equals(clientUserId)) return;
        if (t.getStatus() == TicketStatus.CLOSED) return;

        t.setRating(rating);
        t.setStatus(TicketStatus.CLOSED);
        t.setClosedAt(OffsetDateTime.now());
        tickets.save(t);

        if (t.getAssignedAdminTelegramUserId() != null) {
            var ga = groupAdmins.findByAdminGroupIdAndTelegramUserId(t.getAdminGroup().getId(), t.getAssignedAdminTelegramUserId()).orElse(null);
            if (ga != null) {
                int cnt = ga.getRatingCount();
                double oldAvg = ga.getRatingAvg() == null ? 0.0 : ga.getRatingAvg().doubleValue();
                double newAvg = (cnt == 0) ? rating : ((oldAvg * cnt) + rating) / (cnt + 1);
                ga.setRatingCount(cnt + 1);
                ga.setRatingAvg(java.math.BigDecimal.valueOf(newAvg).setScale(2, java.math.RoundingMode.HALF_UP));
                groupAdmins.save(ga);
            }
        }

        api.sendMessage(clientUserId, null,
                "Спасибо за оценку! ⭐\nТикет <b>#" + t.getId() + "</b> закрыт ✅",
                null).block();

        if (t.getMessageThreadId() != null) {
            api.deleteForumTopic(t.getForumChatId(), t.getMessageThreadId()).onErrorResume(e -> reactor.core.publisher.Mono.empty()).block();
        }
    }

    private Integer extractThreadId(String json) {
        try {
            var node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
            var mtid = node.path("result").path("message_thread_id");
            return mtid.isMissingNode() ? null : mtid.asInt();
        } catch (Exception e) {
            return null;
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}