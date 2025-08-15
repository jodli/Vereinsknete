use actix_web::{test, web, App};
use backend::{
    config::Config,
    handlers,
    middleware::{RequestIdMiddleware, SecurityHeadersMiddleware},
    DbPool,
};
use std::path::PathBuf;

pub fn create_test_app(
    pool: DbPool,
) -> App<
    impl actix_web::dev::ServiceFactory<
        actix_web::dev::ServiceRequest,
        Config = (),
        Response = actix_web::dev::ServiceResponse,
        Error = actix_web::Error,
        InitError = (),
    >,
> {
    // Create a test config
    let test_config = Config {
        database_url: "test.db".to_string(),
        port: 8080,
        host: "localhost".to_string(),
        static_dir: None,
        invoice_dir: PathBuf::from("test_invoices"),
        log_level: "info".to_string(),
        env_mode: "dev".to_string(),
    };

    App::new()
        .wrap(RequestIdMiddleware)
        .wrap(SecurityHeadersMiddleware)
        .app_data(web::Data::new(pool))
        .app_data(web::Data::new(test_config))
        .configure(handlers::health::config)
        .service(
            web::scope("/api")
                .configure(handlers::user_profile::config)
                .configure(handlers::client::config)
                .configure(handlers::session::config)
                .configure(handlers::invoice::config),
        )
}

pub async fn response_to_string(response: actix_web::dev::ServiceResponse) -> String {
    let body = test::read_body(response).await;
    String::from_utf8(body.to_vec()).expect("Response body should be valid UTF-8")
}
