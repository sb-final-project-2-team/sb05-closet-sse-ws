package com.codeit.closet.module.sse.util;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.codeit.closet.common.security.jwt.JwtObject;
import com.codeit.closet.common.security.jwt.JwtTokenProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseJwtResolver {

	private final JwtTokenProvider jwtTokenProvider;

	public UUID resolveReceiverId(HttpServletRequest request) {
		if (request.getCookies() == null) {
			return null;
		}

		for (Cookie cookie : request.getCookies()) {
			if (JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
				try {
					String refreshToken = cookie.getValue();

					if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
						return null;
					}

					JwtObject jwt = jwtTokenProvider.parseRefreshToken(refreshToken);
					return jwt.userDTO().id();

				} catch (Exception e) {
				    log.warn("[SSE] RefreshToken 파싱 실패");
					return null;
				}
			}
		}
		return null;
	}
}
