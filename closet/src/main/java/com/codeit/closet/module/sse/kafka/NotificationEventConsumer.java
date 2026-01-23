package com.codeit.closet.module.sse.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.codeit.closet.common.entity.Notification;
import com.codeit.closet.module.sse.dto.NotificationCreatedEventDTO;
import com.codeit.closet.module.sse.mapper.NotificationSseMapper;
import com.codeit.closet.module.sse.util.SseEmitterManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

	private final ObjectMapper objectMapper;
	private final SseEmitterManager emitterManager;
	private final NotificationSseMapper notificationSseMapper;

	@PostConstruct
	public void init() {
		log.info("[Kafka Consumer] 초기화 완료");
	}

	@KafkaListener(
		topics = NotificationTopics.NOTIFICATION_CREATED,
		groupId = "notification-group"
	)
	public void consume(String message) {
		log.info("[Kafka] 메시지 수신 시작! Message: {}", message);

		try {
			NotificationCreatedEventDTO event = objectMapper.readValue(message, NotificationCreatedEventDTO.class);

			Notification notification = notificationSseMapper.toDto(event);

			log.info("[SSE] 알림 성공, id={}, receiverId={}",
				event.id(), event.receiverId());

			emitterManager.send(
				event.receiverId(),
				"notifications",
				notification
			);

			log.info("[SSE] 알림 전송 완료");
		} catch (Exception e) {
			log.error("[Kafka] 알 수 없는 오류", e);
		}
	}
}
