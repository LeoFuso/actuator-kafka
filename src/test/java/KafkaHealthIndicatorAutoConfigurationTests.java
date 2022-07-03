import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.github.leofuso.autoconfigure.actuator.kafka.KafkaHealthIndicator;
import io.github.leofuso.autoconfigure.actuator.kafka.KafkaHealthIndicatorAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaHealthIndicatorAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(
                                    KafkaAutoConfiguration.class,
                                    KafkaHealthIndicatorAutoConfiguration.class,
                                    HealthContributorAutoConfiguration.class
                            )
                    );

    @Test
    public void runShouldCreateIndicator() {
        this.contextRunner.run(
                (context) -> assertThat(context).hasSingleBean(KafkaHealthIndicator.class));
    }

    @Test
    public void runWhenDisabledShouldNotCreateIndicator() {
        this.contextRunner.withPropertyValues("management.health.kafka.enabled:false")
                          .run((context) -> assertThat(context)
                                  .doesNotHaveBean(KafkaHealthIndicator.class)
                                  .hasSingleBean(HealthContributorAutoConfiguration.class));
    }

}