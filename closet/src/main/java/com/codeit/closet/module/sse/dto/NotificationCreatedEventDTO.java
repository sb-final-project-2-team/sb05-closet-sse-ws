package com.codeit.closet.module.sse.dto;

import java.time.Instant;
import java.util.UUID;

import com.codeit.closet.common.entity.NotificationLevel;

public record NotificationCreatedEventDTO (
	UUID id,
	UUID receiverId,
	String title,
	String content,
	NotificationLevel level,
	Instant createdAt
) {}
