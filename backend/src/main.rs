use actix_cors::Cors;
use actix_files as fs;
use actix_web::{middleware::Logger, web, App, HttpServer};
use diesel::r2d2::{self, ConnectionManager};
use diesel::sqlite::SqliteConnection;
use dotenvy::dotenv;
use std::env;
use std::path::Path;

mod errors;
mod handlers;
mod i18n;
mod models;
mod schema;
mod services;

pub type DbPool = r2d2::Pool<ConnectionManager<SqliteConnection>>;

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    // Load environment variables from .env file if present
    dotenv().ok();

    // Initialize the logger
    env_logger::init_from_env(env_logger::Env::new().default_filter_or("info"));

    // Set up database connection pool
    let database_url = env::var("DATABASE_URL").unwrap_or_else(|_| "vereinsknete.db".to_string());
    let manager = ConnectionManager::<SqliteConnection>::new(database_url);
    let pool = r2d2::Pool::builder()
        .build(manager)
        .expect("Failed to create pool");

    // Set up and start the HTTP server
    log::info!("Starting VereinsKnete server at http://localhost:8080");

    // Check if we're in development mode
    let is_dev_mode = env::var("ENVIRONMENT").unwrap_or_else(|_| "development".to_string()) == "development";
    let static_files_path = "../frontend/build";
    let serve_static_files = !is_dev_mode && Path::new(static_files_path).exists();

    if is_dev_mode {
        log::info!("Running in development mode - static files will not be served");
        log::info!("Frontend should be started separately (e.g., with npm start)");
    } else if serve_static_files {
        log::info!("Running in production mode - serving static files from {}", static_files_path);
    } else {
        log::warn!("Production mode but no static files found at {}", static_files_path);
    }
    HttpServer::new(move || {
        // Configure CORS to allow frontend to access API
        let cors = Cors::default()
            .allow_any_origin()
            .allow_any_method()
            .allow_any_header()
            .max_age(3600);

        let mut app = App::new()
            .wrap(cors)
            .wrap(Logger::default())
            .app_data(web::Data::new(pool.clone()))
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
            app = app.service(fs::Files::new("/", static_files_path).index_file("index.html"));
        }

        app
    })
    .bind(("0.0.0.0", 8080))?
    .run()
    .await
}
