# API Gateway with Rate Limiting

---



A backend infrastructure project that implements an **API Gateway** with **JWT-based request filtering** and **Redis-backed rate limiting** using a **token bucket strategy**. This project is designed to demonstrate backend engineering concepts commonly used in production systems such as **traffic control**, **request filtering**, **gateway middleware**, and **distributed rate limiting**.

## Overview

In modern distributed systems, API Gateways act as the single entry point for client requests. They are responsible for handling cross-cutting concerns such as:

- authentication
- routing
- rate limiting
- request filtering
- observability
- security enforcement

This project focuses on building a simplified but practical API Gateway that can:

- control API request throughput
- prevent abuse using rate limiting
- support distributed traffic control with Redis
- filter and validate incoming requests
- expose monitoring endpoints for visibility into throttling behavior

The goal of this project is to demonstrate understanding of **backend infrastructure design** and **scalable request management patterns**.

---

## Features

- **JWT Authentication Filter** for protected routes
- **Redis-backed Token Bucket Rate Limiter**
- **User-based rate limiting** with IP fallback
- **Structured API error responses**
- **Monitoring endpoint for rate limit status**
- **Spring Security filter chain architecture**
- **Dockerized runtime environment**
- **Environment-specific configuration (local / docker)**
---

## Tech Stack

| Layer | Technology |
|------|-------------|
| Backend | Java, Spring Boot |
| Security | Spring Security, JWT |
| Rate Limiting | Redis |
| Containerization | Docker, Docker Compose |
| Build Tool | Maven |
---

## Architecture


The system is designed around a **gateway-first request pipeline** implemented using Spring Security filters.

### High-Level Flow

```text
Client Request
      |
      v
API Gateway
      |
      +--> JWT Authentication Filter
      |
      +--> Request Validation / Filtering
      |
      +--> Rate Limiter(Token Bucket)
               |
               v
             Redis
      |
      +--> Forward Request(Controller) / Reject with 429
```

### Redis Rate Limit State
Each client has a **token bucket stored in Redis**.


### Why Rate Limiting Matters
Rate limiting is an essential part of production backend systems because it helps:
- protect services from abuse and brute-force traffic
- prevent accidental overload from clients
- improve fairness across consumers
- maintain service availability under traffic spikes
- enforce usage policies for APIs

This project uses a token bucket-inspired approach because it allows short bursts of traffic while still controlling sustained request rates.

---
### Core Concepts Demonstrated
This project showcases several backend engineering concepts:

#### 1. API Gateway Pattern
- A single entry point for handling requests before they reach downstream services.

#### 2. Distributed Rate Limiting
- Using Redis to maintain rate limiting state allows the gateway to scale across multiple instances while preserving consistent throttling behavior.

#### 3. Middleware / Filter Chain Design
- The request pipeline demonstrates how security and traffic policies can be enforced at the gateway layer.

#### 4. Fault Isolation
- The rate limiter protects downstream systems from overload by rejecting excessive traffic early.

#### 5. Observability
- Monitoring endpoints make throttling behavior visible and easier to debug.

---
### Project Structure
```text
src/main/java/com/jeet/apigateway
│
├── config/        # Security and application configuration
├── controller/    # REST controllers
├── exception/     # Custom exception classes
├── model/         # Response DTOs
├── ratelimit/     # Token bucket rate limiting logic
├── security/      # JWT authentication implementation
└── ApiGatewayApplication.java
```

---
### Rate Limiting Strategy
This project uses a token bucket-based rate limiting model.

#### How it works
- Each client is assigned a bucket with a fixed capacity.
- Tokens are consumed when requests are made.
- Tokens are replenished over time.
- If the bucket has tokens available, the request is allowed.
- If no tokens remain, the request is rejected with HTTP 429 Too Many Requests.

#### Why token bucket
Compared to fixed window limiting, token bucket provides more flexibility because:
- it supports burst traffic better
- it avoids unfair blocking near window boundaries
- it behaves closer to real-world production throttling policies

---
### Redis Integration

Redis is used as the shared data store for rate limiting state.

#### Why Redis?
- very fast read/write operations
- suitable for distributed counters
- lightweight and commonly used in production systems
- enables horizontal scalability across multiple gateway instances

By storing token bucket or request count state in Redis, rate limiting remains consistent even when the gateway is deployed across multiple containers or nodes.

---
### Security
This project includes JWT-based request filtering to demonstrate gateway-level authentication handling.

#### Security responsibilities handled by the gateway
- validating incoming JWT tokens
- rejecting unauthorized requests
- applying rate limiting only after or alongside authentication checks
- protecting downstream services from invalid traffic

This mirrors how many production systems offload common security concerns to a gateway layer.

---

### Monitoring and Observability

The project includes monitoring endpoints to make request traffic and throttling behavior visible.

#### Examples of metrics or visibility you may expose:
- total requests received
- throttled requests count
- active buckets / counters
- request distribution by client
- health status of Redis connection

These endpoints help demonstrate a backend engineer’s thinking beyond just request handling.

---
### Example Use Cases

This gateway design can be extended for real-world scenarios such as:
- protecting public REST APIs
- controlling traffic to microservices
- enforcing per-user or per-IP quotas
- securing internal platform APIs
- acting as a lightweight edge gateway for SaaS applications

---
### Getting Started
#### Prerequisites

Make sure you have installed:
- Java 17+
- Maven or Gradle
- Docker
- Redis
---
### Running Locally
#### 1. Clone the repository
```bash
     git clone https://github.com/jeet7122/api-gateway-with-limiter.git
     cd api-gateway-with-limiter
```

#### 2. Start Redis
Using Docker: 
```bash
  docker run -d --name redis-rate-limiter -p 6379:6379 redis
```

#### 3. Run the Spring Boot application
If using Maven:
```bash
  ./mvnw spring-boot:run
```
Or:
```bash
  mvn spring-boot:run
```

#### 4. Test the API's
Send repeated requests to the protected/test endpoint and observe throttling behavior.

Example:

- Public Endpoints
```bash
  GET /api/public/hello
  GET /api/public/token?username=<username>
  GET /api/health
```

- Protected Endpoints
    
    - Require Header: Authorization: Bearer <JWT>

```bash
   GET /api/private/hello
   GET /api/private/rate-limit/status
```

- Status is a monitoring endpoint

    - Example Response
```json
  {
      "clientId": "khushi",
      "tokensRemaining": 3,
      "capacity": 5,
      "refillTokens": 5,
      "refillDurationSeconds": 60
  }
```

If the limit is exceeded, the gateway should return:

```text
HTTP 429 Too Many Requests
```

### Sample Response for Rate Limit Exceeded

```json
{
  "timestamp": "2026-03-05T12:30:00Z",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please try again later."
}
```


---

### Configuration

Typical configuration values include:
- rate limit capacity
- token refill interval
- Redis host and port
- JWT secret / validation config
- protected route patterns

Example application.yml structure:

```yaml
server:
  port: 8080

spring:
  data:
    redis:
      host: localhost
      port: 6379

rate-limit:
  capacity: 5
  refill-tokens: 5
  refill-duration-seconds: 60
```

Update these fields to match your actual configuration.

---
### Example Request Flow

#### Allowed Request
1. Client sends request with valid JWT
2. Gateway validates token
3. Rate limiter checks available tokens
4. Tokens available → request forwarded
5. Success response returned

#### Rejected Request
1. Client sends repeated requests
2. Rate limiter detects token exhaustion
3. Gateway rejects request
4. Returns 429 Too Many Requests

---



---
### Docker Support

This project can be containerized for easier local setup and deployment.

#### Build Docker image
```bash
  docker build -t api-gateway-with-limiter .
```

##### Run Container
```bash
  docker run -p 8080:8080 api-gateway-with-limiter
```

You can also use Docker Compose to run the gateway alongside Redis.

#### Example

```yaml
version: '3.8'

services:
  redis:
    image: redis:7.2
    container_name: api-gateway-redis
    ports:
      - "6379:6379"

  app:
    build: .
    container_name: api-gateway-app
    depends_on:
      - redis
    ports:
      - "8080:8080"
```

---

### Engineering Decisions

#### Why build this project?
I built this project to deepen my understanding of backend infrastructure patterns beyond standard CRUD APIs. I wanted to work on a system that demonstrates:
- gateway middleware design
- distributed rate limiting
- request control under load
- Redis-based state management
- security filtering at the edge

#### Why this matters for backend roles
This project reflects backend engineering concerns commonly seen in:
- SaaS platforms
- API-heavy systems
- fintech platforms
- microservice environments
- infrastructure and platform teams

---

### Possible Future Improvements
There are several extensions that can make this gateway more production-like:
- per-user and per-IP based rate limits
- sliding window rate limiting strategy
- API key support
- route-based rate limiting policies
- structured logging and tracing
- metrics integration with Prometheus / Grafana
- gateway routing to downstream microservices
- circuit breaker / resilience patterns
- distributed tracing support
- admin dashboard for live traffic visualization

---
### Learnings
This project helped strengthen my understanding of:
- how API Gateways protect downstream systems
- how Redis can support distributed infrastructure concerns
- how middleware pipelines are designed in backend systems
- how throttling strategies affect reliability and fairness
- how backend systems are built with scalability and maintainability in mind

---
### Author
Jeet Thakkar

- Github: https://github.com/jeet7122
- Portfolio: https://jeet7122.github.io
