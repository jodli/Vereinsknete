use clap::Parser;
use std::path::PathBuf;

#[derive(Parser, Debug, Clone)]
#[command(name = "vereinsknete")]
#[command(about = "VereinsKnete - Freelance time tracking and invoicing application")]
#[command(version)]
pub struct Config {
    /// Database URL (SQLite file path)
    #[arg(long, env = "DATABASE_URL", default_value = "vereinsknete.db")]
    pub database_url: String,

    /// Port to bind the server to
    #[arg(short, long, env = "PORT", default_value = "8080")]
    pub port: u16,

    /// Host address to bind the server to
    #[arg(long, env = "HOST", default_value = "0.0.0.0")]
    pub host: String,

    /// Directory containing static files to serve
    #[arg(long, env = "STATIC_DIR")]
    pub static_dir: Option<PathBuf>,

    /// Directory to store generated invoices
    #[arg(long, env = "INVOICE_DIR", default_value = "invoices")]
    pub invoice_dir: PathBuf,

    /// Log level (error, warn, info, debug, trace)
    #[arg(long, env = "RUST_LOG", default_value = "info")]
    pub log_level: String,

    /// Environment mode (dev, prod)
    #[arg(long, env = "RUST_ENV", default_value = "dev")]
    pub env_mode: String,
}

impl Config {
    pub fn from_args() -> Self {
        Self::parse()
    }

    pub fn is_production(&self) -> bool {
        matches!(self.env_mode.to_lowercase().as_str(), "prod" | "production")
    }

    pub fn should_serve_static_files(&self) -> bool {
        self.static_dir.is_some() && self.static_dir.as_ref().unwrap().exists()
    }

    pub fn get_static_dir(&self) -> Option<&PathBuf> {
        self.static_dir.as_ref()
    }

    pub fn get_bind_address(&self) -> (String, u16) {
        (self.host.clone(), self.port)
    }
}