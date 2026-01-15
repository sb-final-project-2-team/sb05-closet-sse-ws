package com.codeit.closet.module.sse.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.codeit.closet.module.sse.dto.NotificationCreatedEventDTO;
import com.codeit.closet.module.sse.util.SseEmitterManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

	private final ObjectMapper objectMapper;
	private final SseEmitterManager emitterManager;

	@KafkaListener(
		topics = NotificationTopics.NOTIFICATION_CREATED,
		groupId = "${spring.kafka.consumer.group-id}"
	)
	public void consume(String payload) {
		log.info("[Test] raw payload = {}", payload);

		try{
			NotificationCreatedEventDTO event =
				objectMapper.readValue(payload,NotificationCreatedEventDTO.class);

			log.info("[SSE] 알림 수신, id={}, receiverId={}, level={}, title={}",
				event.id(), event.receiverId(), event.level(), event.title());

			emitterManager.send(
				event.receiverId(),
				"notification",
				event
			);
		} catch (JsonProcessingException e) {
			log.warn("[Kafka] JSON 파싱 실패");
		}
		catch (Exception e) {
			log.error("[Kafka] 알 수 없는 오류", e);
		}


	}
}
