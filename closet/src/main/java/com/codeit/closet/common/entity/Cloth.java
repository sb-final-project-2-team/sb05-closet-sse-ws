package com.codeit.closet.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clothes")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Cloth {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "binary_content_id")
    private BinaryContent binaryContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ClothType type;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // 비즈니스 메서드
    public void updateName(String name) {
        this.name = name;
    }

    public void updateType(ClothType type) {
        this.type = type;
    }

    public void updateBinaryContent(BinaryContent binaryContent) {
        this.binaryContent = binaryContent;
    }

}
