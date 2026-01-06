package com.supportbot.service;

import com.supportbot.repo.AdminGroupRepository;
import com.supportbot.telegram.TelegramApiClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyStatsJob {
    private final AdminGroupRepository groups;
    private final StatsService stats;
    private final TelegramApiClient api;

    public DailyStatsJob(AdminGroupRepository groups, StatsService stats, TelegramApiClient api) {
        this.groups = groups;
        this.stats = stats;
        this.api = api;
    }

    @Scheduled(cron = "0 0 21 * * *", zone = "Europe/Moscow")
    public void run() {
        for (var g : groups.findAll()) {
            if (g.getPinnedStatsMessageId() != null) {
                api.editMessageText(g.getChatId(), g.getPinnedStatsMessageId(), stats.buildAllTime(g), null).block();
            } else if (g.getStatsTopicThreadId() != null) {
                // MVP: первый раз отправим сообщение и (позже) распарсим message_id, чтобы закрепить
                api.sendMessage(g.getChatId(), g.getStatsTopicThreadId(), stats.buildAllTime(g), null).block();
            }

            if (g.getStatsTopicThreadId() != null) {
                api.sendMessage(g.getChatId(), g.getStatsTopicThreadId(), stats.buildDaily(g), null).block();
            }
        }
    }
}