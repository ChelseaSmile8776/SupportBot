package com.supportbot.service;

import com.supportbot.repo.AdminGroupRepository;
import com.supportbot.telegram.TelegramApiClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyAdminsJob {
    private final AdminGroupRepository groups;
    private final AdminsService admins;
    private final TelegramApiClient api;

    public DailyAdminsJob(AdminGroupRepository groups, AdminsService admins, TelegramApiClient api) {
        this.groups = groups;
        this.admins = admins;
        this.api = api;
    }

    @Scheduled(cron = "0 0 21 * * *", zone = "Europe/Moscow")
    public void run() {
        for (var g : groups.findAll()) {
            if (g.getPinnedAdminsMessageId() != null) {
                api.editMessageText(g.getChatId(), g.getPinnedAdminsMessageId(), admins.buildAdmins(g), null).block();
            } else if (g.getAdminsTopicThreadId() != null) {
                // MVP: пока просто шлём сообщение в топик (как в статистике)
                api.sendMessage(g.getChatId(), g.getAdminsTopicThreadId(), admins.buildAdmins(g), null).block();
            }
        }
    }
}