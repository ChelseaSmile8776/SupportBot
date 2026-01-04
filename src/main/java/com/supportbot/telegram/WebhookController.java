package com.supportbot.telegram;

import com.supportbot.telegram.dto.Update;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class WebhookController {
    private final UpdateRouter router;
    private final String secret;

    public WebhookController(UpdateRouter router,
                             @Value("${telegram.webhook-secret}") String secret) {
        this.router = router;
        this.secret = secret;
    }

    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.OK)
    public void onUpdate(@RequestBody Update update,
                         @RequestHeader(name = "X-Telegram-Bot-Api-Secret-Token", required = false)
                         String headerSecret) {
        if (secret != null && !secret.isBlank()) {
            if (headerSecret == null || !secret.equals(headerSecret)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        router.route(update);
    }
}