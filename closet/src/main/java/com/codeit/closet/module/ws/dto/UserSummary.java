package com.codeit.closet.module.ws.dto;

import java.util.UUID;

public record UserSummary(
        UUID userId,
        String name,
        String profileImageUrl
) {
}