package io.github.leofuso.autoconfigure.actuator.kafka;

import java.time.Duration;

import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.core.KafkaAdmin;


@AutoConfiguration(after = KafkaAutoConfiguration.class)
@ConditionalOnClass(KafkaAdmin.class)
@ConditionalOnBean(KafkaAdmin.class)
@ConditionalOnEnabledHealthIndicator("kafka")
@EnableConfigurationProperties(KafkaHealthIndicatorProperties.class)
public class KafkaHealthIndicatorAutoConfiguration {

    private final KafkaAdmin admin;
    private final KafkaHealthIndicatorProperties properties;

    public KafkaHealthIndicatorAutoConfiguration(KafkaAdmin admin, KafkaHealthIndicatorProperties properties) {
        this.admin = admin;
        this.properties = properties;
    }

    @Bean
    @DependsOn({"kafkaAdmin"})
    @ConditionalOnMissingBean(name = "kafkaHealthIndicator")
    public HealthIndicator kafkaHealthIndicator() {
		final Duration responseTimeout = properties.getResponseTimeout();
		final long timeout = responseTimeout.toMillis();
		return new KafkaHealthIndicator(admin, timeout, properties.isConsiderReplicationFactor());
    }

}