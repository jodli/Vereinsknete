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

#[derive(Debug, Serialize, Deserialize, Validate)]
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

#[derive(Debug, Serialize, Deserialize, Validate)]
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

#[cfg(test)]
mod tests {
    use super::*;
    use chrono::{NaiveDate, NaiveTime};
    use validator::Validate;

    // Test fixtures
    fn create_valid_session_request() -> NewSessionRequest {
        NewSessionRequest {
            client_id: 1,
            name: "Test Session".to_string(),
            date: NaiveDate::from_ymd_opt(2024, 1, 15).unwrap(),
            start_time: NaiveTime::from_hms_opt(9, 0, 0).unwrap(),
            end_time: NaiveTime::from_hms_opt(17, 0, 0).unwrap(),
        }
    }

    fn create_short_session_request() -> NewSessionRequest {
        NewSessionRequest {
            client_id: 2,
            name: "Short Meeting".to_string(),
            date: NaiveDate::from_ymd_opt(2024, 1, 16).unwrap(),
            start_time: NaiveTime::from_hms_opt(14, 0, 0).unwrap(),
            end_time: NaiveTime::from_hms_opt(15, 30, 0).unwrap(),
        }
    }

    fn create_update_session_request() -> UpdateSessionRequest {
        UpdateSessionRequest {
            client_id: 1,
            name: "Updated Session".to_string(),
            date: NaiveDate::from_ymd_opt(2024, 1, 17).unwrap(),
            start_time: NaiveTime::from_hms_opt(10, 0, 0).unwrap(),
            end_time: NaiveTime::from_hms_opt(18, 0, 0).unwrap(),
        }
    }

    // NewSessionRequest validation tests
    #[test]
    fn test_new_session_request_valid() {
        let session = create_valid_session_request();
        assert!(session.validate().is_ok());
    }

    #[test]
    fn test_new_session_request_short_valid() {
        let session = create_short_session_request();
        assert!(session.validate().is_ok());
    }

    #[test]
    fn test_new_session_request_zero_client_id() {
        let mut session = create_valid_session_request();
        session.client_id = 0;

        let result = session.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("client_id"));
    }

    #[test]
    fn test_new_session_request_negative_client_id() {
        let mut session = create_valid_session_request();
        session.client_id = -1;

        let result = session.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("client_id"));
    }

    #[test]
    fn test_new_session_request_empty_name() {
        let mut session = create_valid_session_request();
        session.name = "".to_string();

        let result = session.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("name"));
    }

    #[test]
    fn test_new_session_request_name_too_long() {
        let mut session = create_valid_session_request();
        session.name = "a".repeat(201); // Exceeds 200 character limit

        let result = session.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("name"));
    }

    #[test]
    fn test_new_session_request_end_before_start() {
        let mut session = create_valid_session_request();
        session.start_time = NaiveTime::from_hms_opt(17, 0, 0).unwrap();
        session.end_time = NaiveTime::from_hms_opt(9, 0, 0).unwrap();

        let result = session.validate_and_sanitize();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("end_time"));
    }

    #[test]
    fn test_new_session_request_same_start_end_time() {
        let mut session = create_valid_session_request();
        let same_time = NaiveTime::from_hms_opt(12, 0, 0).unwrap();
        session.start_time = same_time;
        session.end_time = same_time;

        let result = session.validate_and_sanitize();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("end_time"));
    }

    // Sanitization tests
    #[test]
    fn test_new_session_request_sanitization() {
        let mut session = NewSessionRequest {
            client_id: 1,
            name: "  Test Session  ".to_string(),
            date: NaiveDate::from_ymd_opt(2024, 1, 15).unwrap(),
            start_time: NaiveTime::from_hms_opt(9, 0, 0).unwrap(),
            end_time: NaiveTime::from_hms_opt(17, 0, 0).unwrap(),
        };

        assert!(session.validate_and_sanitize().is_ok());

        // Check sanitization worked
        assert_eq!(session.name, "Test Session");
    }

    // UpdateSessionRequest tests
    #[test]
    fn test_update_session_request_valid() {
        let session = create_update_session_request();
        assert!(session.validate().is_ok());
    }

    #[test]
    fn test_update_session_request_invalid_client_id() {
        let mut session = create_update_session_request();
        session.client_id = 0;

        let result = session.validate();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("client_id"));
    }

    #[test]
    fn test_update_session_request_invalid_time_range() {
        let mut session = create_update_session_request();
        session.start_time = NaiveTime::from_hms_opt(18, 0, 0).unwrap();
        session.end_time = NaiveTime::from_hms_opt(10, 0, 0).unwrap();

        let result = session.validate_and_sanitize();
        assert!(result.is_err());

        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("end_time"));
    }

    #[test]
    fn test_update_session_request_sanitization() {
        let mut session = UpdateSessionRequest {
            client_id: 1,
            name: "  Updated Session  ".to_string(),
            date: NaiveDate::from_ymd_opt(2024, 1, 17).unwrap(),
            start_time: NaiveTime::from_hms_opt(10, 0, 0).unwrap(),
            end_time: NaiveTime::from_hms_opt(18, 0, 0).unwrap(),
        };

        assert!(session.validate_and_sanitize().is_ok());

        // Check sanitization worked
        assert_eq!(session.name, "Updated Session");
    }

    // Conversion tests
    #[test]
    fn test_new_session_from_request() {
        let request = create_valid_session_request();
        let new_session = NewSession::from(request);

        assert_eq!(new_session.client_id, 1);
        assert_eq!(new_session.name, "Test Session");
        assert_eq!(new_session.date, "2024-01-15");
        assert_eq!(new_session.start_time, "09:00");
        assert_eq!(new_session.end_time, "17:00");
        assert!(!new_session.created_at.is_empty());
    }

    #[test]
    fn test_update_session_from_request() {
        let request = create_update_session_request();
        let update_session = UpdateSession::from(request);

        assert_eq!(update_session.client_id, 1);
        assert_eq!(update_session.name, "Updated Session");
        assert_eq!(update_session.date, "2024-01-17");
        assert_eq!(update_session.start_time, "10:00");
        assert_eq!(update_session.end_time, "18:00");
    }

    // Time formatting tests
    #[test]
    fn test_time_formatting_edge_cases() {
        let request = NewSessionRequest {
            client_id: 1,
            name: "Edge Case Session".to_string(),
            date: NaiveDate::from_ymd_opt(2024, 12, 31).unwrap(),
            start_time: NaiveTime::from_hms_opt(0, 0, 0).unwrap(), // Midnight
            end_time: NaiveTime::from_hms_opt(23, 59, 59).unwrap(), // Almost midnight
        };

        let new_session = NewSession::from(request);

        assert_eq!(new_session.date, "2024-12-31");
        assert_eq!(new_session.start_time, "00:00");
        assert_eq!(new_session.end_time, "23:59");
    }

    #[test]
    fn test_time_formatting_with_seconds() {
        let request = NewSessionRequest {
            client_id: 1,
            name: "Precise Session".to_string(),
            date: NaiveDate::from_ymd_opt(2024, 6, 15).unwrap(),
            start_time: NaiveTime::from_hms_opt(9, 30, 45).unwrap(),
            end_time: NaiveTime::from_hms_opt(17, 45, 30).unwrap(),
        };

        let new_session = NewSession::from(request);

        // Seconds should be ignored in formatting
        assert_eq!(new_session.start_time, "09:30");
        assert_eq!(new_session.end_time, "17:45");
    }

    // Boundary value tests
    #[test]
    fn test_session_boundary_values() {
        // Test minimum valid values
        let mut session = NewSessionRequest {
            client_id: 1,          // Minimum positive value
            name: "A".to_string(), // Minimum 1 character
            date: NaiveDate::from_ymd_opt(2024, 1, 1).unwrap(),
            start_time: NaiveTime::from_hms_opt(0, 0, 0).unwrap(),
            end_time: NaiveTime::from_hms_opt(0, 0, 1).unwrap(), // 1 second later
        };
        assert!(session.validate_and_sanitize().is_ok());

        // Test maximum valid values
        session.name = "A".repeat(200); // Maximum 200 characters
        session.client_id = i32::MAX; // Maximum i32 value
        assert!(session.validate_and_sanitize().is_ok());
    }

    // Date edge cases
    #[test]
    fn test_session_date_edge_cases() {
        let session = NewSessionRequest {
            client_id: 1,
            name: "Leap Year Session".to_string(),
            date: NaiveDate::from_ymd_opt(2024, 2, 29).unwrap(), // Leap year
            start_time: NaiveTime::from_hms_opt(9, 0, 0).unwrap(),
            end_time: NaiveTime::from_hms_opt(17, 0, 0).unwrap(),
        };

        assert!(session.validate().is_ok());

        let new_session = NewSession::from(session);
        assert_eq!(new_session.date, "2024-02-29");
    }

    // SessionFilterParams tests
    #[test]
    fn test_session_filter_params_empty() {
        let filter = SessionFilterParams {
            client_id: None,
            start_date: None,
            end_date: None,
        };

        // Should be valid (no filters applied)
        assert!(filter.client_id.is_none());
        assert!(filter.start_date.is_none());
        assert!(filter.end_date.is_none());
    }

    #[test]
    fn test_session_filter_params_with_values() {
        let filter = SessionFilterParams {
            client_id: Some(1),
            start_date: Some(NaiveDate::from_ymd_opt(2024, 1, 1).unwrap()),
            end_date: Some(NaiveDate::from_ymd_opt(2024, 1, 31).unwrap()),
        };

        assert_eq!(filter.client_id, Some(1));
        assert_eq!(
            filter.start_date,
            Some(NaiveDate::from_ymd_opt(2024, 1, 1).unwrap())
        );
        assert_eq!(
            filter.end_date,
            Some(NaiveDate::from_ymd_opt(2024, 1, 31).unwrap())
        );
    }

    // Special character tests
    #[test]
    fn test_session_with_special_characters() {
        let session = NewSessionRequest {
            client_id: 1,
            name: "Müller & Co. - Beratung (Projekt #123)".to_string(),
            date: NaiveDate::from_ymd_opt(2024, 1, 15).unwrap(),
            start_time: NaiveTime::from_hms_opt(9, 0, 0).unwrap(),
            end_time: NaiveTime::from_hms_opt(17, 0, 0).unwrap(),
        };

        assert!(session.validate().is_ok());
    }

    // Duration calculation helper test (for SessionWithDuration)
    #[test]
    fn test_session_duration_calculation() {
        let start = NaiveTime::from_hms_opt(9, 0, 0).unwrap();
        let end = NaiveTime::from_hms_opt(17, 30, 0).unwrap();

        let duration = end - start;
        let duration_minutes = duration.num_minutes();

        assert_eq!(duration_minutes, 510); // 8.5 hours = 510 minutes
    }

    #[test]
    fn test_session_overnight_duration() {
        // Test case where session goes past midnight (edge case)
        let start = NaiveTime::from_hms_opt(23, 0, 0).unwrap();
        let end = NaiveTime::from_hms_opt(1, 0, 0).unwrap();

        // This would be invalid in our validation, but test the time calculation
        let duration = end - start;

        // This will be negative, which is why we validate end > start
        assert!(duration.num_minutes() < 0);
    }
}
