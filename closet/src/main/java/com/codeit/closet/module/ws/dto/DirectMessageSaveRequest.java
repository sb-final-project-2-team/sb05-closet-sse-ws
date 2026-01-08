package com.codeit.closet.module.ws.dto;

import java.util.UUID;

public record DirectMessageSaveRequest(
        UUID receiverId,
        String content
) {}