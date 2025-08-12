---
inclusion: manual
---

# Backend Testing & Deployment Guidelines

This document provides guidelines for testing, deployment, and production considerations for the VereinsKnete backend.

## Testing Strategy

### Unit Testing Structure
```rust
#[cfg(test)]
mod tests {
    use super::*;
    use diesel::r2d2::{self, ConnectionManager};
    use diesel::sqlite::SqliteConnection;

    fn setup_test_db() -> DbPool {
        let manager = ConnectionManager::<SqliteConnection>::new(":memory:");
        r2d2::Pool::builder().build(manager).expect("Failed to create pool")
    }

    #[test]
    fn test_create_entity() {
        let pool = setup_test_db();
        // Run migrations
        // Test entity creation
        
        let new_entity = NewEntity {
            name: "Test Entity".to_string(),
            rate: 50.0,
        };
        
        let result = create_entity(&pool, new_entity);
        assert!(result.is_ok());
        
        let entity = result.unwrap();
        assert_eq!(entity.name, "Test Entity");
        assert_eq!(entity.rate, 50.0);
    }

    #[test]
    fn test_validation_errors() {
        let pool = setup_test_db();
        
        let invalid_entity = NewEntity {
            name: "".to_string(),  // Invalid: empty name
            rate: -10.0,  // Invalid: negative rate
        };
        
        let result = create_entity(&pool, invalid_entity);
        assert!(result.is_err());
        
        // Check specific error type
        match result.unwrap_err() {
            diesel::result::Error::DatabaseError(kind, _) => {
                assert_eq!(kind, diesel::result::DatabaseErrorKind::CheckViolation);
            }
            _ => panic!("Expected CheckViolation error"),
        }
    }
}
```

### Integration Testing
```rust
#[cfg(test)]
mod integration_tests {
    use super::*;
    use actix_web::{test, web, App};

    #[actix_web::test]
    async fn test_create_client_endpoint() {
        let pool = setup_test_db();
        
        let app = test::init_service(
            App::new()
                .app_data(web::Data::new(pool))
                .configure(handlers::client::config)
        ).await;

        let new_client = NewClient {
            name: "Test Client".to_string(),
            address: "Test Address".to_string(),
            contact_person: Some("Test Contact".to_string()),
            default_hourly_rate: 50.0,
        };

        let req = test::TestRequest::post()
            .uri("/clients")
            .set_json(&new_client)
            .to_request();

        let resp = test::call_service(&app, req).await;
        assert!(resp.status().is_success());
    }

    #[actix_web::test]
    async fn test_validation_error_response() {
        let pool = setup_test_db();
        
        let app = test::init_service(
            App::new()
                .app_data(web::Data::new(pool))
                .configure(handlers::client::config)
        ).await;

        let invalid_client = serde_json::json!({
            "name": "",  // Invalid: empty name
            "address": "Test Address",
            "default_hourly_rate": -10.0  // Invalid: negative rate
        });

        let req = test::TestRequest::post()
            .uri("/clients")
            .set_json(&invalid_client)
            .to_request();

        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), 422);  // Unprocessable Entity
    }
}
```

### Test Data Management
```rust
// Test fixtures
pub fn create_test_client() -> NewClient {
    NewClient {
        name: "Test Client".to_string(),
        address: "123 Test Street, Test City".to_string(),
        contact_person: Some("Test Contact".to_string()),
        default_hourly_rate: 50.0,
    }
}

pub fn create_test_session(client_id: i32) -> NewSessionRequest {
    NewSessionRequest {
        client_id,
        name: "Test Session".to_string(),
        date: chrono::NaiveDate::from_ymd_opt(2024, 1, 15).unwrap(),
        start_time: chrono::NaiveTime::from_hms_opt(9, 0, 0).unwrap(),
        end_time: chrono::NaiveTime::from_hms_opt(17, 0, 0).unwrap(),
    }
}

// Test database setup with migrations
fn setup_test_db_with_migrations() -> DbPool {
    let manager = ConnectionManager::<SqliteConnection>::new(":memory:");
    let pool = r2d2::Pool::builder().build(manager).expect("Failed to create pool");
    
    // Run migrations
    let mut conn = pool.get().expect("Failed to get connection");
    diesel_migrations::run_pending_migrations(&mut conn).expect("Failed to run migrations");
    
    pool
}
```

### Error Testing Patterns
```rust
#[test]
fn test_business_logic_validation() {
    let pool = setup_test_db();
    
    // Test duplicate name validation
    let client1 = create_test_client();
    let result1 = create_client(&pool, client1);
    assert!(result1.is_ok());
    
    let client2 = create_test_client(); // Same name
    let result2 = create_client(&pool, client2);
    assert!(result2.is_err());
    
    match result2.unwrap_err() {
        diesel::result::Error::DatabaseError(kind, _) => {
            assert_eq!(kind, diesel::result::DatabaseErrorKind::UniqueViolation);
        }
        _ => panic!("Expected UniqueViolation error"),
    }
}

#[test]
fn test_foreign_key_constraints() {
    let pool = setup_test_db();
    
    // Try to create session with non-existent client
    let invalid_session = NewSessionRequest {
        client_id: 999, // Non-existent client
        name: "Test Session".to_string(),
        date: chrono::NaiveDate::from_ymd_opt(2024, 1, 15).unwrap(),
        start_time: chrono::NaiveTime::from_hms_opt(9, 0, 0).unwrap(),
        end_time: chrono::NaiveTime::from_hms_opt(17, 0, 0).unwrap(),
    };
    
    let result = create_session(&pool, invalid_session);
    assert!(result.is_err());
}
```

## Performance Testing

### Load Testing with Criterion
```rust
use criterion::{black_box, criterion_group, criterion_main, Criterion};

fn benchmark_client_creation(c: &mut Criterion) {
    let pool = setup_test_db_with_migrations();
    
    c.bench_function("create_client", |b| {
        b.iter(|| {
            let client = NewClient {
                name: format!("Client {}", black_box(rand::random::<u32>())),
                address: "Test Address".to_string(),
                contact_person: Some("Test Contact".to_string()),
                default_hourly_rate: 50.0,
            };
            
            create_client(&pool, client)
        })
    });
}

fn benchmark_client_query(c: &mut Criterion) {
    let pool = setup_test_db_with_migrations();
    
    // Setup test data
    for i in 0..1000 {
        let client = NewClient {
            name: format!("Client {}", i),
            address: "Test Address".to_string(),
            contact_person: Some("Test Contact".to_string()),
            default_hourly_rate: 50.0,
        };
        create_client(&pool, client).unwrap();
    }
    
    c.bench_function("get_all_clients", |b| {
        b.iter(|| get_all_clients(&pool))
    });
}

criterion_group!(benches, benchmark_client_creation, benchmark_client_query);
criterion_main!(benches);
```

## Docker Configuration

### Multi-stage Dockerfile
```dockerfile
# Multi-stage build for smaller production image
FROM rust:1.75 as builder

WORKDIR /app

# Copy dependency files first for better caching
COPY Cargo.toml Cargo.lock ./
COPY src ./src

# Build the application
RUN cargo build --release

# Production stage
FROM debian:bookworm-slim

# Install runtime dependencies
RUN apt-get update && apt-get install -y \
    sqlite3 \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Create app user
RUN useradd -r -s /bin/false appuser

# Copy the binary from builder stage
COPY --from=builder /app/target/release/backend /usr/local/bin/backend

# Create directories and set permissions
RUN mkdir -p /app/data /app/invoices && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

WORKDIR /app

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Run the application
CMD ["backend"]
```

### Docker Compose for Development
```yaml
version: '3.8'

services:
  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      - RUST_ENV=development
      - DATABASE_URL=/app/data/vereinsknete.db
      - RUST_LOG=debug
    volumes:
      - ./data:/app/data
      - ./invoices:/app/invoices
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

  # Optional: Add monitoring
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
```

## Production Deployment

### Environment Configuration
```bash
# Production environment variables
RUST_ENV=production
DATABASE_URL=/app/data/vereinsknete.db
RUST_LOG=info
PORT=8080
HOST=0.0.0.0
STATIC_FILES_PATH=/app/public

# Security considerations
CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

### Production Checklist
- [ ] Set `RUST_LOG=info` for production logging
- [ ] Configure proper database path with persistent storage
- [ ] Set up proper CORS origins (not `allow_any_origin()`)
- [ ] Enable HTTPS in production
- [ ] Set up database backups
- [ ] Configure health check endpoints
- [ ] Set up monitoring and alerting
- [ ] Implement log aggregation
- [ ] Configure reverse proxy (nginx/traefik)
- [ ] Set up SSL certificates
- [ ] Configure firewall rules
- [ ] Set up automated deployments
- [ ] Implement database migration strategy

### Database Migration Strategy
```rust
// Migration runner for production
use diesel_migrations::{embed_migrations, EmbeddedMigrations, MigrationHarness};

const MIGRATIONS: EmbeddedMigrations = embed_migrations!("migrations/");

pub fn run_migrations(connection: &mut SqliteConnection) -> Result<(), Box<dyn std::error::Error + Send + Sync + 'static>> {
    connection.run_pending_migrations(MIGRATIONS)?;
    Ok(())
}

// In main.rs for production
if matches!(env_mode, AppEnv::Prod) {
    let mut conn = pool.get().expect("Failed to get DB connection");
    run_migrations(&mut conn).expect("Failed to run migrations");
    log::info!("Database migrations completed");
}
```

### Monitoring & Alerting
```yaml
# Prometheus configuration
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'vereinsknete-backend'
    static_configs:
      - targets: ['backend:8080']
    metrics_path: '/metrics'
    scrape_interval: 30s

rule_files:
  - "alert_rules.yml"

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093
```

### Alert Rules
```yaml
# alert_rules.yml
groups:
  - name: vereinsknete
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }} errors per second"

      - alert: DatabaseDown
        expr: up{job="vereinsknete-backend"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "VereinsKnete backend is down"
          description: "The backend service has been down for more than 1 minute"

      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m])) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time"
          description: "95th percentile response time is {{ $value }} seconds"
```

### Backup Strategy
```bash
#!/bin/bash
# backup.sh - Database backup script

BACKUP_DIR="/app/backups"
DB_PATH="/app/data/vereinsknete.db"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/vereinsknete_$TIMESTAMP.db"

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

# Create backup
sqlite3 "$DB_PATH" ".backup $BACKUP_FILE"

# Compress backup
gzip "$BACKUP_FILE"

# Keep only last 30 days of backups
find "$BACKUP_DIR" -name "vereinsknete_*.db.gz" -mtime +30 -delete

echo "Backup completed: ${BACKUP_FILE}.gz"
```

### Log Management
```yaml
# docker-compose.prod.yml
version: '3.8'

services:
  backend:
    # ... other config
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
    
  # Log aggregation with ELK stack
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.8.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data

  logstash:
    image: docker.elastic.co/logstash/logstash:8.8.0
    volumes:
      - ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    depends_on:
      - elasticsearch

  kibana:
    image: docker.elastic.co/kibana/kibana:8.8.0
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    depends_on:
      - elasticsearch

volumes:
  elasticsearch_data:
```

### Security Hardening
```dockerfile
# Security-hardened Dockerfile
FROM rust:1.75 as builder
# ... build stage

FROM gcr.io/distroless/cc-debian12

# Copy only necessary files
COPY --from=builder /app/target/release/backend /backend

# Use non-root user (distroless already provides this)
USER nonroot:nonroot

EXPOSE 8080

ENTRYPOINT ["/backend"]
```

This document should be referenced when working on testing, deployment, or production operations for the VereinsKnete backend.