use crate::DbPool;
use actix_web::{get, web, HttpResponse, Result};
use serde::Serialize;
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
    let metrics = "# HELP http_requests_total Total number of HTTP requests\n\
         # TYPE http_requests_total counter\n\
         http_requests_total{{method=\"GET\",endpoint=\"/health\"}} 1\n\
         # HELP database_connections_active Active database connections\n\
         # TYPE database_connections_active gauge\n\
         database_connections_active 1\n"
        .to_string();

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
        clients
            .select(diesel::dsl::count_star())
            .first::<i64>(&mut conn)
    })
    .await
    .map_err(|_| diesel::result::Error::NotFound)?;

    Ok(())
}

pub fn config(cfg: &mut web::ServiceConfig) {
    cfg.service(health_check).service(metrics);
}
