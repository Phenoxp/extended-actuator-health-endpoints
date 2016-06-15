package de.filiadata.infrastructure.actuator.health;

import de.filiadata.infrastructure.actuator.health.endpoint.AliveHealthEndpoint;
import de.filiadata.infrastructure.actuator.health.endpoint.BasicHealthEndpoint;
import de.filiadata.infrastructure.actuator.health.endpoint.DetailHealthEndpoint;
import de.filiadata.infrastructure.actuator.health.indicator.ApplicationAliveIndicator;
import de.filiadata.infrastructure.actuator.health.indicator.BasicHealthIndicator;
import de.filiadata.infrastructure.actuator.health.indicator.DetailHealthIndicator;
import de.filiadata.infrastructure.actuator.health.mvcendpoint.AliveHealthController;
import de.filiadata.infrastructure.actuator.health.mvcendpoint.BasicHealthController;
import de.filiadata.infrastructure.actuator.health.mvcendpoint.DetailHealthController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.HealthIndicatorAutoConfiguration;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.OrderedHealthAggregator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * ExtendedHealthEndpointAutoConfiguration configures HealthIndicators like Spring Boots HealthEndpoint, but with different levels of HealthIndicators, currently:
 *
 * * {@link ApplicationAliveIndicator} (faster than basic, used by load balancer to decide if application is alive)
 * * {@link BasicHealthIndicator} (only basic, combined should not take longer than 5 seconds)
 * * {@link DetailHealthIndicator} (includes all, combined may take up to 30 seconds)
 *
 * Beans implementing one of these Interfaces are automatically included in one of these health endpoint categories.
 */
@Configuration
@ConditionalOnWebApplication
@AutoConfigureAfter(HealthIndicatorAutoConfiguration.class)
@EnableConfigurationProperties(ExtendedHealthProperties.class)
public class ExtendedHealthEndpointAutoConfiguration {

    @Autowired
    private ExtendedHealthProperties properties;

    @Autowired(required = false)
    public Map<String, ApplicationAliveIndicator> aliveIndicators = new HashMap<>();

    @Autowired(required = false)
    public Map<String, BasicHealthIndicator> basicHealthIndicators = new HashMap<>();

    @Autowired(required = false)
    public Map<String, HealthIndicator> allHealthIndicators = new HashMap<>();

    public AliveHealthEndpoint applicationAliveEndpoint() {

        return new AliveHealthEndpoint(properties.getAliveId(), new OrderedHealthAggregator(), aliveIndicators);
    }

    public BasicHealthEndpoint basicHealthEndpoint() {

        return new BasicHealthEndpoint(properties.getBasicId(), new OrderedHealthAggregator(), basicHealthIndicators);
    }

    public DetailHealthEndpoint detailHealthEndpoint() {

        return new DetailHealthEndpoint(properties.getDetailId(), new OrderedHealthAggregator(), allHealthIndicators);
    }

    @Bean
    @ConditionalOnMissingBean(AliveHealthController.class)
    public AliveHealthController aliveMvcEndpoint() {
        return new AliveHealthController(applicationAliveEndpoint());
    }

    @Bean
    @ConditionalOnMissingBean(BasicHealthController.class)
    public BasicHealthController basicMvcEndpoint() {
        return new BasicHealthController(basicHealthEndpoint());
    }

    @Bean
    @ConditionalOnMissingBean(DetailHealthController.class)
    public DetailHealthController detailMvcEndpoint() {
        return new DetailHealthController(detailHealthEndpoint());
    }

}
