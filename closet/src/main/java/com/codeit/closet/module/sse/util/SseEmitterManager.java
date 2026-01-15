package com.codeit.closet.module.sse.util;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SseEmitterManager {

	private final ConcurrentHashMap<UUID, Set<SseEmitter>> emitterStore = new ConcurrentHashMap<>();

	private static final long DEFAULT_TIMEOUT_MILLIS = 60L * 60L * 1000L; // 1시간 설정

	public SseEmitter add(UUID receiverId) {
		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MILLIS);

		Set<SseEmitter> emitters =
			emitterStore.computeIfAbsent(receiverId, key -> ConcurrentHashMap.newKeySet());
		emitters.add(emitter);

		emitter.onCompletion(() -> remove(receiverId, emitter));
		emitter.onTimeout(() -> remove(receiverId, emitter));
		emitter.onError(ex ->{
			log.info("[SSE] 연결 오류로 emitter 제거, receiverId={}, 사유={}", receiverId, ex.toString());
			remove(receiverId, emitter);
		});

		log.info("[SSE] emitter 등록, receiverId={}, emitter.size={}", receiverId, emitters.size());
		return emitter;
	}

	public void remove(UUID receiverId, SseEmitter emitter) {
		Set<SseEmitter> emitters = emitterStore.get(receiverId);
		if (emitters == null) {
			return;
		}

		boolean removed = emitters.remove(emitter);
		if (removed) {
			log.info("[SSE] emitter 제거 receiverId={}, emitters.size={}", receiverId, emitters.size());
		}

		if (emitters.isEmpty()) {
			emitterStore.remove(receiverId);
		}
	}

	public void send(UUID receiverId, String eventName, Object data) {
		Set<SseEmitter> emitters = emitterStore.get(receiverId);

		if (emitters == null|| emitters.isEmpty()) {
			log.info("[SSE] 전송 대상 없음 receiverId={}, eventName={}", receiverId,eventName);
			return;
		}

		for (SseEmitter emitter : emitters) {
			try {
				emitter.send(SseEmitter.event()
					.id(Instant.now().toString())
					.name(eventName)
					.data(data)
				);
			} catch (IOException e) {
				log.info("[SSE] 전송 실패로 emitter 제거, receiverId={}, 이벤트={}, 사유={}",
					receiverId, eventName, e.toString());
				remove(receiverId, emitter);
			}
		}
	}
	public int count(UUID receiverId) {
		Set<SseEmitter> emitters = emitterStore.get(receiverId);
		return emitters == null ? 0 : emitters.size();
	}
}
