use tokio::signal;

/// Wait for a shutdown signal (SIGINT or SIGTERM on Unix, Ctrl+C on Windows)
pub async fn wait_for_shutdown_signal() {
    let ctrl_c = async {
        signal::ctrl_c()
            .await
            .expect("failed to install Ctrl+C handler");
    };

    #[cfg(unix)]
    let terminate = async {
        signal::unix::signal(signal::unix::SignalKind::terminate())
            .expect("failed to install signal handler")
            .recv()
            .await;
    };

    #[cfg(not(unix))]
    let terminate = std::future::pending::<()>();

    tokio::select! {
        _ = ctrl_c => {
            log::info!("Received Ctrl+C signal, initiating graceful shutdown");
        },
        _ = terminate => {
            log::info!("Received SIGTERM signal, initiating graceful shutdown");
        },
    }
}
