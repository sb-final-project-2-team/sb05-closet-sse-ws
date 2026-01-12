package com.codeit.closet.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "clothes_attributes_values",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_clothes_attr",
                columnNames = {"clothes_id", "clothes_attributes_id"}
        ))
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothAttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "clothes_id", nullable = false)
    private UUID clothId;

    @Column(name = "clothes_attributes_id", nullable = false)
    private UUID clothAttributeId;

    @Column(name = "value", nullable = false, length = 255)
    private String value;

}
