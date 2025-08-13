use crate::common::{helpers::create_test_app, test_db::setup_test_db};
use actix_web::{http::StatusCode, test};

#[cfg(test)]
mod security_headers_middleware_tests {
    use super::*;

    #[actix_rt::test]
    async fn test_security_headers_present() {
        let pool = setup_test_db();
        let app = test::init_service(create_test_app(pool)).await;
        let req = test::TestRequest::get().uri("/health").to_request();
        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::OK);
        for header in [
            "x-content-type-options",
            "x-frame-options",
            "x-xss-protection",
            "content-security-policy",
        ] {
            assert!(
                resp.headers().contains_key(header),
                "Missing header {}",
                header
            );
        }
    }
}
