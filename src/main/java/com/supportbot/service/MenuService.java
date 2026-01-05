package com.supportbot.service;

import com.supportbot.domain.UserProfile;
import com.supportbot.repo.TicketRepository;
import com.supportbot.telegram.TelegramApiClient;
import com.supportbot.telegram.TelegramUi;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportbot.repo.UserProfileRepository;

import java.util.List;
import java.util.Map;

@Service
public class MenuService {
    private final TelegramApiClient api;
    private final TicketRepository tickets;
    private final UserProfileRepository users;
    private final ObjectMapper om = new ObjectMapper();

    public MenuService(TelegramApiClient api, TicketRepository tickets, UserProfileRepository users) {
        this.api = api;
        this.tickets = tickets;
        this.users = users;
    }

    public void showMainMenu(UserProfile user) {
        if (user.getLastMenuMessageId() != null) {
            api.deleteMessage(user.getTelegramUserId(), user.getLastMenuMessageId()).onErrorResume(e -> reactor.core.publisher.Mono.empty()).block();
        }

        String supportLine = (user.getActiveAdminGroup() == null)
                ? "🏢 Активная поддержка: <b>не выбрана</b>\nОткрой ссылку поддержки или нажми «Ввести код»."
                : "🏢 Активная поддержка: <b>" + safe(user.getActiveAdminGroup().getTitle()) + "</b>";

        var kb = TelegramUi.inlineKeyboard(TelegramUi.rows(
                TelegramUi.row(
                        TelegramUi.btn("➕ Создать тикет", "MENU:CREATE"),
                        TelegramUi.btn("🎫 Мои тикеты", "MENU:MY")
                ),
                TelegramUi.row(
                        TelegramUi.btn("🏢 Мои поддержки", "MENU:SUPPORTS"),
                        TelegramUi.btn("🔁 Ввести код", "MENU:CODE")
                )
        ));

        String text = "Привет! 👋\n\n" +
                "Это бесплатный бот техподдержки/обратной связи.\n\n" +
                supportLine + "\n\n" +
                "Выбирай действие ниже 👇";

        var resp = api.sendMessage(user.getTelegramUserId(), null, text, kb).block();

        try {
            if (resp != null) {
                var node = om.readTree(resp);
                int messageId = node.path("result").path("message_id").asInt(0);
                if (messageId != 0) {
                    user.setLastMenuMessageId(messageId);
                    users.save(user);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void showMyTickets(UserProfile user) {
        var list = tickets.findTop10ByClientTelegramUserIdOrderByIdDesc(user.getTelegramUserId());

        StringBuilder sb = new StringBuilder();
        sb.append("🎫 <b>Ваши последние тикеты</b>\n\n");
        if (list.isEmpty()) {
            sb.append("Пока тикетов нет.\n");
        } else {
            for (var t : list) {
                sb.append("• #").append(t.getId())
                        .append(" — ").append(t.getStatus())
                        .append(" — ").append(safe(t.getAdminGroup().getTitle()))
                        .append("\n");
            }
        }

        var kb = TelegramUi.inlineKeyboard(TelegramUi.rows(
                TelegramUi.row(TelegramUi.btn("⬅️ Назад", "MENU:BACK"))
        ));

        api.sendMessage(user.getTelegramUserId(), null, sb.toString(), kb).block();
    }

    private String safe(String s) {
        if (s == null) return "—";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}