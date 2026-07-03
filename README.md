# Centralized Configuration Service with Spring Cloud Config

This repository implements a robust, centralized, Git-backed configuration system for microservices using Spring Cloud Config and Spring Boot. It features an externalized environment-aware config repository, dynamic run-time configuration refreshing, and automated Docker multi-stage orchestration with healthy dependency checking.

---

## Core Architectural Concepts

### Decentralized vs. Centralized Configuration

In a decentralized or traditional configuration pattern, each microservice carries its own environment configurations (like database credentials, feature flags, or service limits) inside its local application package (e.g., `application.properties` or `application.yml`).

This traditional approach has critical flaws in distributed production environments:
1. **Redundant Configurations**: The same setting (like database URLs or email server configurations) is duplicated across multiple services, leading to inconsistencies.
2. **Configuration Drift**: Over time, settings on dev, staging, and production environments drift apart, causing bugs that only show up under specific environments.
3. **No Auditing or History**: Changes are made directly on servers or pushed through code, making it difficult to audit who changed a setting or when it was changed.
4. **Service Downtime**: Applying any configuration change requires rebuilding, repackaging, and restarting the entire microservice, dropping active traffic.

**Centralized configuration** resolves these issues by moving all configuration files to a centralized provider (Spring Cloud Config Server) backed by a version-controlled repository (Git). Client microservices query this central server at startup to retrieve their environment-specific settings.

---

## System Architecture

![System Architecture Overview](images/system_architecture_diagram.png)

The system consists of the following components:

1. **Config Repository (`config-repo/`)**: A local Git repository containing configuration files (`inventory-service-dev.yml`, `inventory-service-prod.yml`) for different environments. This provides version control, rollback capabilities, and commit-based audits.
2. **Config Server (`config-server/`)**: A Spring Boot microservice built with `spring-cloud-config-server`. It exposes properties from the Git repository via standard HTTP REST APIs.
3. **Inventory Service (`inventory-service/`)**: A client microservice running Spring Boot. At boot, it resolves the config server's location via its bootstrap context, downloads target configurations, maps them using `@ConfigurationProperties`, and features hot-swappable `@RefreshScope` beans.
4. **Docker Compose Orchestration**: Links the services together, ensuring that `inventory-service` only starts after the `config-server` passes its Actuator health check.

---

## Detailed Concepts Explained

### 1. Why Git Backing Matters
Using Git as the config repository backend provides enterprise-grade benefits:
* **Versioning**: Every configuration change is tracked as a Git commit, showing exactly who modified a setting and why.
* **Audit Trail**: Provides full transparency on environment changes, reducing security and compliance risks.
* **Rollbacks**: If a config change breaks production, reverting to the previous working state is as simple as running a `git revert` command.

### 2. How Dynamic Configuration Refresh Works
The magic of updating configurations without a service restart relies on Spring Boot Actuator and Spring Cloud's `@RefreshScope`:

![Dynamic Config Refresh Flow](images/refresh_flow_diagram.png)

1. The developer updates the configuration file in `config-repo` and commits it.
2. A `POST` request is sent to the client's `/actuator/refresh` endpoint.
3. Spring Cloud handles the event by clearing all cached beans annotated with `@RefreshScope`.
4. It calls the Config Server to fetch the latest properties and inserts them into the Spring `Environment`.
5. When the client next accesses the bean, Spring instantiates a new instance, binding the new configurations on-the-fly. This happens with **zero downtime**.

### 3. Container Boot Sequence & Resilience
To prevent microservices from failing to boot due to missing configurations, the `inventory-service` uses Spring's `fail-fast: true` property. If the Config Server is offline, the client will immediately shut down. 

In Docker Compose, we ensure this doesn't cause startup loops by defining a health check on `config-server` (`/actuator/health`) and using the `condition: service_healthy` rule inside `depends_on` on the `inventory-service`. The client waits until the server is fully initialized before booting.

---

## Getting Started

### 1. Set Environment Variables
Configure your target environment by creating a `.env` file in the project root based on the provided `.env.example`:

```bash
cp .env.example .env
```

The default configurations target the `dev` profile.

### 2. Build & Start the System
Launch the entire suite using a single command:

```bash
docker-compose up --build
```

---

## Core API Reference & Verification

### 1. Config Server - Direct Configuration Check
Verify that the Config Server properly loads and translates the Git repository's YAML to a structured API response:

**GET** `http://localhost:8888/inventory-service/dev`

**Sample Response**:
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
* **Explanation**: The response contains the application's configuration metadata. The `propertySources` array lists the active files, and the `source` block lists the exact key-value pairs parsed from `inventory-service-dev.yml` to be served to the client.

### 2. Client Config - Read Ingested Properties
Query the running client microservice to ensure it mapped the configuration values:

**GET** `http://localhost:8081/api/inventory/config`

**Expected JSON**:
```json
{
  "profile": "dev",
  "maxStock": 100,
  "replenishThreshold": 10
}
```
* **Explanation**: This is the mapped output of the `@ConfigurationProperties` bean, verifying that `inventory-service` correctly ingested properties from the Config Server and bound them to Java fields.

### 3. Custom Health Indicator Check
Validate connection status with the configuration provider:

**GET** `http://localhost:8081/api/inventory/health`

**Expected Response**:
```json
{
    "status": "UP",
    "configServer": "connected"
}
```
* **Explanation**: This calls a custom `HealthIndicator` bean (`ConfigServerHealthIndicator`). If the application has loaded configurations (verified by searching for the `inventory.maxStock` property in the Spring environment), it returns an `UP` status. This integrates with `/actuator/health` to report consolidated microservice health.
