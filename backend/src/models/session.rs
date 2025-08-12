use chrono::{NaiveDate, NaiveTime};
use diesel::prelude::*;
use serde::{Deserialize, Serialize};
use validator::Validate;

#[derive(Debug, Serialize, Deserialize, Queryable, Selectable)]
#[diesel(table_name = crate::schema::sessions)]
#[diesel(check_for_backend(diesel::sqlite::Sqlite))]
pub struct Session {
    pub id: i32,
    pub client_id: i32,
    pub name: String,
    pub date: String,
    pub start_time: String,
    pub end_time: String,
    pub created_at: String,
}

#[derive(Debug, Deserialize, Validate)]
pub struct NewSessionRequest {
    #[validate(range(min = 1, message = "Client ID must be positive"))]
    pub client_id: i32,

    #[validate(length(
        min = 1,
        max = 200,
        message = "Session name must be between 1 and 200 characters"
    ))]
    pub name: String,

    pub date: NaiveDate,
    pub start_time: NaiveTime,
    pub end_time: NaiveTime,
}

#[derive(Debug, Insertable)]
#[diesel(table_name = crate::schema::sessions)]
pub struct NewSession {
    pub client_id: i32,
    pub name: String,
    pub date: String,
    pub start_time: String,
    pub end_time: String,
    pub created_at: String,
}

impl From<NewSessionRequest> for NewSession {
    fn from(req: NewSessionRequest) -> Self {
        NewSession {
            client_id: req.client_id,
            name: req.name,
            date: req.date.format("%Y-%m-%d").to_string(),
            start_time: req.start_time.format("%H:%M").to_string(),
            end_time: req.end_time.format("%H:%M").to_string(),
            created_at: chrono::Local::now().format("%Y-%m-%dT%H:%M:%S").to_string(),
        }
    }
}

#[derive(Debug, Deserialize, Validate)]
pub struct UpdateSessionRequest {
    #[validate(range(min = 1, message = "Client ID must be positive"))]
    pub client_id: i32,

    #[validate(length(
        min = 1,
        max = 200,
        message = "Session name must be between 1 and 200 characters"
    ))]
    pub name: String,

    pub date: NaiveDate,
    pub start_time: NaiveTime,
    pub end_time: NaiveTime,
}

#[derive(Debug, AsChangeset)]
#[diesel(table_name = crate::schema::sessions)]
pub struct UpdateSession {
    pub client_id: i32,
    pub name: String,
    pub date: String,
    pub start_time: String,
    pub end_time: String,
}

impl From<UpdateSessionRequest> for UpdateSession {
    fn from(req: UpdateSessionRequest) -> Self {
        UpdateSession {
            client_id: req.client_id,
            name: req.name,
            date: req.date.format("%Y-%m-%d").to_string(),
            start_time: req.start_time.format("%H:%M").to_string(),
            end_time: req.end_time.format("%H:%M").to_string(),
        }
    }
}

impl NewSessionRequest {
    pub fn validate_and_sanitize(&mut self) -> Result<(), validator::ValidationErrors> {
        // Sanitize input
        self.name = self.name.trim().to_string();

        // Validate basic fields
        self.validate()?;

        // Custom validation: end time must be after start time
        if self.end_time <= self.start_time {
            let mut errors = validator::ValidationErrors::new();
            let mut error = validator::ValidationError::new("invalid_time_range");
            error.message = Some("End time must be after start time".into());
            errors.add("end_time", error);
            return Err(errors);
        }

        Ok(())
    }
}

impl UpdateSessionRequest {
    pub fn validate_and_sanitize(&mut self) -> Result<(), validator::ValidationErrors> {
        // Sanitize input
        self.name = self.name.trim().to_string();

        // Validate basic fields
        self.validate()?;

        // Custom validation: end time must be after start time
        if self.end_time <= self.start_time {
            let mut errors = validator::ValidationErrors::new();
            let mut error = validator::ValidationError::new("invalid_time_range");
            error.message = Some("End time must be after start time".into());
            errors.add("end_time", error);
            return Err(errors);
        }

        Ok(())
    }
}

#[derive(Debug, Serialize)]
pub struct SessionWithDuration {
    #[serde(flatten)]
    pub session: Session,
    pub client_name: String,
    pub duration_minutes: i64,
}

#[derive(Debug, Deserialize, Clone)]
pub struct SessionFilterParams {
    pub client_id: Option<i32>,
    pub start_date: Option<NaiveDate>,
    pub end_date: Option<NaiveDate>,
}
