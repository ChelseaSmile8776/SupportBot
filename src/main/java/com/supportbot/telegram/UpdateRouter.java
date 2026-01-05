package com.supportbot.telegram;


import com.fasterxml.jackson.databind.JsonNode;
import com.supportbot.domain.SupportMembership;
import com.supportbot.domain.UserProfile;
import com.supportbot.repo.AdminGroupRepository;
import com.supportbot.repo.SupportMembershipRepository;
import com.supportbot.repo.UserProfileRepository;
import com.supportbot.service.GroupBootstrapService;
import com.supportbot.service.MenuService;
import com.supportbot.service.TicketService;
import com.supportbot.telegram.dto.Update;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class UpdateRouter {
    private final GroupBootstrapService bootstrap;
    private final UserProfileRepository users;
    private final AdminGroupRepository groups;
    private final SupportMembershipRepository memberships;
    private final MenuService menu;
    private final TicketService tickets;
    private final TelegramApiClient api;

    public UpdateRouter(GroupBootstrapService bootstrap,
                        UserProfileRepository users,
                        AdminGroupRepository groups,
                        SupportMembershipRepository memberships,
                        MenuService menu,
                        TicketService tickets,
                        TelegramApiClient api) {
        this.bootstrap = bootstrap;
        this.users = users;
        this.groups = groups;
        this.memberships = memberships;
        this.menu = menu;
        this.tickets = tickets;
        this.api = api;
    }

    public void route(Update u) {
        if (u.my_chat_member() != null) {
            bootstrap.onMyChatMember(u.my_chat_member());
            return;
        }

        if (u.callback_query() != null) {
            onCallback(u.callback_query());
            return;
        }

        if (u.message() != null) {
            onMessage(u.message());
        }
    }

    private void onMessage(JsonNode msg) {
        var chat = TelegramJson.obj(msg, "chat");
        var from = TelegramJson.obj(msg, "from");
        String chatType = TelegramJson.textOrNull(chat, "type");

        Long fromId = TelegramJson.longOrNull(from, "id");
        if (fromId == null) return;

        // сообщения админа в топике
        if (!"private".equals(chatType)) {
            Long chatId = TelegramJson.longOrNull(chat, "id");
            Integer threadId = TelegramJson.intOrNull(msg, "message_thread_id");
            String text = TelegramJson.textOrNull(msg, "text");
            if (chatId != null && threadId != null && text != null && !text.startsWith("/")) {
                tickets.onAdminMessage(chatId, threadId, fromId, text);
            }
            return;
        }

        // private чат
        UserProfile user = ensureUser(from);
        String text = TelegramJson.textOrNull(msg, "text");

        if (text != null && text.startsWith("/start")) {
            String payload = null;
            var parts = text.trim().split("\\s+", 2);
            if (parts.length == 2) payload = parts[1].trim();
            onStart(user, payload);
            return;
        }

        if (text != null && text.startsWith("/menu")) {
            menu.showMainMenu(user);
            return;
        }

        // обычный текст идёт в активный тикет
        if (text != null && !text.isBlank()) {
            tickets.onClientMessage(user.getTelegramUserId(), text);
        }
    }

    private void onStart(UserProfile user, String payload) {
        if (payload == null || payload.isBlank()) {
            menu.showMainMenu(user);
            return;
        }

        var gOpt = groups.findByPublicCode(payload);
        if (gOpt.isEmpty()) {
            api.sendMessage(user.getTelegramUserId(), null,
                    "⚠️ Код поддержки не найден.\nПроверь ссылку или попроси правильную у администратора 🙏",
                    null).block();
            menu.showMainMenu(user);
            return;
        }

        var g = gOpt.get();
        if (user.getActiveAdminGroup() != null && user.getActiveAdminGroup().getId().equals(g.getId())) {
            api.sendMessage(user.getTelegramUserId(), null,
                    "✅ Ты уже в поддержке: <b>" + safe(g.getTitle()) + "</b>",
                    null).block();
            menu.showMainMenu(user);return;
        }

        // Да/Нет
        user.setPendingSwitchAdminGroup(g);
        user.setPendingSwitchUntil(OffsetDateTime.now().plusMinutes(10));
        users.save(user);

        var kb = TelegramUi.inlineKeyboard(TelegramUi.rows(
                TelegramUi.row(
                        TelegramUi.btn("✅ Да", "SW:OK:" + g.getId()),
                        TelegramUi.btn("❌ Нет", "SW:NO")
                )
        ));

        api.sendMessage(user.getTelegramUserId(), null,
                "🔁 Переключиться на поддержку:\n<b>" + safe(g.getTitle()) + "</b> ?",
                kb).block();
    }

    private void onCallback(JsonNode cq) {
        String id = TelegramJson.textOrNull(cq, "id");
        String data = TelegramJson.textOrNull(cq, "data");
        var from = TelegramJson.obj(cq, "from");
        Long fromId = TelegramJson.longOrNull(from, "id");

        if (id == null || data == null || fromId == null) return;

        api.answerCallbackQuery(id, null)
                .onErrorResume(e -> reactor.core.publisher.Mono.empty())
                .subscribe();

        UserProfile user = ensureUser(from);

        if (data.startsWith("SW:")) {
            handleSwitch(user, data);
            return;
        }

        if (data.startsWith("MENU:")) {
            handleMenu(user, data);
            return;
        }

        if (data.startsWith("T:")) {
            handleTicketAction(fromId, data);
        }
    }

    private void handleSwitch(UserProfile user, String data) {
        if ("SW:NO".equals(data)) {
            user.setPendingSwitchAdminGroup(null);
            user.setPendingSwitchUntil(null);
            users.save(user);
            api.sendMessage(user.getTelegramUserId(), null, "Окей, не переключаю 🙌", null).block();
            menu.showMainMenu(user);
            return;
        }

        var parts = data.split(":");
        if (parts.length != 3) return;

        Long gid = Long.valueOf(parts[2]);
        var pending = user.getPendingSwitchAdminGroup();

        var until = user.getPendingSwitchUntil();
        if (until == null || until.isBefore(OffsetDateTime.now())) {
            user.setPendingSwitchAdminGroup(null);
            user.setPendingSwitchUntil(null);
            users.save(user);
            api.sendMessage(user.getTelegramUserId(), null,
                    "⏳ Запрос на переключение устарел. Открой ссылку поддержки ещё раз (/start CODE).",
                    null).block();
            menu.showMainMenu(user);
            return;
        }

        if (pending == null || !pending.getId().equals(gid)) return;

        user.setActiveAdminGroup(pending);
        user.setPendingSwitchAdminGroup(null);
        user.setPendingSwitchUntil(null);
        users.save(user);

        memberships.findByUserProfileIdAndAdminGroupId(user.getId(), pending.getId()).orElseGet(() -> {
            SupportMembership m = new SupportMembership();
            m.setUserProfile(user);
            m.setAdminGroup(pending);
            return memberships.save(m);
        });

        api.sendMessage(user.getTelegramUserId(), null,
                "✅ Переключено!\nТеперь активная поддержка: <b>" + safe(pending.getTitle()) + "</b>",
                null).block();

        menu.showMainMenu(user);
    }

    private void handleMenu(UserProfile user, String data) {
        switch (data) {
            case "MENU:CREATE" -> tickets.createTicket(user);
            case "MENU:MY" -> menu.showMyTickets(user);
            case "MENU:SUPPORTS" -> api.sendMessage(user.getTelegramUserId(), null,
                    "🏢 Мои поддержки (MVP): переключение через ссылку /start CODE.\n" +
                            "Позже сделаем список последних поддержек кнопками.",
                    null).block();
            case "MENU:CONNECT" -> api.sendMessage(user.getTelegramUserId(), null,
                    "👮 <b>Подключить поддержку (для админов)</b>\n\n" +
                            "1) Создай супергруппу и включи <b>Topics</b> (форум).\n" +
                            "2) Добавь @" + "ItsMySupportBot" + " в эту группу.\n" +
                            "3) Сделай бота админом и дай права: manage_topics, delete_messages, pin_messages, edit_messages.\n\n" +
                            "После этого бот сам создаст служебные топики и пришлёт ссылку для клиентов.",
                    null).block();
            case "MENU:CODE" -> api.sendMessage(user.getTelegramUserId(), null,
                    "🔁 Просто открой ссылку поддержки (она выглядит как https://t.me/ItsMySupportBot?start=CODE).\n" +
                            "Если хочешь — пришли сюда CODE, и я добавлю ручной ввод в следующем шаге.",
                    null).block();
            case "MENU:BACK" -> menu.showMainMenu(user);
            default -> {}
        }
    }

    private void handleTicketAction(long actorUserId, String data) {
        var parts = data.split(":");
        if (parts.length < 3) return;

        String action = parts[1];
        long ticketId = Long.parseLong(parts[2]);

        switch (action) {
            case "TAKE" -> tickets.takeTicket(actorUserId, ticketId);
            case "CLOSE" -> tickets.closeTicketAskRating(actorUserId, ticketId);
            case "RATE" -> {
                if (parts.length != 4) return;
                int rating = Integer.parseInt(parts[3]);
                tickets.rateAndFinish(actorUserId, ticketId, rating);
            }
            default -> {}
        }
    }

    private UserProfile ensureUser(JsonNode from) {
        Long id = TelegramJson.longOrNull(from, "id");if (id == null) throw new IllegalStateException("No from.id");

        return users.findByTelegramUserId(id).orElseGet(() -> {
            UserProfile u = new UserProfile();
            u.setTelegramUserId(id);
            u.setUsername(TelegramJson.textOrNull(from, "username"));
            u.setFirstName(TelegramJson.textOrNull(from, "first_name"));
            return users.save(u);
        });
    }

    private String safe(String s) {
        if (s == null) return "—";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}