package com.codeit.closet.module.ws.dto;

import java.util.UUID;

public record DirectMessagePersistRequest(
        UUID receiverId,
        UUID senderId,
        String content
) {}