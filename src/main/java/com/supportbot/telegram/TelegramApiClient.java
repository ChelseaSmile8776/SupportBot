package com.supportbot.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class TelegramApiClient {
    private final WebClient web;

    public TelegramApiClient(@Value("${telegram.bot-token}") String token) {
        this.web = WebClient.builder()
                .baseUrl("https://api.telegram.org/bot" + token)
                .build();
    }

    public Mono<String> setWebhook(String url, String secretToken) {
        Map<String, Object> body = new HashMap<>();
        body.put("url", url);
        body.put("secret_token", secretToken);
        return post("/setWebhook", body);
    }

    public Mono<String> sendMessage(long chatId, Integer messageThreadId, String text, Object replyMarkup) {
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        if (messageThreadId != null) body.put("message_thread_id", messageThreadId);
        body.put("text", text);
        body.put("parse_mode", "HTML");
        if (replyMarkup != null) body.put("reply_markup", replyMarkup);
        return post("/sendMessage", body);
    }

    public Mono<String> editMessageText(long chatId, int messageId, String text, Object replyMarkup) {
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("message_id", messageId);
        body.put("text", text);
        body.put("parse_mode", "HTML");
        if (replyMarkup != null) body.put("reply_markup", replyMarkup);
        return post("/editMessageText", body);
    }

    public Mono<String> deleteMessage(long chatId, int messageId) {
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("message_id", messageId);
        return post("/deleteMessage", body);
    }

    public Mono<String> pinChatMessage(long chatId, int messageId) {
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("message_id", messageId);
        body.put("disable_notification", true);
        return post("/pinChatMessage", body);
    }

    public Mono<String> answerCallbackQuery(String callbackQueryId, String text) {
        Map<String, Object> body = new HashMap<>();
        body.put("callback_query_id", callbackQueryId);
        if (text != null) body.put("text", text);
        return post("/answerCallbackQuery", body);
    }

    public Mono<String> createForumTopic(long chatId, String name) {
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("name", name);
        return post("/createForumTopic", body);
    }

    public Mono<String> deleteForumTopic(long chatId, int messageThreadId) {
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("message_thread_id", messageThreadId);
        return post("/deleteForumTopic", body);
    }

    private Mono<String> post(String method, Object body) {
        return web.post()
                .uri(method)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }
}
