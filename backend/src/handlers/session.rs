use crate::errors::AppError;
use crate::models::session::{NewSessionRequest, SessionFilterParams};
use crate::services::session as session_service;
use crate::DbPool;
use actix_web::{get, post, web, Error, HttpResponse};

#[post("/sessions")]
async fn create_session(
    pool: web::Data<DbPool>,
    session_data: web::Json<NewSessionRequest>,
) -> Result<HttpResponse, Error> {
    let session =
        web::block(move || session_service::create_session(&pool, session_data.into_inner()))
            .await?
            .map_err(|e| {
                eprintln!("Error creating session: {:?}", e);
                AppError::Database(e)
            })?;

    Ok(HttpResponse::Created().json(session))
}

#[get("/sessions")]
async fn get_sessions(
    pool: web::Data<DbPool>,
    query: web::Query<SessionFilterParams>,
) -> Result<HttpResponse, Error> {
    let sessions =
        web::block(move || session_service::get_all_sessions(&pool, Some(query.into_inner())))
            .await?
            .map_err(|e| {
                eprintln!("Error getting sessions: {:?}", e);
                AppError::Database(e)
            })?;

    Ok(HttpResponse::Ok().json(sessions))
}

#[get("/clients/{id}/sessions")]
async fn get_client_sessions(
    pool: web::Data<DbPool>,
    client_id: web::Path<i32>,
) -> Result<HttpResponse, Error> {
    let sessions =
        web::block(move || session_service::get_sessions_by_client(&pool, client_id.into_inner()))
            .await?
            .map_err(|e| {
                eprintln!("Error getting client sessions: {:?}", e);
                AppError::Database(e)
            })?;

    Ok(HttpResponse::Ok().json(sessions))
}

pub fn config(cfg: &mut web::ServiceConfig) {
    cfg.service(create_session)
        .service(get_sessions)
        .service(get_client_sessions);
}
