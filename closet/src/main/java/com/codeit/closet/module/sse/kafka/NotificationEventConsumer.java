package com.codeit.closet.module.sse.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.codeit.closet.module.sse.dto.NotificationCreatedEventDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotificationEventConsumer {

	@KafkaListener(
		topics = NotificationTopics.NOTIFICATION_CREATED,
		groupId = "${spring.kafka.consumer.group-id}"
	)
	public void consume(NotificationCreatedEventDTO event) {
		log.info("[Notification] 알림 수신, receiverId={}, id={}, level={}, title={} ",
			event.receiverId(), event.id(), event.level(), event.title());
	}
}
