---
inclusion: fileMatch
fileMatchPattern: 'backend/src/models/*'
---

# Backend Models & Data Validation Guidelines

This document provides guidelines for data models, DTOs, validation, and serialization in the VereinsKnete backend.

## Model Structure Pattern

Every entity should have three model variants with comprehensive validation:

### 1. Main Model (Database Representation)
```rust
#[derive(Debug, Serialize, Deserialize, Queryable, Selectable)]
#[diesel(table_name = crate::schema::table_name)]
#[diesel(check_for_backend(diesel::sqlite::Sqlite))]
pub struct EntityName {
    pub id: i32,
    pub name: String,
    pub optional_field: Option<String>,
    // Don't include timestamps in API responses unless needed
}
```

### 2. New Entity (Creation DTO)
```rust
#[derive(Debug, Deserialize, Insertable, Validate)]
#[diesel(table_name = crate::schema::table_name)]
pub struct NewEntityName {
    #[validate(length(min = 1, max = 100, message = "Name must be between 1 and 100 characters"))]
    pub name: String,
    
    #[validate(length(min = 1, max = 200, message = "Field must be between 1 and 200 characters"))]
    pub optional_field: Option<String>,
    
    #[validate(range(min = 0.0, max = 1000.0, message = "Rate must be between 0 and 1000"))]
    pub rate: f32,
    // No id or timestamps - handled by database
}

impl NewEntityName {
    pub fn validate_and_sanitize(&mut self) -> Result<(), validator::ValidationErrors> {
        // Sanitize input
        self.name = self.name.trim().to_string();
        if let Some(ref mut field) = self.optional_field {
            *field = field.trim().to_string();
            if field.is_empty() {
                self.optional_field = None;
            }
        }
        
        // Validate basic rules
        self.validate()?;
        
        // Custom business logic validation
        if self.name.to_lowercase().contains("admin") {
            let mut errors = validator::ValidationErrors::new();
            let mut error = validator::ValidationError::new("reserved_name");
            error.message = Some("Name cannot contain 'admin'".into());
            errors.add("name", error);
            return Err(errors);
        }
        
        Ok(())
    }
}
```

### 3. Update Entity (Partial Update DTO)
```rust
#[derive(Debug, Deserialize, AsChangeset, Validate)]
#[diesel(table_name = crate::schema::table_name)]
pub struct UpdateEntityName {
    #[validate(length(min = 1, max = 100, message = "Name must be between 1 and 100 characters"))]
    pub name: Option<String>,
    
    #[validate(length(min = 1, max = 200, message = "Field must be between 1 and 200 characters"))]
    pub optional_field: Option<String>,
    
    #[validate(range(min = 0.0, max = 1000.0, message = "Rate must be between 0 and 1000"))]
    pub rate: Option<f32>,
    // All fields optional for partial updates
}

impl UpdateEntityName {
    pub fn validate_and_sanitize(&mut self) -> Result<(), validator::ValidationErrors> {
        // Sanitize input
        if let Some(ref mut name) = self.name {
            *name = name.trim().to_string();
        }
        if let Some(ref mut field) = self.optional_field {
            *field = field.trim().to_string();
            if field.is_empty() {
                self.optional_field = None;
            }
        }
        
        // Validate basic rules
        self.validate()?;
        
        // Custom business logic validation
        if let Some(ref name) = self.name {
            if name.to_lowercase().contains("admin") {
                let mut errors = validator::ValidationErrors::new();
                let mut error = validator::ValidationError::new("reserved_name");
                error.message = Some("Name cannot contain 'admin'".into());
                errors.add("name", error);
                return Err(errors);
            }
        }
        
        Ok(())
    }
}
```

## Comprehensive Validation Framework

### Built-in Validation Rules
```rust
use validator::Validate;

#[derive(Debug, Deserialize, Validate)]
pub struct EntityRequest {
    // String length validation
    #[validate(length(min = 1, max = 100, message = "Name must be between 1 and 100 characters"))]
    pub name: String,
    
    // Email validation
    #[validate(email(message = "Invalid email format"))]
    pub email: Option<String>,
    
    // Numeric range validation
    #[validate(range(min = 0.0, max = 1000.0, message = "Rate must be between 0 and 1000"))]
    pub hourly_rate: f32,
    
    // Custom validation function
    #[validate(custom = "validate_phone")]
    pub phone: Option<String>,
    
    // URL validation
    #[validate(url(message = "Invalid URL format"))]
    pub website: Option<String>,
}

// Custom validation function
fn validate_phone(phone: &str) -> Result<(), validator::ValidationError> {
    let phone_regex = regex::Regex::new(r"^\+?[1-9]\d{1,14}$").unwrap();
    if phone_regex.is_match(phone) {
        Ok(())
    } else {
        Err(validator::ValidationError::new("invalid_phone"))
    }
}
```

### Complex Custom Validation
```rust
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
        
        // Custom validation: session duration must be reasonable (max 24 hours)
        let duration = self.end_time - self.start_time;
        if duration.num_hours() > 24 {
            let mut errors = validator::ValidationErrors::new();
            let mut error = validator::ValidationError::new("duration_too_long");
            error.message = Some("Session duration cannot exceed 24 hours".into());
            errors.add("end_time", error);
            return Err(errors);
        }
        
        Ok(())
    }
}
```

## Request/Response DTOs

### Specialized DTOs for Complex Operations
```rust
// Request DTOs with validation
#[derive(Debug, Deserialize, Validate)]
pub struct InvoiceRequest {
    #[validate(range(min = 1, message = "Client ID must be positive"))]
    pub client_id: i32,
    
    pub start_date: NaiveDate,
    pub end_date: NaiveDate,
    
    #[validate(length(min = 2, max = 5, message = "Language must be 2-5 characters"))]
    pub language: Option<String>,
}

impl InvoiceRequest {
    pub fn validate_and_sanitize(&mut self) -> Result<(), validator::ValidationErrors> {
        // Sanitize language input
        if let Some(ref mut lang) = self.language {
            *lang = lang.trim().to_lowercase();
            if lang.is_empty() {
                self.language = None;
            }
        }
        
        // Validate basic fields
        self.validate()?;
        
        // Custom validation: end date must be after start date
        if self.end_date <= self.start_date {
            let mut errors = validator::ValidationErrors::new();
            let mut error = validator::ValidationError::new("invalid_date_range");
            error.message = Some("End date must be after start date".into());
            errors.add("end_date", error);
            return Err(errors);
        }
        
        Ok(())
    }
}

// Response DTOs with computed fields
#[derive(Debug, Serialize)]
pub struct EntityWithMetadata {
    #[serde(flatten)]
    pub entity: Entity,
    pub computed_field: String,
    pub related_count: i64,
    pub status: String,
}
```

### Filter and Query DTOs
```rust
#[derive(Debug, Deserialize, Clone, Validate)]
pub struct EntityFilterParams {
    #[validate(range(min = 1, message = "Client ID must be positive"))]
    pub client_id: Option<i32>,
    
    pub start_date: Option<NaiveDate>,
    pub end_date: Option<NaiveDate>,
    
    #[validate(length(min = 1, max = 20, message = "Status must be between 1 and 20 characters"))]
    pub status: Option<String>,
    
    #[validate(range(min = 1, max = 100, message = "Limit must be between 1 and 100"))]
    #[serde(default)]
    pub limit: Option<i64>,
    
    #[validate(range(min = 0, message = "Offset must be non-negative"))]
    #[serde(default)]
    pub offset: Option<i64>,
}
```

## Data Type Standards

### Core Data Types
- **IDs**: Always `i32` for primary and foreign keys
- **Strings**: Use `String` for owned data, `&str` for references
- **Optional Fields**: Use `Option<T>` for nullable database columns
- **Dates**: Use `chrono::NaiveDate` for dates, `NaiveTime` for times
- **Money**: Use `f32` for currency amounts (consider `Decimal` for precision-critical apps)
- **Status Fields**: Use string enums with validation

### Serialization Patterns
```rust
// Custom serialization for dates
#[derive(Debug, Serialize, Deserialize)]
pub struct EntityWithDate {
    pub id: i32,
    pub name: String,
    
    #[serde(with = "date_format")]
    pub created_date: NaiveDate,
}

mod date_format {
    use chrono::NaiveDate;
    use serde::{self, Deserialize, Deserializer, Serializer};

    const FORMAT: &'static str = "%Y-%m-%d";

    pub fn serialize<S>(date: &NaiveDate, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: Serializer,
    {
        let s = format!("{}", date.format(FORMAT));
        serializer.serialize_str(&s)
    }

    pub fn deserialize<'de, D>(deserializer: D) -> Result<NaiveDate, D::Error>
    where
        D: Deserializer<'de>,
    {
        let s = String::deserialize(deserializer)?;
        NaiveDate::parse_from_str(&s, FORMAT).map_err(serde::de::Error::custom)
    }
}
```

## Input Sanitization Patterns

### String Sanitization
```rust
impl NewEntity {
    pub fn sanitize_strings(&mut self) {
        // Trim whitespace
        self.name = self.name.trim().to_string();
        
        // Handle optional strings
        if let Some(ref mut field) = self.optional_field {
            *field = field.trim().to_string();
            if field.is_empty() {
                self.optional_field = None;
            }
        }
        
        // Normalize email
        if let Some(ref mut email) = self.email {
            *email = email.trim().to_lowercase();
            if email.is_empty() {
                self.email = None;
            }
        }
        
        // Clean phone numbers (remove non-digits except +)
        if let Some(ref mut phone) = self.phone {
            *phone = phone.chars()
                .filter(|c| c.is_ascii_digit() || *c == '+')
                .collect();
            if phone.is_empty() {
                self.phone = None;
            }
        }
    }
}
```

## Status and Enum Validation

### Status Field Validation
```rust
#[derive(Debug, Deserialize, Validate)]
pub struct UpdateStatusRequest {
    #[validate(length(min = 1, max = 20, message = "Status must be between 1 and 20 characters"))]
    pub status: String,
    
    pub updated_date: Option<String>,
}

impl UpdateStatusRequest {
    pub fn validate_and_sanitize(&mut self) -> Result<(), validator::ValidationErrors> {
        // Sanitize status
        self.status = self.status.trim().to_lowercase();
        
        // Validate basic rules
        self.validate()?;
        
        // Custom validation: valid status values
        let valid_statuses = ["created", "sent", "paid", "overdue", "cancelled"];
        if !valid_statuses.contains(&self.status.as_str()) {
            let mut errors = validator::ValidationErrors::new();
            let mut error = validator::ValidationError::new("invalid_status");
            error.message = Some("Status must be one of: created, sent, paid, overdue, cancelled".into());
            errors.add("status", error);
            return Err(errors);
        }
        
        Ok(())
    }
}
```

## Error Handling in Models

### Validation Error Patterns
```rust
// In handlers, convert validation errors to AppError
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
```

## Testing Models

### Model Testing Patterns
```rust
#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_new_entity_validation() {
        let mut entity = NewEntity {
            name: "  Test Entity  ".to_string(),
            email: Some("  TEST@EXAMPLE.COM  ".to_string()),
            rate: 50.0,
        };

        // Should pass validation after sanitization
        assert!(entity.validate_and_sanitize().is_ok());
        
        // Check sanitization worked
        assert_eq!(entity.name, "Test Entity");
        assert_eq!(entity.email, Some("test@example.com".to_string()));
    }

    #[test]
    fn test_validation_errors() {
        let mut entity = NewEntity {
            name: "".to_string(),  // Invalid: empty name
            email: Some("invalid-email".to_string()),  // Invalid: bad email format
            rate: -10.0,  // Invalid: negative rate
        };

        let result = entity.validate_and_sanitize();
        assert!(result.is_err());
        
        let errors = result.unwrap_err();
        assert!(errors.field_errors().contains_key("name"));
        assert!(errors.field_errors().contains_key("email"));
        assert!(errors.field_errors().contains_key("rate"));
    }
}
```

This document should be referenced when working on data models, DTOs, validation logic, or serialization.