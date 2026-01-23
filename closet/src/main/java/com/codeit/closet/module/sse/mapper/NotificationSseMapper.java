package com.codeit.closet.module.sse.mapper;

import org.mapstruct.Mapper;

import com.codeit.closet.common.entity.Notification;
import com.codeit.closet.module.sse.dto.NotificationCreatedEventDTO;
import com.codeit.closet.module.sse.dto.NotificationCreatedEventDTO;

@Mapper(componentModel = "spring")
public interface NotificationSseMapper {

	Notification toDto(NotificationCreatedEventDTO event);
}
