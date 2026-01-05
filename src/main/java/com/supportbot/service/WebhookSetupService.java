package com.supportbot.service;


import com.supportbot.telegram.TelegramApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

@Service
public class WebhookSetupService implements ApplicationRunner {
    private final TelegramApiClient api;
    private final String url;
    private final String secret;

    public WebhookSetupService(TelegramApiClient api,
                               @Value("${telegram.webhook-url}") String url,
                               @Value("${telegram.webhook-secret}") String secret) {
        this.api = api;
        this.url = url;
        this.secret = secret;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            api.setWebhook(url, secret).block();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}