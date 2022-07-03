package io.github.leofuso.actuator.kafka;

import java.time.Duration;

import org.springframework.beans.factory.ObjectProvider;
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

import static io.github.leofuso.actuator.kafka.KafkaHealthIndicatorAutoConfiguration.INDICATOR;


@AutoConfiguration(after = KafkaAutoConfiguration.class)
@ConditionalOnClass(KafkaAdmin.class)
@ConditionalOnBean(KafkaAdmin.class)
@ConditionalOnEnabledHealthIndicator(INDICATOR)
@EnableConfigurationProperties(KafkaHealthIndicatorProperties.class)
public class KafkaHealthIndicatorAutoConfiguration {

    public static final String INDICATOR = "kafka";
    private final KafkaHealthIndicatorProperties properties;

    public KafkaHealthIndicatorAutoConfiguration(KafkaHealthIndicatorProperties properties) {
        this.properties = properties;
    }

    @Bean
    @DependsOn({"kafkaAdmin"})
    @ConditionalOnMissingBean(name = "kafkaHealthIndicator")
    public KafkaHealthIndicator kafkaHealthIndicator(ObjectProvider<KafkaAdmin> adminProvider) {
        final KafkaAdmin admin = adminProvider.getIfAvailable();
        if (admin != null) {

            final Duration responseTimeout = properties.getResponseTimeout();
            final long timeout = responseTimeout.toMillis();
            final boolean considerReplicationFactor = properties.isConsiderReplicationFactor();

            return new KafkaHealthIndicator(admin, timeout, considerReplicationFactor);
        }
        return null;
    }

}