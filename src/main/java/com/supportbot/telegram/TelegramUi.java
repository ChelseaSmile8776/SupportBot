package com.supportbot.telegram;

import java.util.*;

public final class TelegramUi {
    private TelegramUi() {}

    public static Map<String, Object> inlineKeyboard(List<List<Map<String, Object>>> rows) {
        return Map.of("inline_keyboard", rows);
    }

    public static Map<String, Object> btn(String text, String callbackData) {
        return Map.of("text", text, "callback_data", callbackData);
    }

    public static List<List<Map<String, Object>>> rows(List<Map<String, Object>>... rows) {
        return Arrays.asList(rows);
    }

    public static List<Map<String, Object>> row(Map<String, Object>... buttons) {
        return Arrays.asList(buttons);
    }
}