use actix_cors::Cors;
use actix_files as fs;
use actix_web::{middleware::Logger, web, App, HttpServer};
use diesel::r2d2::{self, ConnectionManager};
use diesel::sqlite::SqliteConnection;
use diesel_migrations::{embed_migrations, EmbeddedMigrations, MigrationHarness};
use std::fs as std_fs;
use std::time::Duration;

// Import modules from the library crate
use backend::{config::Config, handlers, middleware, shutdown};
use middleware::{RequestIdMiddleware, SecurityHeadersMiddleware};

pub type DbPool = r2d2::Pool<ConnectionManager<SqliteConnection>>;

// Embed migrations at compile time
const MIGRATIONS: EmbeddedMigrations = embed_migrations!("migrations");

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    // Parse command-line arguments and configuration
    let config = Config::from_args();

    // Initialize the logger with the configured log level
    env_logger::init_from_env(env_logger::Env::new().default_filter_or(&config.log_level));

    log::info!("Starting VereinsKnete v{}", env!("CARGO_PKG_VERSION"));
    log::info!("Configuration: {:?}", config);

    // Create invoice directory if it doesn't exist
    if let Err(e) = std_fs::create_dir_all(&config.invoice_dir) {
        log::error!(
            "Failed to create invoice directory {:?}: {}",
            config.invoice_dir,
            e
        );
        return Err(std::io::Error::other(e));
    }
    log::info!("Invoice directory: {:?}", config.invoice_dir);

    // Set up database connection pool
    log::info!("Using database: {}", config.database_url);

    // Create database directory if it doesn't exist (for SQLite files)
    if let Some(parent) = std::path::Path::new(&config.database_url).parent() {
        if let Err(e) = std_fs::create_dir_all(parent) {
            log::error!("Failed to create database directory {:?}: {}", parent, e);
            return Err(std::io::Error::other(e));
        }
    }

    let manager = ConnectionManager::<SqliteConnection>::new(&config.database_url);
    let pool = r2d2::Pool::builder()
        .max_size(10)
        .min_idle(Some(1))
        .connection_timeout(Duration::from_secs(30))
        .build(manager)
        .map_err(|e| {
            log::error!("Failed to create database connection pool: {}", e);
            std::io::Error::other(e)
        })?;

    // Run database migrations
    log::info!("Running database migrations...");
    {
        let mut conn = pool.get().map_err(|e| {
            log::error!("Failed to get database connection for migrations: {}", e);
            std::io::Error::other(e)
        })?;

        conn.run_pending_migrations(MIGRATIONS).map_err(|e| {
            log::error!("Failed to run database migrations: {}", e);
            std::io::Error::other(e)
        })?;
        log::info!("Database migrations completed successfully");
    }

    let (host, port) = config.get_bind_address();
    log::info!("Starting server at http://{}:{}", host, port);

    // Log static file serving configuration
    if let Some(static_dir) = config.get_static_dir() {
        if config.should_serve_static_files() {
            log::info!("Serving static files from: {:?}", static_dir);
        } else {
            log::warn!("Static directory specified but not found: {:?}", static_dir);
        }
    } else {
        log::info!("No static directory specified - API only mode");
    }

    // Clone config for use in the server closure
    let config_clone = config.clone();

    // Create the HTTP server
    let server = HttpServer::new(move || {
        // Configure CORS for add-on compatibility
        // In Home Assistant add-on context, we need to allow ingress proxy requests
        let cors = if config_clone.is_production() {
            // Production mode: more restrictive CORS for security
            Cors::default()
                .allow_any_origin() // Home Assistant ingress proxy needs flexible origins
                .allowed_methods(vec!["GET", "POST", "PUT", "DELETE", "OPTIONS"])
                .allowed_headers(vec!["Content-Type", "Authorization", "X-Requested-With"])
                .max_age(3600)
        } else {
            // Development mode: permissive CORS
            Cors::default()
                .allow_any_origin()
                .allow_any_method()
                .allow_any_header()
                .max_age(3600)
        };

        let mut app = App::new()
            .wrap(Logger::default())
            .wrap(RequestIdMiddleware)
            .wrap(SecurityHeadersMiddleware)
            .wrap(cors)
            .app_data(web::Data::new(pool.clone()))
            .app_data(web::Data::new(config_clone.clone()))
            // Health check endpoints (outside API scope for monitoring)
            .configure(handlers::health::config)
            // Register API routes with proper ingress compatibility
            .service(
                web::scope("/api")
                    .configure(handlers::user_profile::config)
                    .configure(handlers::client::config)
                    .configure(handlers::session::config)
                    .configure(handlers::invoice::config),
            );

        // Conditionally serve static files if configured
        if let Some(static_dir) = config_clone.get_static_dir() {
            if config_clone.should_serve_static_files() {
                app = app.service(
                    fs::Files::new("/", static_dir)
                        .index_file("index.html")
                        .prefer_utf8(true),
                );
            }
        }

        app
    })
    .bind((host.as_str(), port))?;

    // Set up graceful shutdown with signal handling
    let server_handle = server.run();

    tokio::select! {
        result = server_handle => {
            match result {
                Ok(()) => {
                    log::info!("Server completed successfully");
                    Ok(())
                },
                Err(e) => {
                    log::error!("Server error: {}", e);
                    Err(e)
                }
            }
        },
        _ = shutdown::wait_for_shutdown_signal() => {
            log::info!("Shutdown signal received, stopping server");
            Ok(())
        }
    }
}
