package com.example.inventoryservice;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component("configServer")
public class ConfigServerHealthIndicator implements HealthIndicator {

    private final Environment environment;

    public ConfigServerHealthIndicator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Health health() {
        // Check for presence of config server properties in the environment
        if (environment.containsProperty("inventory.maxStock")) {
            return Health.up()
                    .withDetail("configServer", "connected")
                    .build();
        } else {
            return Health.down()
                    .withDetail("configServer", "disconnected")
                    .build();
        }
    }
}
