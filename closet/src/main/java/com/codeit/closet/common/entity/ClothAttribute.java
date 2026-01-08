package com.codeit.closet.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "clothes_attributes")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes_values", columnDefinition = "jsonb", nullable = false)
    private List<String> attributesValues;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    // 비즈니스 메서드
    public void updateName(String name) {
        if (name != null) {
            this.name = name;
        }
    }

    public void updateAttributesValues(List<String> attributesValues) {
        if (attributesValues != null) {
            this.attributesValues = attributesValues;
        }
    }

}
