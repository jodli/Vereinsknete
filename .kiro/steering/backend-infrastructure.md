---
inclusion: fileMatch
fileMatchPattern: 'backend/src/{main.rs,middleware.rs,errors.rs}'
---

# Backend Infrastructure & Middleware Guidelines

This document provides guidelines for application infrastructure, middleware, error handling, and system configuration in the VereinsKnete backend.

## Application Architecture

### Environment Management
The application supports two environments with automatic configuration loading:

#### Development Environment
- Uses `.env.development` file
- Database: `vereinsknete.db` (local SQLite file)
- Logging: `RUST_LOG=debug` for verbose output
- Static files: Not served (frontend runs separately)
- Port: `8080` (configurable via `PORT` env var)

#### Production Environment  
- Uses `.env.production` file
- Database: `data/vereinsknete.db` (persistent volume mount)
- Logging: `RUST_LOG=info` for production-level logging
- Static files: Served from `./public` directory
- Port: `8080` (Docker-configurable)

### Environment Detection Pattern
```rust
#[derive(Debug, Clone)]
pub enum AppEnv {
    Dev,
    Prod,
}

impl AppEnv {
    pub fn from_env() -> Self {
        std::env::var("RUST_ENV")
            .ok()
            .and_then(|v| AppEnv::from_str(&v).ok())
            .unwrap_or(AppEnv::Dev)
    }
}

impl FromStr for AppEnv {
    type Err = String;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s.to_ascii_lowercase().as_str() {
            "dev" | "development" => Ok(AppEnv::Dev),
            "prod" | "production" => Ok(AppEnv::Prod),
            s => Err(format!("Invalid environment: {s}")),
        }
    }
}
```

## Middleware Architecture

### Request ID Middleware
Every request gets a unique UUID for tracking across the entire request lifecycle:

```rust
use actix_web::{
    dev::{forward_ready, Service, ServiceRequest, ServiceResponse, Transform},
    error::Error,
    http::header::{HeaderName, HeaderValue},
    HttpMessage,
};
use futures_util::future::LocalBoxFuture;
use serde_json::json;
use std::{
    future::{ready, Ready},
    rc::Rc,
    time::Instant,
};
use uuid::Uuid;

pub struct RequestIdMiddleware;

impl<S, B> Transform<S, ServiceRequest> for RequestIdMiddleware
where
    S: Service<ServiceRequest, Response = ServiceResponse<B>, Error = Error> + 'static,
    S::Future: 'static,
    B: 'static,
{
    type Response = ServiceResponse<B>;
    type Error = Error;
    type Transform = RequestIdMiddlewareService<S>;
    type InitError = ();
    type Future = Ready<Result<Self::Transform, Self::InitError>>;

    fn new_transform(&self, service: S) -> Self::Future {
        ready(Ok(RequestIdMiddlewareService {
            service: Rc::new(service),
        }))
    }
}

pub struct RequestIdMiddlewareService<S> {
    service: Rc<S>,
}

impl<S, B> Service<ServiceRequest> for RequestIdMiddlewareService<S>
where
    S: Service<ServiceRequest, Response = ServiceResponse<B>, Error = Error> + 'static,
    S::Future: 'static,
    B: 'static,
{
    type Response = ServiceResponse<B>;
    type Error = Error;
    type Future = LocalBoxFuture<'static, Result<Self::Response, Self::Error>>;

    forward_ready!(service);

    fn call(&self, mut req: ServiceRequest) -> Self::Future {
        let request_id = Uuid::new_v4().to_string();
        
        // Add request ID to headers
        req.headers_mut().insert(
            HeaderName::from_static("x-request-id"),
            HeaderValue::from_str(&request_id).unwrap(),
        );

        // Store request ID in extensions for handlers to access
        req.extensions_mut().insert(request_id.clone());

        let service = self.service.clone();
        Box::pin(async move {
            let start_time = Instant::now();
            let method = req.method().to_string();
            let path = req.path().to_string();
            
            let res = service.call(req).await?;
            let duration = start_time.elapsed();

            // Log request completion with structured logging
            log::info!(
                target: "http_requests",
                "{}",
                json!({
                    "request_id": request_id,
                    "method": method,
                    "path": path,
                    "status": res.status().as_u16(),
                    "duration_ms": duration.as_millis(),
                    "user_agent": res.request().headers().get("user-agent")
                        .and_then(|h| h.to_str().ok())
                        .unwrap_or("unknown")
                })
            );

            Ok(res)
        })
    }
}
```

### Security Headers Middleware
Automatically adds security headers to all responses:

```rust
pub struct SecurityHeadersMiddleware;

impl<S, B> Transform<S, ServiceRequest> for SecurityHeadersMiddleware
where
    S: Service<ServiceRequest, Response = ServiceResponse<B>, Error = Error> + 'static,
    S::Future: 'static,
    B: 'static,
{
    type Response = ServiceResponse<B>;
    type Error = Error;
    type Transform = SecurityHeadersMiddlewareService<S>;
    type InitError = ();
    type Future = Ready<Result<Self::Transform, Self::InitError>>;

    fn new_transform(&self, service: S) -> Self::Future {
        ready(Ok(SecurityHeadersMiddlewareService {
            service: Rc::new(service),
        }))
    }
}

pub struct SecurityHeadersMiddlewareService<S> {
    service: Rc<S>,
}

impl<S, B> Service<ServiceRequest> for SecurityHeadersMiddlewareService<S>
where
    S: Service<ServiceRequest, Response = ServiceResponse<B>, Error = Error> + 'static,
    S::Future: 'static,
    B: 'static,
{
    type Response = ServiceResponse<B>;
    type Error = Error;
    type Future = LocalBoxFuture<'static, Result<Self::Response, Self::Error>>;

    forward_ready!(service);

    fn call(&self, req: ServiceRequest) -> Self::Future {
        let service = self.service.clone();
        Box::pin(async move {
            let mut res = service.call(req).await?;
            
            let headers = res.headers_mut();
            
            // Prevent XSS attacks
            headers.insert(
                HeaderName::from_static("x-content-type-options"),
                HeaderValue::from_static("nosniff"),
            );
            
            // Prevent clickjacking
            headers.insert(
                HeaderName::from_static("x-frame-options"),
                HeaderValue::from_static("DENY"),
            );
            
            // XSS protection
            headers.insert(
                HeaderName::from_static("x-xss-protection"),
                HeaderValue::from_static("1; mode=block"),
            );
            
            // Content Security Policy
            headers.insert(
                HeaderName::from_static("content-security-policy"),
                HeaderValue::from_static(
                    "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'"
                ),
            );

            Ok(res)
        })
    }
}
```

## Error Handling System

### Centralized Error Types
```rust
#[derive(Debug)]
pub enum AppError {
    Database(DieselError),
    NotFound(String),
    InternalServer(String),
    BadRequest(String),
    Validation(String),
    #[allow(dead_code)]
    Unauthorized(String),
    #[allow(dead_code)]
    Forbidden(String),
}

#[derive(Serialize)]
pub struct ApiError {
    pub error: String,
    pub status: String,
    pub code: Option<String>,
    pub details: Option<serde_json::Value>,
}
```

### Error Response Implementation
```rust
impl ResponseError for AppError {
    fn error_response(&self) -> HttpResponse {
        let api_error = match self {
            AppError::Database(error) => ApiError {
                error: "Database error occurred".to_string(),
                status: "error".to_string(),
                code: Some("DATABASE_ERROR".to_string()),
                details: Some(serde_json::json!({"message": error.to_string()})),
            },
            AppError::NotFound(error) => ApiError {
                error: error.clone(),
                status: "error".to_string(),
                code: Some("NOT_FOUND".to_string()),
                details: None,
            },
            AppError::BadRequest(error) => ApiError {
                error: error.clone(),
                status: "error".to_string(),
                code: Some("BAD_REQUEST".to_string()),
                details: None,
            },
            AppError::Validation(error) => ApiError {
                error: error.clone(),
                status: "error".to_string(),
                code: Some("VALIDATION_ERROR".to_string()),
                details: None,
            },
            AppError::Unauthorized(error) => ApiError {
                error: error.clone(),
                status: "error".to_string(),
                code: Some("UNAUTHORIZED".to_string()),
                details: None,
            },
            AppError::Forbidden(error) => ApiError {
                error: error.clone(),
                status: "error".to_string(),
                code: Some("FORBIDDEN".to_string()),
                details: None,
            },
            AppError::InternalServer(error) => ApiError {
                error: "Internal server error".to_string(),
                status: "error".to_string(),
                code: Some("INTERNAL_SERVER_ERROR".to_string()),
                details: Some(serde_json::json!({"message": error})),
            },
        };

        match self {
            AppError::Database(_) => HttpResponse::InternalServerError().json(api_error),
            AppError::NotFound(_) => HttpResponse::NotFound().json(api_error),
            AppError::BadRequest(_) => HttpResponse::BadRequest().json(api_error),
            AppError::Validation(_) => HttpResponse::UnprocessableEntity().json(api_error),
            AppError::Unauthorized(_) => HttpResponse::Unauthorized().json(api_error),
            AppError::Forbidden(_) => HttpResponse::Forbidden().json(api_error),
            AppError::InternalServer(_) => HttpResponse::InternalServerError().json(api_error),
        }
    }

    fn status_code(&self) -> StatusCode {
        match self {
            AppError::Database(_) => StatusCode::INTERNAL_SERVER_ERROR,
            AppError::NotFound(_) => StatusCode::NOT_FOUND,
            AppError::BadRequest(_) => StatusCode::BAD_REQUEST,
            AppError::Validation(_) => StatusCode::UNPROCESSABLE_ENTITY,
            AppError::Unauthorized(_) => StatusCode::UNAUTHORIZED,
            AppError::Forbidden(_) => StatusCode::FORBIDDEN,
            AppError::InternalServer(_) => StatusCode::INTERNAL_SERVER_ERROR,
        }
    }
}

impl From<DieselError> for AppError {
    fn from(error: DieselError) -> Self {
        match error {
            DieselError::NotFound => AppError::NotFound("Record not found".to_string()),
            _ => AppError::Database(error),
        }
    }
}
```

## Application Configuration

### Main Application Setup
```rust
#[actix_web::main]
async fn main() -> std::io::Result<()> {
    // Load environment variables from appropriate .env file
    let env_mode = AppEnv::from_env();

    match env_mode {
        AppEnv::Prod => {
            if Path::new(".env.production").exists() {
                dotenvy::from_filename(".env.production").ok();
            } else {
                dotenv().ok();
            }
        }
        AppEnv::Dev => {
            if Path::new(".env.development").exists() {
                dotenvy::from_filename(".env.development").ok();
            } else {
                dotenv().ok();
            }
        }
    }

    // Initialize the logger
    env_logger::init_from_env(env_logger::Env::new().default_filter_or("info"));
    log::info!("Running in {:?} mode", env_mode);

    // Set up database connection pool
    let database_url = env::var("DATABASE_URL").unwrap_or_else(|_| "vereinsknete.db".to_string());
    log::info!("Using database URL: {}", database_url);
    let manager = ConnectionManager::<SqliteConnection>::new(database_url);
    let pool = r2d2::Pool::builder()
        .max_size(10)
        .min_idle(Some(1))
        .connection_timeout(Duration::from_secs(30))
        .build(manager)
        .expect("Failed to create pool");

    // Set the bind target (IP address and port) from environment variables
    let host = env::var("HOST").unwrap_or_else(|_| "0.0.0.0".to_string());
    let port = env::var("PORT")
        .unwrap_or_else(|_| "8080".to_string())
        .parse::<u16>()
        .unwrap_or(8080);
    let target = (host.as_str(), port);

    log::info!("Starting VereinsKnete server at http://{}:{}", target.0, target.1);

    // Environment-specific configuration
    let static_files_path = env::var("STATIC_FILES_PATH")
        .unwrap_or_else(|_| match env_mode {
            AppEnv::Dev => "../frontend/build".to_string(),
            AppEnv::Prod => "./public".to_string(),
        });
    let serve_static_files = matches!(env_mode, AppEnv::Prod) && Path::new(&static_files_path).exists();

    HttpServer::new(move || {
        let cors = match env_mode {
            AppEnv::Dev => Cors::default()
                .allow_any_origin()
                .allow_any_method()
                .allow_any_header()
                .max_age(3600),
            AppEnv::Prod => Cors::default()
                .allowed_origin("https://yourdomain.com")
                .allowed_methods(vec!["GET", "POST", "PUT", "DELETE"])
                .allowed_headers(vec!["Content-Type", "Authorization"])
                .max_age(3600),
        };

        let mut app = App::new()
            .wrap(Logger::default())
            .wrap(RequestIdMiddleware)
            .wrap(SecurityHeadersMiddleware)
            .wrap(cors)
            .app_data(web::Data::new(pool.clone()))
            .configure(handlers::health::config)  // Health checks
            .service(web::scope("/api")
                .configure(handlers::client::config)
                .configure(handlers::session::config)
                .configure(handlers::user_profile::config)
                .configure(handlers::invoice::config)
            );

        // Conditionally serve static files only in production
        if serve_static_files {
            app = app.service(fs::Files::new("/", &static_files_path).index_file("index.html"));
        }

        app
    })
    .bind(target)?
    .run()
    .await
}
```

## Health Checks & Monitoring

### Health Check Implementation
```rust
use std::collections::HashMap;
use std::time::Instant;

#[derive(Serialize)]
pub struct HealthStatus {
    pub status: String,
    pub timestamp: String,
    pub version: String,
    pub checks: HashMap<String, CheckResult>,
}

#[derive(Serialize)]
pub struct CheckResult {
    pub status: String,
    pub response_time_ms: u64,
    pub details: Option<String>,
}

#[get("/health")]
async fn health_check(pool: web::Data<DbPool>) -> Result<HttpResponse> {
    let mut checks = HashMap::new();
    
    // Database health check
    let db_start = Instant::now();
    let db_status = match check_database_health(&pool).await {
        Ok(_) => CheckResult {
            status: "healthy".to_string(),
            response_time_ms: db_start.elapsed().as_millis() as u64,
            details: None,
        },
        Err(e) => CheckResult {
            status: "unhealthy".to_string(),
            response_time_ms: db_start.elapsed().as_millis() as u64,
            details: Some(e.to_string()),
        },
    };
    checks.insert("database".to_string(), db_status);
    
    // Determine overall status
    let overall_status = if checks.values().all(|check| check.status == "healthy") {
        "healthy"
    } else {
        "unhealthy"
    };
    
    let health = HealthStatus {
        status: overall_status.to_string(),
        timestamp: chrono::Utc::now().to_rfc3339(),
        version: env!("CARGO_PKG_VERSION").to_string(),
        checks,
    };
    
    if overall_status == "healthy" {
        Ok(HttpResponse::Ok().json(health))
    } else {
        Ok(HttpResponse::ServiceUnavailable().json(health))
    }
}

#[get("/metrics")]
async fn metrics() -> Result<HttpResponse> {
    // Basic Prometheus-style metrics
    let metrics = format!(
        "# HELP http_requests_total Total number of HTTP requests\n\
         # TYPE http_requests_total counter\n\
         http_requests_total{{method=\"GET\",endpoint=\"/health\"}} 1\n\
         # HELP database_connections_active Active database connections\n\
         # TYPE database_connections_active gauge\n\
         database_connections_active 1\n"
    );
    
    Ok(HttpResponse::Ok()
        .content_type("text/plain; version=0.0.4; charset=utf-8")
        .body(metrics))
}

async fn check_database_health(pool: &DbPool) -> Result<(), diesel::result::Error> {
    use crate::schema::clients::dsl::*;
    use diesel::prelude::*;
    
    let pool_clone = pool.clone();
    let _count = web::block(move || {
        let mut conn = pool_clone.get().expect("Failed to get DB connection");
        clients.select(diesel::dsl::count_star()).first::<i64>(&mut conn)
    })
    .await
    .map_err(|_| diesel::result::Error::NotFound)?;
    
    Ok(())
}
```

## Logging Configuration

### Structured Logging Setup
```rust
// Initialize logger with environment-specific levels
env_logger::init_from_env(env_logger::Env::new().default_filter_or("info"));

// Log important events
log::info!("Starting VereinsKnete server at http://{}:{}", host, port);
log::warn!("Production mode but no static files found at {}", path);
log::error!("Database connection failed: {}", error);
```

### Logging Levels
- **ERROR**: Critical errors that require immediate attention
- **WARN**: Important issues that don't stop execution
- **INFO**: General application flow and important events
- **DEBUG**: Detailed information for development/troubleshooting

### Structured Logging Format
All logs use structured JSON format for better parsing and analysis:

```json
{
  "request_id": "uuid-here",
  "method": "GET",
  "path": "/api/clients",
  "status": 200,
  "duration_ms": 45,
  "user_agent": "browser-info"
}
```

## Security Configuration

### Environment-Specific CORS
```rust
let cors = match env_mode {
    AppEnv::Dev => Cors::default()
        .allow_any_origin()
        .allow_any_method()
        .allow_any_header()
        .max_age(3600),
    AppEnv::Prod => Cors::default()
        .allowed_origin("https://yourdomain.com")  // Configure for production
        .allowed_methods(vec!["GET", "POST", "PUT", "DELETE"])
        .allowed_headers(vec!["Content-Type", "Authorization"])
        .max_age(3600),
};
```

### Security Headers
All responses automatically include:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Content-Security-Policy: default-src 'self'`

## Environment Variables

### Required Variables
```bash
DATABASE_URL=path/to/database.db
RUST_ENV=development|production
```

### Optional Variables with Defaults
```bash
RUST_LOG=info
PORT=8080
HOST=0.0.0.0
STATIC_FILES_PATH=./public
```

This document should be referenced when working on application infrastructure, middleware, error handling, or system configuration.