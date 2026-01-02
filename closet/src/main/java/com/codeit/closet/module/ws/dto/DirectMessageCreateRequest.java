package com.codeit.closet.module.ws.dto;

import java.util.UUID;

public record DirectMessageCreateRequest(
        UUID receiverId,
        UUID senderId,
        String content
) {}