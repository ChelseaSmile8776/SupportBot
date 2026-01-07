package com.supportbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportbot.domain.UserProfile;
import com.supportbot.repo.GroupAdminRepository;
import com.supportbot.repo.SupportMembershipRepository;
import com.supportbot.repo.TicketRepository;
import com.supportbot.repo.UserProfileRepository;
import com.supportbot.telegram.TelegramApiClient;
import com.supportbot.telegram.TelegramUi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//@Transactional
@Service
public class MenuService {
    private final TelegramApiClient api;
    private final TicketRepository tickets;
    private final UserProfileRepository users;
    private final SupportMembershipRepository memberships;
    private final GroupAdminRepository groupAdmins;
    private final ObjectMapper om = new ObjectMapper();

    public MenuService(TelegramApiClient api,
                       TicketRepository tickets,
                       UserProfileRepository users,
                       SupportMembershipRepository memberships,
                       GroupAdminRepository groupAdmins) {
        this.api = api;
        this.tickets = tickets;
        this.users = users;
        this.memberships = memberships;
        this.groupAdmins = groupAdmins;
    }

    public void showMainMenu(UserProfile user) {
//        if (user.getPendingSwitchAdminGroup() == null && user.getPendingSwitchUntil() != null) {
//            user.setPendingSwitchUntil(null);
//            users.save(user);
//        }

        if (user.getLastMenuMessageId() != null) {
            api.deleteMessage(user.getTelegramUserId(), user.getLastMenuMessageId())
                    .onErrorResume(e -> reactor.core.publisher.Mono.empty()).block();
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
                ),
                TelegramUi.row(
                        TelegramUi.btn("☎\uFE0F Созданные поддержки", "MENU:ADMIN")
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

    public void showEnterCode(UserProfile user) {
        user.setPendingSwitchAdminGroup(null);
        user.setPendingSwitchUntil(OffsetDateTime.now().plusMinutes(5));
        users.save(user);

        api.sendMessage(user.getTelegramUserId(), null,
                "✍️ <b>Введите код поддержки</b>\n\n" +
                        "Отправьте мне код группы (например <code>start-xyz</code>), который вам дал администратор.",
                TelegramUi.inlineKeyboard(TelegramUi.rows(
                        TelegramUi.row(TelegramUi.btn("⬅️ Отмена", "MENU:BACK"))
                ))
        ).block();
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

    public void showMySupports(UserProfile user) {
        var list = memberships.findTop10ByUserProfileOrderByLastUsedAtDesc(user);

        if (list.isEmpty()) {
            api.sendMessage(user.getTelegramUserId(), null,
                    "🤷 Вы пока не подписаны ни на одну поддержку.",
                    TelegramUi.inlineKeyboard(TelegramUi.rows(
                            TelegramUi.row(TelegramUi.btn("⬅️ Назад", "MENU:BACK"))
                    ))
            ).block();
            return;
        }

        List<List<Map<String, Object>>> rows = new ArrayList<>();

        for (var m : list) {
            String mark = (user.getActiveAdminGroup() != null && m.getAdminGroup().getId().equals(user.getActiveAdminGroup().getId()))
                    ? "✅ " : "";
            rows.add(TelegramUi.row(
                    TelegramUi.btn(mark + safe(m.getAdminGroup().getTitle()), "SW:" + m.getAdminGroup().getId())
            ));
        }
        rows.add(TelegramUi.row(TelegramUi.btn("⬅️ Назад", "MENU:BACK")));

        api.sendMessage(user.getTelegramUserId(), null,
                "🏢 <b>Ваши подписки</b>\nНажмите на группу, чтобы сделать её активной (для создания тикетов):",
                TelegramUi.inlineKeyboard(rows)
        ).block();
    }

    public void showAdminProjects(UserProfile user) {
        var admins = groupAdmins.findByTelegramUserId(user.getTelegramUserId());

        if (admins.isEmpty()) {
            api.sendMessage(user.getTelegramUserId(), null,
                    "🤷 Вы не являетесь администратором ни в одной группе.\nЧтобы создать свою поддержку — просто добавьте бота в вашу группу.",
                    TelegramUi.inlineKeyboard(TelegramUi.rows(
                            TelegramUi.row(TelegramUi.btn("⬅️ Назад", "MENU:BACK"))
                    ))
            ).block();
            return;
        }

        StringBuilder sb = new StringBuilder("☎\uFE0F <b>Ваши проекты (вы админ)</b>\n\n");
        for (var a : admins) {
            sb.append("• <b>").append(safe(a.getAdminGroup().getTitle())).append("</b>")
                    .append(" (").append(a.getRole()).append(")\n")
                    .append("   🔗 Код для клиентов: <code>").append(a.getAdminGroup().getPublicCode()).append("</code>\n\n");
        }

        api.sendMessage(user.getTelegramUserId(), null, sb.toString(),
                TelegramUi.inlineKeyboard(TelegramUi.rows(
                        TelegramUi.row(TelegramUi.btn("⬅️ Назад", "MENU:BACK"))
                ))
        ).block();
    }

    private String safe(String s) {
        if (s == null) return "—";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}