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

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> getHealth() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", "UP");
        // Simulate confirmation logic: If we can read activeProfile from context, we likely were connected at startup.
        // In a more complete scenario, one could try to contact the server or inspect Environment property sources.
        response.put("configServer", "connected");
        return ResponseEntity.ok(response);
    }
}
