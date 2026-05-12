package com.example.inventoryservice;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "inventory")
public class InventoryConfig {

    private int maxStock;
    private int replenishThreshold;

    public int getMaxStock() {
        return maxStock;
    }

    public void setMaxStock(int maxStock) {
        this.maxStock = maxStock;
    }

    public int getReplenishThreshold() {
        return replenishThreshold;
    }

    public void setReplenishThreshold(int replenishThreshold) {
        this.replenishThreshold = replenishThreshold;
    }
}
