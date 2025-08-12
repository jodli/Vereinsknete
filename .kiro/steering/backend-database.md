---
inclusion: fileMatch
fileMatchPattern: 'backend/src/services/*'
---

# Backend Database & Services Guidelines

This document provides guidelines for database operations, service layer implementation, and data persistence in the VereinsKnete backend.

## Database Architecture

### Schema Design Patterns
- **Primary Keys**: Always use `INTEGER PRIMARY KEY` for auto-incrementing IDs
- **Timestamps**: Include `created_at` and `updated_at` with automatic triggers
- **Foreign Keys**: Use proper constraints with `ON DELETE CASCADE` where appropriate
- **Unique Constraints**: Implement business logic constraints (e.g., `UNIQUE(year, sequence_number)`)

### Migration Standards
```sql
-- Always include descriptive comments
-- Create tables for the VereinsKnete application

CREATE TABLE table_name (
    id INTEGER PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    optional_field TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add update triggers for timestamp management
CREATE TRIGGER update_table_name_timestamp
AFTER UPDATE ON table_name
BEGIN
    UPDATE table_name SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;
```

### Diesel Configuration
```toml
# diesel.toml
[print_schema]
file = "src/schema.rs"
custom_type_derives = ["diesel::query_builder::QueryId", "Clone"]

[migrations_directory]
dir = "migrations"
```

## Service Layer Architecture

### Service Function Patterns
All service functions should follow this signature pattern with comprehensive documentation and validation:

```rust
/// Retrieves all entities from the database
/// 
/// # Arguments
/// * `pool` - Database connection pool
/// 
/// # Returns
/// * `Result<Vec<Entity>, diesel::result::Error>` - List of all entities or database error
pub fn get_all_entities(pool: &DbPool) -> Result<Vec<Entity>, diesel::result::Error> {
    use crate::schema::table_name::dsl::*;

    let mut conn = pool.get().expect("Failed to get DB connection");
    
    log::debug!("Fetching all entities from database");
    
    let result = table_name.select(Entity::as_select()).load(&mut conn);
    
    match &result {
        Ok(entities_list) => log::debug!("Successfully fetched {} entities", entities_list.len()),
        Err(e) => log::error!("Failed to fetch entities: {}", e),
    }
    
    result
}
```

### Database Connection Pattern
Always use this pattern for database operations with validation and logging:

```rust
pub fn service_function(pool: &DbPool, params: Type) -> Result<ReturnType, diesel::result::Error> {
    use crate::schema::table_name::dsl::*;
    
    // Input validation
    if params.id <= 0 {
        log::warn!("Invalid ID provided: {}", params.id);
        return Err(diesel::result::Error::NotFound);
    }
    
    let mut conn = pool.get().expect("Failed to get DB connection");
    
    log::debug!("Performing database operation with params: {:?}", params);
    
    // Perform database operations
    let result = table_name
        .filter(conditions)
        .select(Entity::as_select())
        .load(&mut conn);
    
    match &result {
        Ok(data) => log::debug!("Successfully retrieved {} records", data.len()),
        Err(e) => log::error!("Database operation failed: {}", e),
    }
    
    result
}
```

### Business Logic Validation in Services
Services must implement comprehensive business logic validation:

```rust
pub fn create_entity(pool: &DbPool, new_entity: NewEntity) -> Result<Entity, diesel::result::Error> {
    // Business logic validation
    if new_entity.name.trim().is_empty() {
        log::warn!("Attempted to create entity with empty name");
        return Err(diesel::result::Error::DatabaseError(
            diesel::result::DatabaseErrorKind::CheckViolation,
            Box::new("Entity name cannot be empty".to_string()),
        ));
    }

    if new_entity.hourly_rate < 0.0 {
        log::warn!("Attempted to create entity with negative rate: {}", new_entity.hourly_rate);
        return Err(diesel::result::Error::DatabaseError(
            diesel::result::DatabaseErrorKind::CheckViolation,
            Box::new("Hourly rate cannot be negative".to_string()),
        ));
    }

    let mut conn = pool.get().expect("Failed to get DB connection");

    log::info!("Creating new entity: {}", new_entity.name);

    // Check for duplicate names
    let existing_count: i64 = entities
        .filter(name.eq(&new_entity.name))
        .select(diesel::dsl::count_star())
        .first(&mut conn)?;

    if existing_count > 0 {
        log::warn!("Attempted to create entity with duplicate name: {}", new_entity.name);
        return Err(diesel::result::Error::DatabaseError(
            diesel::result::DatabaseErrorKind::UniqueViolation,
            Box::new("Entity name already exists".to_string()),
        ));
    }

    // Continue with creation...
}
```

### Error Handling in Services
- Return `diesel::result::Error` from service functions (or `anyhow::Result` for complex operations)
- Implement comprehensive business logic validation
- Use structured logging for all error conditions
- Validate business constraints (uniqueness, foreign keys, data ranges)
- Log all validation failures with context

### SQLite-Specific Patterns
```rust
// SQLite doesn't support RETURNING, so fetch after insert
diesel::insert_into(table::table)
    .values(&new_entity)
    .execute(&mut conn)?;

// Fetch the inserted record
table
    .order(id.desc())
    .limit(1)
    .select(Entity::as_select())
    .get_result(&mut conn)
```

## Database Connection Pooling

### Optimized Pool Configuration
```rust
let pool = r2d2::Pool::builder()
    .max_size(10)                    // Maximum connections
    .min_idle(Some(1))               // Minimum idle connections
    .connection_timeout(Duration::from_secs(30))  // Connection timeout
    .build(manager)
    .expect("Failed to create pool");
```

### Query Optimization
- Use `select()` to specify columns instead of `SELECT *`
- Use `limit()` and `offset()` for pagination
- Create database indexes for frequently queried columns
- Use `optional()` for queries that might return no results

## Data Type Standards
- **IDs**: Always `i32` for primary and foreign keys
- **Strings**: Use `String` for owned data, `&str` for references
- **Optional Fields**: Use `Option<T>` for nullable database columns
- **Dates**: Use `chrono::NaiveDate` for dates, `NaiveTime` for times
- **Money**: Use `f32` for currency amounts (consider `Decimal` for precision-critical apps)
- **Status Fields**: Use string enums with validation

## Testing Database Operations

### Unit Testing Structure
```rust
#[cfg(test)]
mod tests {
    use super::*;
    use diesel::r2d2::{self, ConnectionManager};
    use diesel::sqlite::SqliteConnection;

    fn setup_test_db() -> DbPool {
        let manager = ConnectionManager::<SqliteConnection>::new(":memory:");
        r2d2::Pool::builder().build(manager).expect("Failed to create pool")
    }

    #[test]
    fn test_create_entity() {
        let pool = setup_test_db();
        // Run migrations
        // Test entity creation
    }
}
```

### Test Data Management
```rust
// Create test fixtures
fn create_test_entity() -> NewEntity {
    NewEntity {
        name: "Test Entity".to_string(),
        field: "Test Value".to_string(),
        rate: 50.0,
    }
}
```

This document should be referenced when working on database operations, service layer implementation, or data persistence logic.