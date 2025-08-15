# Design Document

## Overview

This design document outlines the architecture and implementation approach for packaging VereinsKnete as a Home Assistant add-on. The solution will containerize the existing Rust+React fullstack application using Docker multi-stage builds, integrate with Home Assistant's ingress system for seamless web access, and provide proper configuration management through the Supervisor interface.

The design follows Home Assistant's add-on best practices, emphasizing security, user experience, and maintainability. The add-on will support multiple architectures and use pre-built containers for optimal installation performance.

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Home Assistant                           │
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │   Supervisor    │    │         Ingress Proxy           │ │
│  │                 │    │                                 │ │
│  │ ┌─────────────┐ │    │  ┌─────────────────────────────┐│ │
│  │ │ VereinsKnete│ │◄───┤  │    Web Interface Access    ││ │
│  │ │   Add-on    │ │    │  │   (No separate auth needed) ││ │
│  │ │             │ │    │  └─────────────────────────────┘│ │
│  │ └─────────────┘ │    └─────────────────────────────────┘ │
│  └─────────────────┘                                        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│              VereinsKnete Container                         │
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │  Rust Backend   │    │      React Frontend             │ │
│  │  (Actix-web)    │    │      (Static Files)             │ │
│  │                 │    │                                 │ │
│  │ • API Endpoints │    │ • Built React App              │ │
│  │ • Static Server │    │ • Optimized Bundle             │ │
│  │ • Database      │    │ • Responsive UI                │ │
│  └─────────────────┘    └─────────────────────────────────┘ │
│                              │                             │
│                              ▼                             │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              Persistent Data                            │ │
│  │  • SQLite Database (/data/vereinsknete.db)             │ │
│  │  • Generated Invoices (/data/invoices/)                │ │
│  │  • Configuration (/data/options.json)                  │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Container Architecture

The add-on will use a multi-stage Docker build approach:

1. **Rust Build Stage**: Compile the backend application
2. **Node.js Build Stage**: Build the React frontend
3. **Runtime Stage**: Combine both into a minimal runtime container

### Data Flow

1. **User Access**: Users access VereinsKnete through Home Assistant's web interface
2. **Ingress Routing**: Home Assistant's ingress proxy routes requests to the add-on container
3. **Backend Processing**: Rust backend handles API requests and serves static files
4. **Data Persistence**: All data is stored in Home Assistant's persistent data volumes
5. **Configuration**: Add-on configuration is managed through Home Assistant's interface

## Components and Interfaces

### Add-on Configuration Interface

**config.yaml Structure:**
```yaml
name: "VereinsKnete"
version: "1.0.0"
slug: "vereinsknete"
description: "Freelance time tracking and invoicing application"
url: "https://github.com/your-username/vereinsknete-addon"
arch:
  - aarch64
  - amd64
startup: application
boot: manual
ingress: true
ingress_port: 8080
ingress_entry: "/"
panel_icon: "mdi:clock-time-four"
panel_title: "VereinsKnete"
webui: "http://[HOST]:[PORT:8080]"
options:
  database_path: "/data/vereinsknete.db"
  log_level: "info"
  port: 8080
  invoice_storage_path: "/data/invoices"
schema:
  database_path: "str"
  log_level: "list(debug|info|warn|error)"
  port: "port"
  invoice_storage_path: "str"
image: "ghcr.io/your-username/vereinsknete-addon-{arch}"
```

### Docker Container Interface

**Multi-stage Dockerfile:**
```dockerfile
# Rust build stage
FROM rust:1.70-alpine as rust-builder
RUN apk add --no-cache musl-dev sqlite-dev
WORKDIR /backend
COPY backend/ .
RUN cargo build --release

# Node.js build stage  
FROM node:18-alpine as frontend-builder
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm ci --only=production
COPY frontend/ .
RUN npm run build

# Runtime stage
ARG BUILD_FROM
FROM $BUILD_FROM
RUN apk add --no-cache sqlite libgcc
COPY --from=rust-builder /backend/target/release/vereinsknete /usr/local/bin/
COPY --from=frontend-builder /frontend/build/ /app/static/
COPY run.sh /
RUN chmod a+x /run.sh
EXPOSE 8080
CMD ["/run.sh"]
```

### Startup Script Interface

**run.sh Structure:**
```bash
#!/usr/bin/with-contenv bashio

# Configuration reading
CONFIG_PATH="/data/options.json"
DATABASE_PATH=$(bashio::config 'database_path')
LOG_LEVEL=$(bashio::config 'log_level')
PORT=$(bashio::config 'port')
INVOICE_PATH=$(bashio::config 'invoice_storage_path')

# Environment setup
export RUST_LOG="$LOG_LEVEL"
export DATABASE_URL="sqlite://$DATABASE_PATH"
export PORT="$PORT"
export INVOICE_STORAGE_PATH="$INVOICE_PATH"

# Directory creation
mkdir -p "$(dirname "$DATABASE_PATH")"
mkdir -p "$INVOICE_PATH"

# Database migration
if [ ! -f "$DATABASE_PATH" ]; then
    bashio::log.info "Initializing database..."
    # Database initialization logic
fi

# Start application
bashio::log.info "Starting VereinsKnete on port $PORT"
exec /usr/local/bin/vereinsknete \
    --database-url="$DATABASE_URL" \
    --port="$PORT" \
    --static-dir="/app/static" \
    --invoice-dir="$INVOICE_PATH"
```

### Backend Application Interface

**Rust Application Modifications:**
- Accept command-line arguments for configuration
- Serve static files from configurable directory
- Use configurable database path
- Support graceful shutdown signals
- Implement proper logging integration

### Frontend Integration Interface

**React Application Considerations:**
- Build process must generate static files
- API calls should use relative URLs for ingress compatibility
- No separate authentication needed (handled by Home Assistant)
- Responsive design for various screen sizes

## Data Models

### Configuration Data Model

```typescript
interface AddOnConfiguration {
  database_path: string;        // Path to SQLite database
  log_level: 'debug' | 'info' | 'warn' | 'error';
  port: number;                 // Application port (default: 8080)
  invoice_storage_path: string; // Directory for generated invoices
}
```

### Persistent Data Model

```
/data/
├── vereinsknete.db          # SQLite database (existing schema)
├── invoices/                # Generated PDF invoices
│   ├── invoice_2025-0001.pdf
│   └── invoice_2025-0002.pdf
├── options.json             # Home Assistant configuration
└── logs/                    # Application logs (optional)
```

### Build Data Model

```typescript
interface BuildConfiguration {
  architectures: string[];     // Supported architectures
  base_images: {              // Architecture-specific base images
    [arch: string]: string;
  };
  registry: string;           // Container registry URL
  image_name: string;         // Base image name template
}
```

## Error Handling

### Configuration Validation

**Strategy**: Implement comprehensive validation using Home Assistant's JSON schema system.

**Error Scenarios**:
- Invalid database path (not writable)
- Invalid port number (out of range, already in use)
- Invalid log level (not in allowed list)
- Missing required directories

**Handling Approach**:
```yaml
# Schema validation in config.yaml
schema:
  database_path: "str"
  log_level: "list(debug|info|warn|error)"
  port: "port"
  invoice_storage_path: "str"
```

### Runtime Error Handling

**Database Errors**:
- Connection failures: Retry with exponential backoff
- Migration failures: Log detailed error and prevent startup
- Disk space issues: Alert user through logs

**Network Errors**:
- Port binding failures: Log error with suggested solutions
- Ingress connectivity issues: Provide troubleshooting information

**Application Errors**:
- Rust panic handling: Graceful shutdown with error logging
- Frontend build issues: Clear error messages during build process

### Logging Strategy

**Log Levels**:
- `DEBUG`: Detailed application flow, SQL queries
- `INFO`: Startup, shutdown, configuration changes
- `WARN`: Recoverable errors, deprecated features
- `ERROR`: Critical errors, startup failures

**Log Format**:
```
[TIMESTAMP] [LEVEL] [COMPONENT] MESSAGE
2025-01-14T10:30:00Z [INFO] [ADDON] Starting VereinsKnete v1.0.0
2025-01-14T10:30:01Z [INFO] [DATABASE] Connected to /data/vereinsknete.db
2025-01-14T10:30:02Z [INFO] [SERVER] Listening on 0.0.0.0:8080
```

## Testing Strategy

### Local Development Testing

**VS Code Devcontainer Setup**:
- Use Home Assistant devcontainer template
- Mount add-on directory for live development
- Access Home Assistant on localhost:8123
- Test ingress integration in realistic environment

**Testing Workflow**:
1. Develop add-on in devcontainer environment
2. Test configuration changes through HA interface
3. Verify ingress functionality
4. Test data persistence across restarts

### Build Testing

**Multi-Architecture Builds**:
```bash
# Test build for specific architecture
docker buildx build \
  --platform linux/amd64 \
  --build-arg BUILD_FROM="ghcr.io/home-assistant/amd64-base:latest" \
  -t vereinsknete-addon:amd64 .

# Test container functionality
docker run --rm \
  -v /tmp/test_data:/data \
  -p 8080:8080 \
  vereinsknete-addon:amd64
```

**Automated Testing Pipeline**:
- Build verification for all architectures
- Configuration validation testing
- Integration testing with Home Assistant
- Security scanning of container images

### Integration Testing

**Home Assistant Integration**:
- Install add-on in test Home Assistant instance
- Verify ingress proxy functionality
- Test configuration management
- Validate data persistence
- Check security rating and permissions

**User Acceptance Testing**:
- Test installation process from repository
- Verify all VereinsKnete features work correctly
- Test backup and restore functionality
- Validate multi-architecture compatibility

### Performance Testing

**Container Performance**:
- Startup time measurement
- Memory usage monitoring
- CPU utilization tracking
- Disk I/O performance

**Application Performance**:
- API response time testing
- Frontend loading performance
- Database query optimization
- Large dataset handling

## Security Considerations

### Home Assistant Security Model

**Ingress Integration**:
- Use `ingress: true` for +2 security rating
- No separate authentication required
- Automatic HTTPS through Home Assistant
- Session management handled by HA

**Permissions Model**:
- Minimal required permissions
- No host network access needed
- No privileged capabilities required
- Standard data directory access only

### Container Security

**Base Image Security**:
- Use official Home Assistant base images
- Regular security updates through automated builds
- Minimal attack surface with Alpine Linux

**Application Security**:
- Input validation for all configuration options
- SQL injection prevention (using Diesel ORM)
- XSS protection in React frontend
- Secure file handling for invoice generation

### Data Security

**Database Security**:
- SQLite file permissions restricted to container
- No external database connections required
- Backup integration with Home Assistant

**File System Security**:
- Restricted access to /data directory only
- Proper file permissions for generated invoices
- No access to Home Assistant configuration files

This design provides a comprehensive approach to packaging VereinsKnete as a Home Assistant add-on while maintaining security, performance, and user experience standards.