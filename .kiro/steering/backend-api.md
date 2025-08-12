---
inclusion: fileMatch
fileMatchPattern: 'backend/src/handlers/*'
---

# Backend API & Handlers Guidelines

This document provides guidelines for HTTP handlers, API design, and request/response handling in the VereinsKnete backend.

## HTTP Handlers (Controllers)

### Handler Function Structure
```rust
use actix_web::{get, web, Error, HttpMessage, HttpRequest, HttpResponse};
use serde_json::json;

fn get_request_id(req: &HttpRequest) -> String {
    req.extensions()
        .get::<String>()
        .cloned()
        .unwrap_or_else(|| "unknown".to_string())
}

#[get("/entities")]
async fn get_entities(
    pool: web::Data<DbPool>,
    query: web::Query<FilterParams>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);
    
    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "get_entities",
            "message": "Fetching all entities"
        })
    );

    let entities = web::block(move || {
        entity_service::get_all_entities(&pool, query.into_inner())
    })
    .await?
    .map_err(|e| {
        log::error!(
            target: "business_logic",
            "{}",
            json!({
                "request_id": request_id,
                "action": "get_entities",
                "error": e.to_string(),
                "message": "Database error while fetching entities"
            })
        );
        AppError::Database(e)
    })?;

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "get_entities",
            "count": entities.len(),
            "message": "Successfully fetched entities"
        })
    );

    Ok(HttpResponse::Ok().json(entities))
}
```

### HTTP Method Conventions
- **GET** `/entities` - List all entities
- **GET** `/entities/{id}` - Get single entity by ID
- **POST** `/entities` - Create new entity
- **PUT** `/entities/{id}` - Update existing entity (full update)
- **PATCH** `/entities/{id}` - Partial update (if needed)
- **DELETE** `/entities/{id}` - Delete entity

### Response Status Codes
```rust
// Success responses
HttpResponse::Ok().json(data)           // 200 - GET, PUT success
HttpResponse::Created().json(data)      // 201 - POST success  
HttpResponse::NoContent().finish()      // 204 - DELETE success

// Error responses (handled by AppError)
HttpResponse::NotFound().json(msg)      // 404 - Resource not found
HttpResponse::BadRequest().json(msg)    // 400 - Invalid input
HttpResponse::UnprocessableEntity()     // 422 - Validation errors
HttpResponse::InternalServerError()     // 500 - Server errors
```

### Handler Registration Pattern
```rust
pub fn config(cfg: &mut web::ServiceConfig) {
    cfg.service(get_entities)
        .service(get_entity)
        .service(create_entity)
        .service(update_entity)
        .service(delete_entity);
}
```

### Path Parameter Extraction
```rust
#[get("/entities/{id}")]
async fn get_entity(
    pool: web::Data<DbPool>,
    entity_id: web::Path<i32>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);
    let id = entity_id.into_inner();
    
    // Validate path parameter
    if id <= 0 {
        log::warn!("Invalid entity ID in path: {}", id);
        return Err(AppError::BadRequest("Invalid entity ID".to_string()).into());
    }
    
    // Continue with handler logic...
}
```

### Request Body Handling with Validation
```rust
#[post("/entities")]
async fn create_entity(
    pool: web::Data<DbPool>,
    mut entity_data: web::Json<NewEntity>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);
    
    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "create_entity",
            "entity_name": entity_data.name,
            "message": "Creating new entity"
        })
    );

    // Validate and sanitize input
    if let Err(errors) = entity_data.validate_and_sanitize() {
        log::warn!(
            target: "business_logic",
            "{}",
            json!({
                "request_id": request_id,
                "action": "create_entity",
                "validation_errors": format!("{:?}", errors),
                "message": "Entity validation failed"
            })
        );
        return Err(AppError::Validation(format!("Validation failed: {:?}", errors)).into());
    }

    // Continue with business logic...
}
```

## Error Handling in Handlers

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
            AppError::Validation(error) => ApiError {
                error: error.clone(),
                status: "error".to_string(),
                code: Some("VALIDATION_ERROR".to_string()),
                details: None,
            },
            // ... other error types
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
}
```

### Handler Error Handling Pattern
```rust
let result = web::block(move || service_function(&pool, params))
    .await?
    .map_err(|e| {
        log::error!(
            target: "business_logic",
            "{}",
            json!({
                "request_id": request_id,
                "action": "operation_name",
                "error": e.to_string(),
                "message": "Database error during operation"
            })
        );
        AppError::Database(e)
    })?;
```

## RESTful API Design

### Resource Naming Conventions
```rust
// Good: Use plural nouns for collections
GET /api/clients           // Get all clients
GET /api/clients/123       // Get specific client
POST /api/clients          // Create new client
PUT /api/clients/123       // Update entire client
PATCH /api/clients/123     // Partial update client
DELETE /api/clients/123    // Delete client

// Nested resources for relationships
GET /api/clients/123/sessions     // Get sessions for client 123
POST /api/clients/123/sessions    // Create session for client 123

// Avoid: Verbs in URLs
GET /api/getClients        // Bad
POST /api/createClient     // Bad
```

### Query Parameter Patterns
```rust
#[derive(Debug, Deserialize, Clone)]
pub struct FilterParams {
    pub client_id: Option<i32>,
    pub start_date: Option<NaiveDate>,
    pub end_date: Option<NaiveDate>,
    pub status: Option<String>,
    #[serde(default)]
    pub limit: Option<i64>,
    #[serde(default)]
    pub offset: Option<i64>,
}

// Usage example
// GET /api/sessions?client_id=1&start_date=2024-01-01&limit=20
```

### JSON Response Format
```rust
// Success response with data
{
    "data": [...],
    "status": "success"
}

// Error response  
{
    "error": "Error message",
    "status": "error",
    "code": "ERROR_CODE",
    "details": { /* Additional context */ }
}

// For simple responses, return data directly
[...] // Array of entities
{...} // Single entity
```

## Request Validation

### Input Validation in Handlers
```rust
#[post("/entities")]
async fn create_entity(
    pool: web::Data<DbPool>,
    mut entity_data: web::Json<CreateEntityRequest>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);
    
    // Validate and sanitize input
    if let Err(errors) = entity_data.validate_and_sanitize() {
        log::warn!(
            target: "business_logic",
            "{}",
            json!({
                "request_id": request_id,
                "action": "create_entity",
                "validation_errors": format!("{:?}", errors),
                "message": "Entity validation failed"
            })
        );
        return Err(AppError::Validation(format!("Validation failed: {:?}", errors)).into());
    }
    
    // Continue with business logic...
}
```

## Structured Logging in Handlers

### Request Correlation Logging
```rust
// Business logic logging with request correlation
log::info!(
    target: "business_logic",
    "{}",
    json!({
        "request_id": request_id,
        "action": "create_client",
        "client_name": client_data.name,
        "message": "Creating new client"
    })
);

// Error logging with full context
log::error!(
    target: "business_logic",
    "{}",
    json!({
        "request_id": request_id,
        "action": "create_client",
        "error": e.to_string(),
        "message": "Database error while creating client"
    })
);
```

## Date/Time Handling
- Use ISO 8601 format for date strings: `"2024-01-15"`
- Use `chrono::NaiveDate` for dates without timezone
- Use `chrono::NaiveTime` for times without timezone
- Store dates as TEXT in SQLite for consistency

## Security in Handlers

### Input Sanitization
- Always validate and sanitize user inputs
- Use the validator crate for comprehensive validation
- Implement business logic validation in addition to format validation
- Log all validation failures for security monitoring

### SQL Injection Prevention
- Always use Diesel's query builder (never raw SQL with user input)
- Use parameterized queries for any dynamic SQL
- Validate and sanitize all user inputs

This document should be referenced when working on HTTP handlers, API endpoints, or request/response processing.