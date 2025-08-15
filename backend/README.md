# VereinsKnete Backend

Rust backend for VereinsKnete built with Actix-web, Diesel ORM, and SQLite.

## 🚀 Quick Start

```bash
cd backend
cargo run
```

The application will automatically:
- Create the SQLite database if it doesn't exist
- Run all pending database migrations
- Start the web server on port 8080

## 🗄️ Database Management

### Automatic Migrations

The backend includes automatic database migration handling:

- **Embedded Migrations**: All migration files are embedded in the binary at compile time
- **Automatic Execution**: Migrations run automatically on application startup
- **Zero Configuration**: No manual database setup required
- **Production Ready**: Reliable migration handling for deployments

### Implementation Details

```rust
// Migrations are embedded at compile time
const MIGRATIONS: EmbeddedMigrations = embed_migrations!("migrations");

// Automatically run on startup
conn.run_pending_migrations(MIGRATIONS)
    .expect("Failed to run database migrations");
```

### Creating New Migrations

```bash
# Install Diesel CLI (one-time setup)
cargo install diesel_cli --no-default-features --features sqlite

# Create a new migration
diesel migration generate migration_name

# Edit the generated files:
# - migrations/YYYY-MM-DD-HHMMSS_migration_name/up.sql
# - migrations/YYYY-MM-DD-HHMMSS_migration_name/down.sql

# Migrations will be applied automatically on next startup
cargo run
```

## 🏗️ Architecture

### Project Structure

```
backend/
├── src/
│   ├── handlers/          # HTTP request handlers
│   │   ├── client.rs      # Client management endpoints
│   │   ├── session.rs     # Session tracking endpoints
│   │   ├── invoice.rs     # Invoice generation endpoints
│   │   ├── user_profile.rs # User profile endpoints
│   │   └── health.rs      # Health check endpoints
│   ├── models/            # Data models and schemas
│   ├── services/          # Business logic layer
│   ├── middleware/        # Custom middleware
│   ├── schema.rs          # Diesel schema (auto-generated)
│   ├── lib.rs            # Library crate
│   └── main.rs           # Application entry point
├── migrations/            # Database migration files
├── tests/                # Integration tests
└── Cargo.toml           # Dependencies and configuration
```

### Key Components

- **Handlers**: HTTP endpoint implementations with request/response handling
- **Services**: Business logic layer with database operations
- **Models**: Data structures and validation logic
- **Middleware**: Custom middleware for security, logging, and request processing

## 🔧 Configuration

### Environment Variables

- `RUST_ENV`: Environment mode (`development` or `production`)
- `DATABASE_URL`: SQLite database path (default: `vereinsknete.db`)
- `RUST_LOG`: Logging level (`debug`, `info`, `warn`, `error`)
- `PORT`: Server port (default: `8080`)
- `HOST`: Server host (default: `0.0.0.0`)

### Environment Files

- `.env.development`: Development configuration
- `.env.production`: Production configuration
- `.env.local`: Local overrides (not committed)

## 🧪 Testing

```bash
# Run all tests
cargo test

# Run tests with output
cargo test -- --nocapture

# Run specific test module
cargo test services::client

# Run integration tests
cargo test --test integration_tests
```

### Test Database

Tests use isolated SQLite databases with automatic migration handling:

```rust
// Each test gets a fresh database with migrations applied
let pool = create_test_db();
```

## 📦 Dependencies

### Core Dependencies

- **actix-web**: Web framework
- **diesel**: ORM and query builder
- **diesel_migrations**: Automatic migration handling
- **serde**: Serialization/deserialization
- **chrono**: Date and time handling

### Development Dependencies

- **tokio-test**: Async testing utilities
- **tempfile**: Temporary file handling for tests
- **serial_test**: Sequential test execution

## 🚀 Deployment

### Production Build

```bash
cargo build --release
```

### Docker

The backend is containerized with automatic migration handling:

```dockerfile
# Migrations are embedded in the binary
COPY backend/migrations ./migrations
RUN cargo build --release

# Migrations run automatically on container startup
CMD ["./target/release/backend"]
```

### Home Assistant Add-on

The backend is integrated into a Home Assistant add-on with:

- Automatic configuration from Home Assistant options
- Embedded database migrations
- Proper logging integration
- Graceful shutdown handling

See `run.sh` for the complete startup script implementation.

## 🔍 Monitoring

### Health Checks

- `GET /health`: Basic health check
- `GET /health/ready`: Readiness check (includes database connectivity)

### Logging

Structured logging with configurable levels:

```bash
# Debug logging
RUST_LOG=debug cargo run

# Info logging (production default)
RUST_LOG=info cargo run
```

## 🛠️ Development

### Hot Reloading

```bash
# Install cargo-watch for automatic rebuilds
cargo install cargo-watch

# Run with hot reloading
cargo watch -x run
```

### Database Inspection

```bash
# Connect to SQLite database
sqlite3 vereinsknete.db

# View schema
.schema

# View tables
.tables
```

---

For more information, see the main [README.md](../README.md) and [ENVIRONMENT.md](../ENVIRONMENT.md).