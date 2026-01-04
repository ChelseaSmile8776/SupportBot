package com.supportbot.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Update(
        Long update_id,
        JsonNode message,
        JsonNode edited_message,
        JsonNode callback_query,
        JsonNode my_chat_member
) {}