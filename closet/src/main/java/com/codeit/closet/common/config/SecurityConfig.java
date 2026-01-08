package com.codeit.closet.common.config;

import com.codeit.closet.common.redis.RedisLockProvider;
import com.codeit.closet.common.entity.UserRole;
import com.codeit.closet.common.security.jwt.JwtRegistry;
import com.codeit.closet.common.security.jwt.JwtTokenProvider;
import com.codeit.closet.common.security.jwt.RedisJwtRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.util.UUID;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role(UserRole.ADMIN.name()).implies(UserRole.USER.name())
                .build();
    }

    @Bean
    public JwtRegistry<UUID> jwtRegistry(JwtTokenProvider jwtTokenProvider,
                                         RedisTemplate<String, Object> redisTemplate, RedisLockProvider redisLockProvider) {
        return new RedisJwtRegistry(1, jwtTokenProvider, redisTemplate, redisLockProvider);
    }
}
