package com.codeit.closet.module.ws.dto;

import java.time.Instant;
import java.util.UUID;

public record DirectMessageDTO(
        UUID id,
        Instant createdAt,
        UserSummary sender,
        UserSummary receiver,
        String content
) {
}