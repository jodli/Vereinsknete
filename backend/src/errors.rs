use actix_web::{error::ResponseError, http::StatusCode, HttpResponse};
use diesel::result::Error as DieselError;
use serde::Serialize;
use std::fmt;

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

impl fmt::Display for AppError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            AppError::Database(error) => write!(f, "Database error: {}", error),
            AppError::NotFound(error) => write!(f, "Not found: {}", error),
            AppError::InternalServer(error) => write!(f, "Internal server error: {}", error),
            AppError::BadRequest(error) => write!(f, "Bad request: {}", error),
            AppError::Validation(error) => write!(f, "Validation error: {}", error),
            AppError::Unauthorized(error) => write!(f, "Unauthorized: {}", error),
            AppError::Forbidden(error) => write!(f, "Forbidden: {}", error),
        }
    }
}

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
