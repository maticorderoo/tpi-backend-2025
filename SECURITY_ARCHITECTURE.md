# Security Architecture

## Overview

This document describes the security architecture for the TPI Backend 2025 microservices system. The architecture implements a layered security approach with the API Gateway as the primary security checkpoint for client requests, while allowing secure inter-service communication.

## Architecture

### API Gateway as Security Layer

The API Gateway (`api-gateway`) serves as the single entry point for all client requests and enforces authentication and authorization using:

- **JWT (JSON Web Tokens)** for authentication
- **Keycloak** as the identity provider
- **Role-Based Access Control (RBAC)** for authorization

#### Supported Roles

- `CLIENTE`: End users who create and track orders
- `OPERADOR`: Administrative users with full management capabilities
- `TRANSPORTISTA`: Drivers who execute route segments

#### Gateway Security Configuration

Located in: `api-gateway/src/main/java/com/tpibackend/gateway/config/SecurityConfig.java`

Key configuration:
```java
.pathMatchers("/api/orders/**").hasAnyRole("CLIENTE", "OPERADOR")
.pathMatchers("/api/logistics/tramos/**").hasAnyRole("OPERADOR", "TRANSPORTISTA")
.pathMatchers("/api/logistics/**").hasRole("OPERADOR")
.pathMatchers("/api/fleet/**").hasRole("OPERADOR")
```

### Microservices Security

Each microservice (orders-service, logistics-service, fleet-service) implements defense-in-depth security:

1. **JWT validation** for requests coming through the Gateway
2. **Permit-all endpoints** for inter-service communication
3. **Network-level isolation** (recommended for production)

#### Inter-Service Communication Endpoints

The following endpoints are configured with `permitAll()` to allow direct inter-service calls without authentication:

**Orders Service:**
- `PUT /orders/{id}/estado` - Used by logistics-service to update order status
- `PUT /orders/{id}/costo` - Used by logistics-service to update order cost

**Logistics Service:**
- `GET /logistics/rutas/solicitud/{id}` - Used by orders-service to fetch route information

**Fleet Service:**
- `GET /fleet/trucks/{id}` - Used by logistics-service to fetch truck information
- `PUT /fleet/trucks/{id}/disponibilidad` - Used by logistics-service to update truck availability
- `GET /fleet/metrics/promedios` - Used by orders-service for cost estimation

## Request Flows

### Client Request Flow

1. **Client → API Gateway**
   - Client sends request with JWT token in `Authorization` header
   - Example: `Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...`

2. **API Gateway Validation**
   - Validates JWT signature with Keycloak
   - Extracts roles from token claims
   - Enforces role-based access control based on endpoint
   - Returns 401 (Unauthorized) if token is invalid
   - Returns 403 (Forbidden) if user lacks required role

3. **API Gateway → Microservice**
   - Forwards request to target microservice
   - Includes original `Authorization` header
   - Microservice may optionally re-validate JWT (defense in depth)

4. **Microservice Processing**
   - Processes request
   - Returns response through Gateway to client

### Inter-Service Request Flow

1. **Source Microservice → Target Microservice**
   - Direct HTTP call without authentication
   - Example: `orders-service` calls `logistics-service` to fetch route

2. **Target Microservice Processing**
   - SecurityFilterChain matches request path against `permitAll()` patterns
   - Bypasses authentication requirement
   - Processes request
   - Returns response directly to source microservice

## Security Considerations

### Production Deployment

For production environments, implement the following additional security measures:

1. **Network Isolation**
   - Deploy microservices in a private network (VPC, Docker network)
   - Only expose API Gateway to the public internet
   - Use firewall rules to restrict direct access to microservices

2. **Service Mesh (Optional)**
   - Consider using a service mesh (e.g., Istio, Linkerd) for:
     - Mutual TLS (mTLS) between services
     - Fine-grained traffic policies
     - Service-to-service authentication

3. **API Gateway**
   - Use rate limiting to prevent abuse
   - Implement request logging and monitoring
   - Configure CORS appropriately for web clients

### Development vs Production Profiles

The system supports different security profiles:

- **`dev`, `dev-docker`, `dev-postgres`**: Security disabled for easier development
- **Production (default profile)**: Full security enabled

## Configuration

### API Gateway

- **Application**: `api-gateway/src/main/resources/application.yml`
- **Security**: `api-gateway/src/main/java/com/tpibackend/gateway/config/SecurityConfig.java`
- **Keycloak**: JWT issuer URI configured via environment variable `KEYCLOAK_ISSUER_URI`

### Microservices

Each microservice has:
- **Security Config**: `src/main/java/com/tpibackend/{service}/config/SecurityConfig.java`
- **Keycloak Config**: JWT validation configured in `application.properties`
- **Permit-all Endpoints**: Defined in SecurityFilterChain configuration

## Testing

### Security Tests

Each microservice includes security tests to verify:
- Endpoints requiring authentication reject unauthenticated requests
- Role-based access control works correctly
- Permit-all endpoints are accessible without authentication

**Note**: Some pre-existing security tests may fail due to test setup issues. This is a known issue unrelated to the security architecture implementation.

### Manual Testing

Use Postman collections included in the repository:
- `TPI-2025-COMPLETE.postman_collection.json`
- `TPI-2025.gateway-dev.postman_environment.json`

## Monitoring and Logging

- All requests through the Gateway are logged (see `RequestLoggingFilter`)
- Failed authentication attempts are logged at microservice level
- Monitor `401` and `403` responses for potential security issues

## References

- [Keycloak Documentation](KEYCLOAK.md)
- [API Gateway Documentation](README.md#api-gateway)
- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
