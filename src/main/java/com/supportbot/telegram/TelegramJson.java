package com.supportbot.telegram;

import com.fasterxml.jackson.databind.JsonNode;

public final class TelegramJson {
    private TelegramJson() {}

    public static Long longOrNull(JsonNode n, String field) {
        if (n == null) return null;
        var v = n.get(field);
        return (v == null || v.isNull()) ? null : v.asLong();
    }

    public static Integer intOrNull(JsonNode n, String field) {
        if (n == null) return null;
        var v = n.get(field);
        return (v == null || v.isNull()) ? null : v.asInt();
    }

    public static String textOrNull(JsonNode n, String field) {
        if (n == null) return null;
        var v = n.get(field);
        return (v == null || v.isNull()) ? null : v.asText();
    }

    public static JsonNode obj(JsonNode n, String field) {
        return (n == null) ? null : n.get(field);
    }
}