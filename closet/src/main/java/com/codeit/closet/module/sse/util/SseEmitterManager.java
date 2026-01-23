package com.codeit.closet.module.sse.util;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseEmitterManager {

	private static final long DEFAULT_TIMEOUT = 60L * 60L * 1000L; // 1 hour

	private final ConcurrentHashMap<UUID, Set<SseEmitter>> emitterStore = new ConcurrentHashMap<>();
	private final ObjectMapper objectMapper;

	public SseEmitter add(UUID receiverId) {
		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

		emitterStore
			.computeIfAbsent(receiverId, id -> new CopyOnWriteArraySet<>())
			.add(emitter);

		registerLifecycleCallbacks(receiverId, emitter);

		log.debug("[SSE] emitter 등록, receiverId={}, 현재 emitter 수={}",
			receiverId, emitterStore.get(receiverId).size());

		return emitter;
	}

	public SseEmitter createAnonymousEmitter() {
		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

		emitter.onCompletion(() ->
			log.debug("[SSE] anonymous emitter completed")
		);

		emitter.onTimeout(() ->
			log.debug("[SSE] anonymous emitter timeout")
		);

		emitter.onError(e ->
			log.debug("[SSE] anonymous emitter error: {}", e.getMessage())
		);

		return emitter;
	}

	public void send(UUID receiverId, String eventName, Object data) {
		Set<SseEmitter> emitters = emitterStore.get(receiverId);

		if (emitters == null || emitters.isEmpty()) {
			log.warn("[SSE] 전송 대상 없음, receiverId={}", receiverId);
			return;
		}

		for (SseEmitter emitter : emitters) {
			try {
				String json = objectMapper.writeValueAsString(data);

				emitter.send(SseEmitter.event()
					.name(eventName)
					.data(json)
				);

			} catch (IOException e) {
				log.warn("[SSE] 전송 실패, receiverId={}, reason={}",
					receiverId, e.getMessage());
				remove(receiverId, emitter);
				emitter.completeWithError(e);
			}
		}
	}

	public void remove(UUID receiverId, SseEmitter emitter) {
		Set<SseEmitter> emitters = emitterStore.get(receiverId);

		if (emitters == null) {
			return;
		}

		emitters.remove(emitter);

		if (emitters.isEmpty()) {
			emitterStore.remove(receiverId);
		}

		log.debug("[SSE] emitter 제거, receiverId={}, 남은 emitter 수={}",
			receiverId,
			emitters.size()
		);
	}

	public int count(UUID receiverId) {
		Set<SseEmitter> emitters = emitterStore.get(receiverId);
		return emitters == null ? 0 : emitters.size();
	}

	private void registerLifecycleCallbacks(UUID receiverId, SseEmitter emitter) {
		emitter.onCompletion(() -> {
			log.debug("[SSE] emitter completion, receiverId={}", receiverId);
			remove(receiverId, emitter);
		});

		emitter.onTimeout(() -> {
			log.debug("[SSE] emitter timeout, receiverId={}", receiverId);
			remove(receiverId, emitter);
		});

		emitter.onError(e -> {
			log.debug("[SSE] emitter error, receiverId={}, reason={}",
				receiverId, e.getMessage());
			remove(receiverId, emitter);
		});
	}
}
