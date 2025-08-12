use crate::errors::AppError;
use crate::models::session::{NewSessionRequest, SessionFilterParams, UpdateSessionRequest};
use crate::services::session as session_service;
use crate::DbPool;
use actix_web::{delete, get, post, put, web, Error, HttpMessage, HttpRequest, HttpResponse};
use serde_json::json;

fn get_request_id(req: &HttpRequest) -> String {
    req.extensions()
        .get::<String>()
        .cloned()
        .unwrap_or_else(|| "unknown".to_string())
}

#[post("/sessions")]
async fn create_session(
    pool: web::Data<DbPool>,
    mut session_data: web::Json<NewSessionRequest>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "create_session",
            "client_id": session_data.client_id,
            "session_name": session_data.name,
            "message": "Creating new session"
        })
    );

    // Validate and sanitize input
    if let Err(errors) = session_data.validate_and_sanitize() {
        log::warn!(
            target: "business_logic",
            "{}",
            json!({
                "request_id": request_id,
                "action": "create_session",
                "validation_errors": format!("{:?}", errors),
                "message": "Session validation failed"
            })
        );
        return Err(AppError::Validation(format!("Validation failed: {:?}", errors)).into());
    }

    let session =
        web::block(move || session_service::create_session(&pool, session_data.into_inner()))
            .await?
            .map_err(|e| {
                log::error!(
                    target: "business_logic",
                    "{}",
                    json!({
                        "request_id": request_id,
                        "action": "create_session",
                        "error": e.to_string(),
                        "message": "Database error while creating session"
                    })
                );
                AppError::Database(e)
            })?;

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "create_session",
            "session_id": session.id,
            "message": "Session created successfully"
        })
    );

    Ok(HttpResponse::Created().json(session))
}

#[get("/sessions")]
async fn get_sessions(
    pool: web::Data<DbPool>,
    query: web::Query<SessionFilterParams>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);

    let query_params = query.clone();
    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "get_sessions",
            "filters": format!("{:?}", query_params.into_inner()),
            "message": "Fetching sessions with filters"
        })
    );

    let sessions =
        web::block(move || session_service::get_all_sessions(&pool, Some(query.into_inner())))
            .await?
            .map_err(|e| {
                log::error!(
                    target: "business_logic",
                    "{}",
                    json!({
                        "request_id": request_id,
                        "action": "get_sessions",
                        "error": e.to_string(),
                        "message": "Database error while fetching sessions"
                    })
                );
                AppError::Database(e)
            })?;

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "get_sessions",
            "count": sessions.len(),
            "message": "Successfully fetched sessions"
        })
    );

    Ok(HttpResponse::Ok().json(sessions))
}

#[get("/sessions/{id}")]
async fn get_session(
    pool: web::Data<DbPool>,
    session_id: web::Path<i32>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);
    let session_id = session_id.into_inner();

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "get_session",
            "session_id": session_id,
            "message": "Fetching session details"
        })
    );

    let session = web::block(move || session_service::get_session_by_id(&pool, session_id))
        .await?
        .map_err(|e| {
            log::error!(
                target: "business_logic",
                "{}",
                json!({
                    "request_id": request_id,
                    "action": "get_session",
                    "session_id": session_id,
                    "error": e.to_string(),
                    "message": "Database error while fetching session"
                })
            );
            AppError::Database(e)
        })?;

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "get_session",
            "session_id": session_id,
            "message": "Session fetched successfully"
        })
    );

    Ok(HttpResponse::Ok().json(session))
}

#[put("/sessions/{id}")]
async fn update_session(
    pool: web::Data<DbPool>,
    session_id: web::Path<i32>,
    mut session_data: web::Json<UpdateSessionRequest>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);
    let session_id = session_id.into_inner();

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "update_session",
            "session_id": session_id,
            "message": "Updating session"
        })
    );

    // Validate and sanitize input
    if let Err(errors) = session_data.validate_and_sanitize() {
        log::warn!(
            target: "business_logic",
            "{}",
            json!({
                "request_id": request_id,
                "action": "update_session",
                "session_id": session_id,
                "validation_errors": format!("{:?}", errors),
                "message": "Session validation failed"
            })
        );
        return Err(AppError::Validation(format!("Validation failed: {:?}", errors)).into());
    }

    let session = web::block(move || {
        session_service::update_session(&pool, session_id, session_data.into_inner())
    })
    .await?
    .map_err(|e| {
        log::error!(
            target: "business_logic",
            "{}",
            json!({
                "request_id": request_id,
                "action": "update_session",
                "session_id": session_id,
                "error": e.to_string(),
                "message": "Database error while updating session"
            })
        );
        AppError::Database(e)
    })?;

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "update_session",
            "session_id": session_id,
            "message": "Session updated successfully"
        })
    );

    Ok(HttpResponse::Ok().json(session))
}

#[delete("/sessions/{id}")]
async fn delete_session(
    pool: web::Data<DbPool>,
    session_id: web::Path<i32>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);
    let session_id = session_id.into_inner();

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "delete_session",
            "session_id": session_id,
            "message": "Deleting session"
        })
    );

    web::block(move || session_service::delete_session(&pool, session_id))
        .await?
        .map_err(|e| {
            log::error!(
                target: "business_logic",
                "{}",
                json!({
                    "request_id": request_id,
                    "action": "delete_session",
                    "session_id": session_id,
                    "error": e.to_string(),
                    "message": "Database error while deleting session"
                })
            );
            AppError::Database(e)
        })?;

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "delete_session",
            "session_id": session_id,
            "message": "Session deleted successfully"
        })
    );

    Ok(HttpResponse::NoContent().finish())
}

#[get("/clients/{id}/sessions")]
async fn get_client_sessions(
    pool: web::Data<DbPool>,
    client_id: web::Path<i32>,
    req: HttpRequest,
) -> Result<HttpResponse, Error> {
    let request_id = get_request_id(&req);
    let client_id = client_id.into_inner();

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "get_client_sessions",
            "client_id": client_id,
            "message": "Fetching sessions for client"
        })
    );

    let sessions = web::block(move || session_service::get_sessions_by_client(&pool, client_id))
        .await?
        .map_err(|e| {
            log::error!(
                target: "business_logic",
                "{}",
                json!({
                    "request_id": request_id,
                    "action": "get_client_sessions",
                    "client_id": client_id,
                    "error": e.to_string(),
                    "message": "Database error while fetching client sessions"
                })
            );
            AppError::Database(e)
        })?;

    log::info!(
        target: "business_logic",
        "{}",
        json!({
            "request_id": request_id,
            "action": "get_client_sessions",
            "client_id": client_id,
            "count": sessions.len(),
            "message": "Successfully fetched client sessions"
        })
    );

    Ok(HttpResponse::Ok().json(sessions))
}

pub fn config(cfg: &mut web::ServiceConfig) {
    cfg.service(create_session)
        .service(get_sessions)
        .service(get_session)
        .service(update_session)
        .service(delete_session)
        .service(get_client_sessions);
}
