package com.codeit.closet.common.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "receiver_id",nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID receiverId;

    @Column(name ="title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name ="level", nullable = false, length = 10)
    private NotificationLevel level;

    @Column(name ="created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID(); // 애플리케이션에서 UUID 생성 (캐시 활용)
        }
        if (this.createdAt == null){
            this.createdAt = Instant.now(); // null일 때만 세팅
        }
    }
}
