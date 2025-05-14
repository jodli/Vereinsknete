use actix_web::{error::ResponseError, http::StatusCode, HttpResponse};
use diesel::result::Error as DieselError;
use std::fmt;

#[derive(Debug)]
pub enum AppError {
    Database(DieselError),
    NotFound(String),
    InternalServer(String),
}

impl fmt::Display for AppError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            AppError::Database(error) => write!(f, "Database error: {}", error),
            AppError::NotFound(error) => write!(f, "Not found: {}", error),
            AppError::InternalServer(error) => write!(f, "Internal server error: {}", error),
        }
    }
}

impl ResponseError for AppError {
    fn error_response(&self) -> HttpResponse {
        match self {
            AppError::Database(error) => {
                HttpResponse::InternalServerError().json(format!("Database error: {}", error))
            }
            AppError::NotFound(error) => {
                HttpResponse::NotFound().json(format!("Not found: {}", error))
            }
            AppError::InternalServer(error) => HttpResponse::InternalServerError()
                .json(format!("Internal server error: {}", error)),
        }
    }

    fn status_code(&self) -> StatusCode {
        match self {
            AppError::Database(_) => StatusCode::INTERNAL_SERVER_ERROR,
            AppError::NotFound(_) => StatusCode::NOT_FOUND,
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
