use diesel::r2d2::{self, ConnectionManager};
use diesel::sqlite::SqliteConnection;
use diesel_migrations::{embed_migrations, EmbeddedMigrations, MigrationHarness};
use std::sync::Mutex;
use tempfile::NamedTempFile;

pub type DbPool = r2d2::Pool<ConnectionManager<SqliteConnection>>;

// Embed migrations at compile time
pub const MIGRATIONS: EmbeddedMigrations = embed_migrations!("migrations");

// Global counter for unique database names
static DB_COUNTER: Mutex<u32> = Mutex::new(0);

/// Creates a test database pool with an in-memory SQLite database
pub fn setup_test_db() -> DbPool {
    let mut counter = DB_COUNTER.lock().unwrap();
    *counter += 1;
    // Use a unique shared in-memory database URI per test so each pool gets a fresh schema
    let db_name = format!("file:vk_test_{}?mode=memory&cache=shared", *counter);
    drop(counter);

    let manager = ConnectionManager::<SqliteConnection>::new(db_name);
    let pool = r2d2::Pool::builder()
        .max_size(1) // Single connection for tests to avoid concurrency issues
        .build(manager)
        .expect("Failed to create test database pool");

    // Run migrations
    run_migrations(&pool).expect("Failed to run migrations");

    pool
}

/// Creates a test database pool with a temporary file (for tests that need persistence)
pub fn setup_test_db_file() -> (DbPool, NamedTempFile) {
    let temp_file = NamedTempFile::new().expect("Failed to create temporary database file");
    let db_path = temp_file.path().to_str().unwrap();

    let manager = ConnectionManager::<SqliteConnection>::new(db_path);
    let pool = r2d2::Pool::builder()
        .max_size(1)
        .build(manager)
        .expect("Failed to create test database pool");

    // Run migrations
    run_migrations(&pool).expect("Failed to run migrations");

    (pool, temp_file)
}

/// Runs database migrations on the test database
pub fn run_migrations(pool: &DbPool) -> Result<(), Box<dyn std::error::Error + Send + Sync>> {
    let mut conn = pool.get()?;
    conn.run_pending_migrations(MIGRATIONS)?;
    Ok(())
}

/// Cleans up the test database by dropping all tables
pub fn cleanup_test_db(pool: &DbPool) -> Result<(), diesel::result::Error> {
    use diesel::prelude::*;

    let mut conn = pool.get().map_err(|_| diesel::result::Error::NotFound)?;

    // Drop all tables in reverse dependency order
    diesel::sql_query("DROP TABLE IF EXISTS invoices").execute(&mut conn)?;
    diesel::sql_query("DROP TABLE IF EXISTS sessions").execute(&mut conn)?;
    diesel::sql_query("DROP TABLE IF EXISTS clients").execute(&mut conn)?;
    diesel::sql_query("DROP TABLE IF EXISTS user_profiles").execute(&mut conn)?;
    diesel::sql_query("DROP TABLE IF EXISTS __diesel_schema_migrations").execute(&mut conn)?;

    Ok(())
}

/// Truncates all tables while preserving schema
pub fn truncate_all_tables(pool: &DbPool) -> Result<(), diesel::result::Error> {
    use diesel::prelude::*;

    let mut conn = pool.get().map_err(|_| diesel::result::Error::NotFound)?;
    conn.immediate_transaction(|conn| {
        // Helper closure to ignore errors (e.g., table missing)
        let ignore = |res: Result<usize, diesel::result::Error>| {
            if let Err(e) = &res {
                if !matches!(e, diesel::result::Error::DatabaseError(_, _)) {
                    // For non-database errors propagate (connection etc.)
                    return res;
                }
            }
            Ok(0)
        };

        ignore(diesel::sql_query("DELETE FROM invoices").execute(conn))?;
        ignore(diesel::sql_query("DELETE FROM sessions").execute(conn))?;
        ignore(diesel::sql_query("DELETE FROM clients").execute(conn))?;
        ignore(diesel::sql_query("DELETE FROM user_profiles").execute(conn))?;

        // Reset SQLite sequence counters only if table exists
        // Check existence of sqlite_sequence without defining a struct to avoid dead_code warnings
        #[derive(diesel::QueryableByName)]
        struct CountRow {
            #[diesel(sql_type = diesel::sql_types::BigInt)]
            _count: i64,
        }
        let seq_exists: bool = diesel::sql_query(
            "SELECT COUNT(*) as count FROM sqlite_master WHERE type='table' AND name='sqlite_sequence'"
        )
        .get_result::<CountRow>(conn)
        .map(|r| r._count > 0)
        .unwrap_or(false);

        if seq_exists {
            ignore(diesel::sql_query("DELETE FROM sqlite_sequence").execute(conn))?;
        }

        Ok(())
    })
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_setup_test_db() {
        let pool = setup_test_db();
        assert!(pool.get().is_ok());
    }

    #[test]
    fn test_setup_test_db_file() {
        let (pool, _temp_file) = setup_test_db_file();
        assert!(pool.get().is_ok());
    }

    #[test]
    fn test_cleanup_test_db() {
        let pool = setup_test_db();
        assert!(cleanup_test_db(&pool).is_ok());
    }

    #[test]
    fn test_truncate_all_tables() {
        let pool = setup_test_db();
        assert!(truncate_all_tables(&pool).is_ok());
    }
}
