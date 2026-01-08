package com.codeit.closet.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * 날씨 예보 데이터
 */
@Entity
@Table(name = "weather_data")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class WeatherData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weather_region_id", nullable = false)
    private WeatherRegion weatherRegion;

    @Enumerated(EnumType.STRING)
    @Column(name = "forecast_kind", nullable = false)
    private ForecastKind forecastKind;

    @Column(name = "forecast_at", nullable = false)
    private Instant forecastAt;  // 예보 대상 시각

    @Column(name = "forecasted_at", nullable = false)
    private Instant forecastedAt;  // 예보 생성 시각

    @Enumerated(EnumType.STRING)
    @Column(name = "sky_status", nullable = false)
    private SkyStatus skyStatus;

    // 온도 (4개)
    @Column(name = "temperature_current", nullable = false)
    private Double temperatureCurrent;

    @Column(name = "temperature_comp_prev_day", nullable = false)
    private Double temperatureCompPrevDay;

    @Column(name = "temperature_min", nullable = false)
    private Double temperatureMin;

    @Column(name = "temperature_max", nullable = false)
    private Double temperatureMax;

    // 강수 (3개)
    @Enumerated(EnumType.STRING)
    @Column(name = "precipitation_type", nullable = false)
    private PrecipitationType precipitationType;

    @Column(name = "precipitation_amount", nullable = false)
    private Double precipitationAmount;

    @Column(name = "precipitation_prob", nullable = false)
    private Double precipitationProb;

    // 습도 (2개)
    @Column(name = "humidity_current", nullable = false)
    private Double humidityCurrent;

    @Column(name = "humidity_comp_to_day_before", nullable = false)
    private Double humidityComparedToDayBefore;

    // 바람 (2개)
    @Column(name = "wind_speed", nullable = false)
    private Double windSpeed;

    @Enumerated(EnumType.STRING)
    @Column(name = "wind_as_word", nullable = false)
    private WindStrength windAsWord;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
