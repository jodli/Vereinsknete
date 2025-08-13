use actix_cors::Cors;
use actix_files as fs;
use actix_web::{middleware::Logger, web, App, HttpServer};
use diesel::r2d2::{self, ConnectionManager};
use diesel::sqlite::SqliteConnection;
use dotenvy::dotenv;
use std::env;
use std::path::Path;
use std::str::FromStr;
use std::time::Duration;

// Import modules from the library crate instead of redefining them locally.
// Previously we had `mod handlers;` etc. which recompiled the same source files
// into the binary target. That caused unit tests inside those modules to be
// discovered twice: once for the library test harness and once for the binary
// test harness (showing 114 tests twice). By importing from the library crate
// (whose package name is `backend`), we ensure tests only live in one place.
use backend::{handlers, middleware};
use middleware::{RequestIdMiddleware, SecurityHeadersMiddleware};

pub type DbPool = r2d2::Pool<ConnectionManager<SqliteConnection>>;

#[derive(Debug, Clone)]
pub enum AppEnv {
    Dev,
    Prod,
}

impl FromStr for AppEnv {
    type Err = String;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s.to_ascii_lowercase().as_str() {
            "dev" | "development" => Ok(AppEnv::Dev),
            "prod" | "production" => Ok(AppEnv::Prod),
            s => Err(format!("Invalid environment: {s}")),
        }
    }
}

impl AppEnv {
    pub fn from_env() -> Self {
        std::env::var("RUST_ENV")
            .ok()
            .and_then(|v| AppEnv::from_str(&v).ok())
            .unwrap_or(AppEnv::Dev)
    }
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    // Load environment variables from appropriate .env file
    // Check for explicit environment first, then default to development
    let env_mode = AppEnv::from_env();

    match env_mode {
        AppEnv::Prod => {
            if Path::new(".env.production").exists() {
                dotenvy::from_filename(".env.production").ok();
            } else {
                dotenv().ok();
            }
        }
        AppEnv::Dev => {
            if Path::new(".env.development").exists() {
                dotenvy::from_filename(".env.development").ok();
            } else {
                dotenv().ok();
            }
        }
    }

    // Initialize the logger
    env_logger::init_from_env(env_logger::Env::new().default_filter_or("info"));
    log::info!("Running in {:?} mode", env_mode);

    // Set up database connection pool
    let database_url = env::var("DATABASE_URL").unwrap_or_else(|_| "vereinsknete.db".to_string());
    log::info!("Using database URL: {}", database_url);
    let manager = ConnectionManager::<SqliteConnection>::new(database_url);
    let pool = r2d2::Pool::builder()
        .max_size(10)
        .min_idle(Some(1))
        .connection_timeout(Duration::from_secs(30))
        .build(manager)
        .expect("Failed to create pool");

    // Set the bind target (IP address and port) from environment variables
    let host = env::var("HOST").unwrap_or_else(|_| "0.0.0.0".to_string());
    let port = env::var("PORT")
        .unwrap_or_else(|_| "8080".to_string())
        .parse::<u16>()
        .unwrap_or(8080);
    let target = (host.as_str(), port);

    // Set up and start the HTTP server
    log::info!(
        "Starting VereinsKnete server at http://{}:{}",
        target.0,
        target.1
    );

    // Determine static file serving based on environment mode
    let static_files_path = env::var("STATIC_FILES_PATH").unwrap_or_else(|_| match env_mode {
        AppEnv::Dev => "../frontend/build".to_string(),
        AppEnv::Prod => "./public".to_string(),
    });
    let serve_static_files =
        matches!(env_mode, AppEnv::Prod) && Path::new(&static_files_path).exists();

    match env_mode {
        AppEnv::Dev => {
            log::info!("Running in development mode - static files will not be served");
            log::info!("Frontend should be started separately (e.g., with npm start)");
        }
        AppEnv::Prod => {
            if serve_static_files {
                log::info!(
                    "Running in production mode - serving static files from {}",
                    static_files_path
                );
            } else {
                log::warn!(
                    "Production mode but no static files found at {}",
                    static_files_path
                );
            }
        }
    }
    HttpServer::new(move || {
        let cors = match env_mode {
            AppEnv::Dev => Cors::default()
                .allow_any_origin()
                .allow_any_method()
                .allow_any_header()
                .max_age(3600),
            AppEnv::Prod => Cors::default()
                .allowed_origin("https://yourdomain.com") // Configure for production
                .allowed_methods(vec!["GET", "POST", "PUT", "DELETE"])
                .allowed_headers(vec!["Content-Type", "Authorization"])
                .max_age(3600),
        };

        let mut app = App::new()
            .wrap(Logger::default())
            .wrap(RequestIdMiddleware)
            .wrap(SecurityHeadersMiddleware)
            .wrap(cors)
            .app_data(web::Data::new(pool.clone()))
            // Health check endpoints (outside API scope for monitoring)
            .configure(handlers::health::config)
            // Register API routes
            .service(
                web::scope("/api")
                    .configure(handlers::user_profile::config)
                    .configure(handlers::client::config)
                    .configure(handlers::session::config)
                    .configure(handlers::invoice::config),
            );

        // Conditionally serve static files only in production
        if serve_static_files {
            app = app.service(fs::Files::new("/", &static_files_path).index_file("index.html"));
        }

        app
    })
    .bind(target)?
    .run()
    .await
}
