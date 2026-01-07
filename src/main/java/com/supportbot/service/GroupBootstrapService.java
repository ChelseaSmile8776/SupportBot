package com.supportbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.supportbot.domain.AdminGroup;
import com.supportbot.domain.GroupAdmin;
import com.supportbot.domain.enums.AdminRole;
import com.supportbot.repo.AdminGroupRepository;
import com.supportbot.repo.GroupAdminRepository;
import com.supportbot.telegram.TelegramApiClient;
import com.supportbot.telegram.TelegramJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GroupBootstrapService {
    private final AdminGroupRepository groups;
    private final GroupAdminRepository groupAdmins;
    private final TelegramApiClient api;
    private final CodeGenerator codeGen;
    private final String botUsername;

    public GroupBootstrapService(AdminGroupRepository groups,
                                 GroupAdminRepository groupAdmins,
                                 TelegramApiClient api,
                                 CodeGenerator codeGen,
                                 @Value("${telegram.bot-username}") String botUsername) {
        this.groups = groups;
        this.groupAdmins = groupAdmins;
        this.api = api;
        this.codeGen = codeGen;
        this.botUsername = botUsername;
    }

    public void onMyChatMember(JsonNode myChatMember) {
        var chat = TelegramJson.obj(myChatMember, "chat");
        var from = TelegramJson.obj(myChatMember, "from");
        var newChatMember = TelegramJson.obj(myChatMember, "new_chat_member");

        Long chatId = TelegramJson.longOrNull(chat, "id");
        String chatType = TelegramJson.textOrNull(chat, "type");
        boolean isForum = Optional.ofNullable(chat).map(c -> c.get("is_forum")).map(JsonNode::asBoolean).orElse(false);

        Long actorUserId = TelegramJson.longOrNull(from, "id");

        String status = TelegramJson.obj(newChatMember, "status") != null
                ? TelegramJson.textOrNull(newChatMember, "status")
                : null;

        if (chatId == null || actorUserId == null) return;
        if (!"supergroup".equals(chatType)) return;

        if (!"administrator".equals(status) && !"member".equals(status)) return;

        if ("member".equals(status)) {
            api.sendMessage(actorUserId, null,
                    "Я добавлен в группу ✅\n" +
                            "Теперь сделай меня админом и дай права (минимум):\n" +
                            "• manage_topics\n• delete_messages\n• pin_messages\n• edit_messages\n\n" +
                            "После этого я создам служебные топики и пришлю ссылку для клиентов.",
                    null).block();
            return;
        }

        if (!"administrator".equals(status)) return;

        if (!isForum) {
            api.sendMessage(actorUserId, null,
                    "⚠️ В группе должны быть включены <b>темы (Topics)</b> — это должна быть форум‑супергруппа.\n" +
                            "Включи Topics и добавь бота ещё раз админом 🙏",
                    null).block();
            return;
        }

        AdminGroup g = groups.findByChatId(chatId).orElseGet(() -> {
            AdminGroup ng = new AdminGroup();
            ng.setChatId(chatId);
            ng.setTitle(TelegramJson.textOrNull(chat, "title"));
            ng.setOwnerTelegramUserId(actorUserId);
            ng.setPublicCode(codeGen.newPublicCode(10)); // коротко, чтобы уложиться в лимиты start-параметра
            return groups.save(ng);
        });

        // 3 постоянных топика
        if (g.getAdminChatTopicThreadId() == null) {
            Integer t = extractThreadId(api.createForumTopic(g.getChatId(), "💬 Чат админов").block());
            g.setAdminChatTopicThreadId(t);
        }
        if (g.getAdminsTopicThreadId() == null) {
            Integer t = extractThreadId(api.createForumTopic(g.getChatId(), "👮 Админы").block());
            g.setAdminsTopicThreadId(t);
        }
        if (g.getStatsTopicThreadId() == null) {
            Integer t = extractThreadId(api.createForumTopic(g.getChatId(), "📊 Статистика").block());
            g.setStatsTopicThreadId(t);
        }

        groupAdmins.findByAdminGroupIdAndTelegramUserId(g.getId(), actorUserId).orElseGet(() -> {
            GroupAdmin a = new GroupAdmin();
            a.setAdminGroup(g);
            a.setTelegramUserId(actorUserId);
            a.setRole(AdminRole.OWNER);
            return groupAdmins.save(a);
        });

        groups.save(g);

        String clientLink = "https://t.me/" + botUsername + "?start=" + g.getPublicCode();
        String successMessage = "✅ <b>Готово! Бот настроен.</b>\n\n" +
                "🔗 <b>Ссылка для клиентов</b>:\n" +
                clientLink + "\n\n" +
                "⚙️ Убедитесь, что у бота есть права:\n" +
                "• manage_topics\n• delete_messages\n• pin_messages\n• edit_messages";

        api.sendMessage(actorUserId, null, successMessage, null).subscribe();

        api.sendMessage(g.getChatId(), g.getAdminChatTopicThreadId(), successMessage, null)
                .onErrorResume(e -> reactor.core.publisher.Mono.empty())
                .subscribe();
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
}