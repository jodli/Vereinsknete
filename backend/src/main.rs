use actix_cors::Cors;
use actix_web::{middleware::Logger, web, App, HttpServer};
use diesel::r2d2::{self, ConnectionManager};
use diesel::sqlite::SqliteConnection;
use dotenvy::dotenv;
use std::env;

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
    HttpServer::new(move || {
        // Configure CORS to allow frontend to access API
        let cors = Cors::default()
            .allow_any_origin()
            .allow_any_method()
            .allow_any_header()
            .max_age(3600);

        App::new()
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
            )
    })
    .bind(("127.0.0.1", 8080))?
    .run()
    .await
}
