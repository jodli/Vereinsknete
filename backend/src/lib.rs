pub mod config;
pub mod errors;
pub mod handlers;
pub mod i18n;
pub mod middleware;
pub mod models;
pub mod schema;
pub mod services;
pub mod shutdown;

// Re-export the database pool type for tests and consumers
use diesel::r2d2::{self, ConnectionManager};
use diesel::sqlite::SqliteConnection;

pub type DbPool = r2d2::Pool<ConnectionManager<SqliteConnection>>;
