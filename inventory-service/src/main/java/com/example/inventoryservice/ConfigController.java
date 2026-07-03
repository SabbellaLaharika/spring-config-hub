package com.example.inventoryservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RefreshScope
public class ConfigController {

    @Autowired
    private InventoryConfig inventoryConfig;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("profile", activeProfile);
        response.put("maxStock", inventoryConfig.getMaxStock());
        response.put("replenishThreshold", inventoryConfig.getReplenishThreshold());
        return response;
    }

    @Autowired
    private ConfigServerHealthIndicator configServerHealthIndicator;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> getHealth() {
        org.springframework.boot.actuate.health.Health health = configServerHealthIndicator.health();
        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", health.getStatus().getCode());
        response.put("configServer", (String) health.getDetails().get("configServer"));
        return ResponseEntity.ok(response);
    }
}
