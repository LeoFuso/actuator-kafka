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
import org.apache.kafka.common.config.ConfigResource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.util.Assert;

/**
 * Health indicator for Kafka.
 */
public class KafkaHealthIndicator extends AbstractHealthIndicator implements DisposableBean {

	static final String REPLICATION_PROPERTY = "transaction.state.log.replication.factor";

	private final AdminClient adminClient;

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

		final Map<String, Object> adminProperties = new HashMap<>(admin.getConfigurationProperties());
		final String adminClientIdPrefix = (String) adminProperties
				.getOrDefault(AdminClientConfig.CLIENT_ID_CONFIG, "default-admin-id");

		final String randomAdminClientId = String.format("%s-%s-health-check", adminClientIdPrefix, UUID.randomUUID());
		adminProperties.put(AdminClientConfig.CLIENT_ID_CONFIG, randomAdminClientId);

		this.adminClient = AdminClient.create(adminProperties);
		this.describeOptions = new DescribeClusterOptions()
				.timeoutMs((int) requestTimeout);

		this.considerReplicationFactor = considerReplicationFactor;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
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
		adminClient.close(Duration.ofSeconds(30));
	}

}