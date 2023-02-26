package com.example.task1.config;

import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import org.apache.camel.component.micrometer.MicrometerConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricConfig {

    @Bean(name = {MicrometerConstants.METRICS_REGISTRY_NAME, "prometheusMeterRegistry"})
    public PrometheusMeterRegistry prometheusMeterRegistry(PrometheusConfig prometheusConfig, CollectorRegistry collectorRegistry, Clock clock) {
        return new PrometheusMeterRegistry(prometheusConfig, collectorRegistry, clock);
    }
}
