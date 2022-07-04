package io.github.leofuso.actuator.kafka;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.config.ConfigResource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.util.Assert;

/**
 * Health indicator for Kafka.
 */
public class KafkaHealthIndicator
		extends AbstractHealthIndicator implements DisposableBean, SmartInitializingSingleton {

	static final String REPLICATION_PROPERTY = "transaction.state.log.replication.factor";

	private AdminClient adminClient;
	private final Map<String, Object> adminProperties;

	private final DescribeClusterOptions describeOptions;

	private final boolean considerReplicationFactor;

	/**
	 * Create a new {@link KafkaHealthIndicator} instance.
	 *
	 * @param admin the kafka admin
	 * @param requestTimeout the request timeout in milliseconds
	 * @param considerReplicationFactor if the replication factor should be considered
	 */
	public KafkaHealthIndicator(KafkaAdmin admin, long requestTimeout, boolean considerReplicationFactor) {
		Assert.notNull(admin, "KafkaAdmin must not be null");

		this.adminProperties = new HashMap<>(admin.getConfigurationProperties());
		final String adminClientIdPrefix = (String) adminProperties
				.getOrDefault(AdminClientConfig.CLIENT_ID_CONFIG, "default-admin-id");

		final String randomAdminClientId = String.format("%s-%s-health-check", adminClientIdPrefix, UUID.randomUUID());
		adminProperties.put(AdminClientConfig.CLIENT_ID_CONFIG, randomAdminClientId);
		this.describeOptions = new DescribeClusterOptions()
				.timeoutMs((int) requestTimeout);

		this.considerReplicationFactor = considerReplicationFactor;
	}

	@Override
	public void afterSingletonsInstantiated() {
		initialize();
	}

	/**
	 * Call this method to create the {@link AdminClient client}; this might be needed if the broker was not
	 * available when the application context was initialized.
	 * @return true if successful.
	 */
	public boolean initialize() {

		if(this.adminClient != null) {
			return true;
		}

		try {
			this.adminClient = AdminClient.create(adminProperties);
			return true;
		} catch (final KafkaException ex) {
			LoggerFactory
					.getLogger(KafkaHealthIndicator.class)
					.error("Something went wrong with AdminClient creation.", ex);
			return false;
		}
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {

		final boolean initialized = initialize();
		if(!initialized) {
			builder.unknown().withDetail("initialization", "Jaas probably hasn't fully started yet.");
			return;
		}

		final DescribeClusterResult result = adminClient.describeCluster(describeOptions);
		final String brokerId = result.controller().get().idString();
		final int nodes = result.nodes().get().size();
		if (considerReplicationFactor) {
			int replicationFactor = getReplicationFactor(brokerId);
			Status status = nodes >= replicationFactor ? Status.UP : Status.DOWN;
			builder.status(status).withDetail("requiredNodes", replicationFactor);
		}
		else {
			builder.up();
		}
		builder.withDetail("clusterId", result.clusterId().get())
				.withDetail("brokerId", brokerId).withDetail("nodes", nodes);
	}

	private int getReplicationFactor(String brokerId) throws Exception {
		final ConfigResource configResource = new ConfigResource(ConfigResource.Type.BROKER, brokerId);
		final Map<ConfigResource, Config> kafkaConfig = adminClient
				.describeConfigs(Collections.singletonList(configResource)).all().get();
		final Config brokerConfig = kafkaConfig.get(configResource);
		return Integer.parseInt(brokerConfig.get(REPLICATION_PROPERTY).value());
	}

	@Override
	public void destroy() {
		final boolean initialized = initialize();
		if(initialized) {
			adminClient.close(Duration.ofSeconds(30));
		}
	}
}