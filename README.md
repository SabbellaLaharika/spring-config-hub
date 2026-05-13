# Centralized Configuration Service with Spring Cloud Config

This repository implements a robust, centralized, Git-backed configuration system for microservices using Spring Cloud Config and Spring Boot. It features an externalized environment-aware config repository, dynamic run-time configuration refreshing, and automated Docker multi-stage orchestration with healthy dependency checking.

## System Architecture Overview

The architecture consists of:
1. **Config Repository (`config-repo/`)**: A local Git repository storing YAML configurations (`inventory-service-dev.yml`, `inventory-service-prod.yml`) for different environments.
2. **Config Server (`config-server/`)**: A dedicated Spring Boot microservice built with `spring-cloud-config-server`. It exposes configuration properties from the Git repository via standard HTTP APIs.
3. **Inventory Service (`inventory-service/`)**: A client microservice running Spring Boot. On startup, it resolves the config server's location via its bootstrap context, downloads target configurations, maps them using `@ConfigurationProperties`, and features hot-swappable `@RefreshScope` beans.
4. **Docker Orchestration**: Config Server and Inventory Service have multi-stage `Dockerfile` declarations and are linked in a `docker-compose.yml` setup configured with dependent healthchecks.

---

## Project Prerequisites

- **Docker** and **Docker Compose** installed.
- Git (optional, for further changes to config-repo manually).

---

## Getting Started

### 1. Set Environment Variables
You can configure your target environment by creating a `.env` file in the project root based on the provided `.env.example`:

```bash
cp .env.example .env
```

The default configuration targets the `dev` profile.

### 2. Build & Start System
Launch the entire suite using a single command:

```bash
docker-compose up --build
```

This will:
- Build both microservices using high-performance multi-stage Docker configurations.
- Wait for the `config-server` to become healthy (via `/actuator/health`).
- Launch the `inventory-service` once connectivity is verified.

---

## Core API Reference & Verification

### 1. Config Server - Direct Configuration Check
Verify that the Config Server properly loads and translates the Git repository's YAML to a structured API response:

**GET** `http://localhost:8888/inventory-service/dev`

**Sample Valid Response snippet**:
```json
{
  "name": "inventory-service",
  "profiles": ["dev"],
  "propertySources": [
    {
      "name": "file:///etc/config-repo/inventory-service-dev.yml",
      "source": {
        "inventory.maxStock": 100,
        "inventory.replenishThreshold": 10,
        "server.port": 8081
      }
    }
  ]
}
```

### 2. Client Config - Read Ingested Properties
Query the running microservice's state to ensure it mapped the dynamic values correctly:

**GET** `http://localhost:8081/api/inventory/config`

**Expected JSON**:
```json
{
  "profile": "dev",
  "maxStock": 100,
  "replenishThreshold": 10
}
```

### 3. Health Check Endpoint
Validate connection persistence with the configuration provider:

**GET** `http://localhost:8081/api/inventory/health`

**Expected Response**:
```json
{
    "status": "UP",
    "configServer": "connected"
}
```

---

## Demonstrating Dynamic Configuration Updates (Zero-Downtime)

To verify dynamic refresh functionality without restarting the `inventory-service` container, follow these steps:

1. Verify current initial stock limit is `100` by visiting `http://localhost:8081/api/inventory/config`.
2. Modify the underlying Git file `config-repo/inventory-service-dev.yml` on the host machine. Change `maxStock` to `250`:
   ```yaml
   inventory:
     maxStock: 250
     replenishThreshold: 10
   server:
     port: 8081
   ```
3. Commit the new file update into the local Git repository:
   ```bash
   cd config-repo
   git add .
   git commit -m "Testing config refresh payload"
   cd ..
   ```
4. Trigger a configuration reload by sending a POST request to the Inventory Service's Actuator endpoint:
   - **POST** `http://localhost:8081/actuator/refresh`
   - (Expected Status Code: 200 OK, body details array like `["inventory.maxStock"]`)
5. Verify the updated config is immediate: Visit `http://localhost:8081/api/inventory/config` and confirm `"maxStock": 250` is active.
