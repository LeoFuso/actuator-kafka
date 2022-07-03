package io.github.leofuso.actuator.kafka;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "management.health.kafka", ignoreUnknownFields = false)
public class KafkaHealthIndicatorProperties {

    /**
     * Time to wait for a response from the cluster description operation.
     */
    private Duration responseTimeout = Duration.ofMillis(1000);


    /**
     * Use the replication factor of the broker to validate if it has enough nodes.
     */
    private boolean considerReplicationFactor = false;

    public Duration getResponseTimeout() {
        return this.responseTimeout;
    }

    public void setResponseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public boolean isConsiderReplicationFactor() {
        return this.considerReplicationFactor;
    }

    public void setConsiderReplicationFactor(boolean considerReplicationFactor) {
        this.considerReplicationFactor = considerReplicationFactor;
    }

}