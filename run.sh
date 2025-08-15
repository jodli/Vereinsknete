#!/usr/bin/with-contenv bashio

# VereinsKnete Home Assistant Add-on Startup Script
# Implements configuration management, database initialization, and proper logging
# Requirements: 3.1, 3.3, 5.4, 6.3

set -e

# Configuration file path
CONFIG_PATH="/data/options.json"

# Logging functions with proper Home Assistant integration
log_info() {
    bashio::log.info "$1"
}

log_warn() {
    bashio::log.warning "$1"
}

log_error() {
    bashio::log.error "$1"
}

log_debug() {
    bashio::log.debug "$1"
}

log_fatal() {
    bashio::log.fatal "$1"
    exit 1
}

# Function to read configuration from Home Assistant options.json
read_configuration() {
    log_info "Reading Home Assistant configuration..."
    
    # Check if configuration file exists
    if [[ ! -f "$CONFIG_PATH" ]]; then
        log_fatal "Configuration file not found at $CONFIG_PATH"
    fi
    
    # Read configuration values using bashio
    DATABASE_PATH=$(bashio::config 'database_path' '/data/vereinsknete.db')
    LOG_LEVEL=$(bashio::config 'log_level' 'info')
    PORT=$(bashio::config 'port' '8080')
    INVOICE_STORAGE_PATH=$(bashio::config 'invoice_storage_path' '/data/invoices')
    
    # Validate configuration values
    if [[ -z "$DATABASE_PATH" ]]; then
        log_fatal "Database path cannot be empty"
    fi
    
    if [[ ! "$LOG_LEVEL" =~ ^(debug|info|warn|error)$ ]]; then
        log_warn "Invalid log level '$LOG_LEVEL', defaulting to 'info'"
        LOG_LEVEL="info"
    fi
    
    if [[ ! "$PORT" =~ ^[0-9]+$ ]] || [[ "$PORT" -lt 1 ]] || [[ "$PORT" -gt 65535 ]]; then
        log_warn "Invalid port '$PORT', defaulting to 8080"
        PORT="8080"
    fi
    
    if [[ -z "$INVOICE_STORAGE_PATH" ]]; then
        log_fatal "Invoice storage path cannot be empty"
    fi
    
    log_info "Configuration loaded successfully:"
    log_info "  Database path: $DATABASE_PATH"
    log_info "  Log level: $LOG_LEVEL"
    log_info "  Port: $PORT"
    log_info "  Invoice storage: $INVOICE_STORAGE_PATH"
}

# Function to create directory structure for persistent data storage
create_directory_structure() {
    log_info "Creating directory structure for persistent data storage..."
    
    # Create main data directory
    if ! mkdir -p /data; then
        log_fatal "Failed to create /data directory"
    fi
    
    # Create database directory (if database path contains subdirectories)
    local db_dir
    db_dir=$(dirname "$DATABASE_PATH")
    if [[ "$db_dir" != "." ]] && [[ "$db_dir" != "/" ]]; then
        if ! mkdir -p "$db_dir"; then
            log_fatal "Failed to create database directory: $db_dir"
        fi
        log_debug "Created database directory: $db_dir"
    fi
    
    # Create invoice storage directory
    if ! mkdir -p "$INVOICE_STORAGE_PATH"; then
        log_fatal "Failed to create invoice storage directory: $INVOICE_STORAGE_PATH"
    fi
    log_debug "Created invoice storage directory: $INVOICE_STORAGE_PATH"
    
    # Set proper permissions for data directories
    chmod 755 /data
    chmod 755 "$INVOICE_STORAGE_PATH"
    
    # Create logs directory for application logs
    if ! mkdir -p /data/logs; then
        log_warn "Failed to create logs directory, continuing without it"
    else
        chmod 755 /data/logs
        log_debug "Created logs directory: /data/logs"
    fi
    
    log_info "Directory structure created successfully"
}

# Function to initialize database and run migrations
initialize_database() {
    log_info "Initializing database..."
    
    local db_exists=false
    
    # Check if database file already exists
    if [[ -f "$DATABASE_PATH" ]]; then
        log_info "Database file exists at $DATABASE_PATH"
        db_exists=true
    else
        log_info "Database file does not exist, will be created during first connection"
    fi
    
    # Set database URL for Diesel
    export DATABASE_URL="sqlite://$DATABASE_PATH"
    log_debug "Database URL set to: $DATABASE_URL"
    
    # Test database connectivity by attempting to create a connection
    # We'll let the Rust application handle the actual migrations since diesel_migrations
    # is included in the dependencies and the application can run migrations on startup
    
    # Ensure database file has proper permissions if it exists
    if [[ -f "$DATABASE_PATH" ]]; then
        chmod 644 "$DATABASE_PATH"
        log_debug "Set database file permissions to 644"
    fi
    
    # The Rust application will handle database migrations automatically
    # using the diesel_migrations crate when it starts up
    
    if [[ "$db_exists" == "true" ]]; then
        log_info "Database initialization completed (existing database)"
    else
        log_info "Database initialization prepared (new database will be created)"
    fi
}

# Function to setup environment variables for Rust application
setup_environment_variables() {
    log_info "Setting up environment variables for Rust application..."
    
    # Core application environment
    export RUST_ENV="production"
    export RUST_LOG="$LOG_LEVEL"
    export DATABASE_URL="sqlite://$DATABASE_PATH"
    export PORT="$PORT"
    export HOST="0.0.0.0"
    
    # Application-specific paths
    export INVOICE_STORAGE_PATH="$INVOICE_STORAGE_PATH"
    export STATIC_FILES_PATH="/app/static"
    
    # Home Assistant specific environment
    export HA_ADDON="true"
    export HA_INGRESS="true"
    
    # Logging configuration
    export RUST_BACKTRACE="1"
    
    log_debug "Environment variables set:"
    log_debug "  RUST_ENV=$RUST_ENV"
    log_debug "  RUST_LOG=$RUST_LOG"
    log_debug "  DATABASE_URL=$DATABASE_URL"
    log_debug "  PORT=$PORT"
    log_debug "  HOST=$HOST"
    log_debug "  INVOICE_STORAGE_PATH=$INVOICE_STORAGE_PATH"
    log_debug "  STATIC_FILES_PATH=$STATIC_FILES_PATH"
    
    log_info "Environment variables configured successfully"
}

# Function to perform pre-startup health checks
perform_health_checks() {
    log_info "Performing pre-startup health checks..."
    
    # Check if the VereinsKnete binary exists
    if [[ ! -f "/usr/local/bin/vereinsknete" ]]; then
        log_fatal "VereinsKnete binary not found at /usr/local/bin/vereinsknete"
    fi
    
    # Check if binary is executable
    if [[ ! -x "/usr/local/bin/vereinsknete" ]]; then
        log_fatal "VereinsKnete binary is not executable"
    fi
    
    # Check if static files directory exists (for production mode)
    if [[ ! -d "$STATIC_FILES_PATH" ]]; then
        log_warn "Static files directory not found at $STATIC_FILES_PATH"
        log_warn "Frontend may not be accessible through ingress"
    fi
    
    # Check disk space for data directory
    local available_space
    available_space=$(df /data | awk 'NR==2 {print $4}')
    if [[ "$available_space" -lt 100000 ]]; then  # Less than ~100MB
        log_warn "Low disk space available for data directory: ${available_space}KB"
    fi
    
    # Check if port is available (basic check)
    if command -v netstat >/dev/null 2>&1; then
        if netstat -ln | grep -q ":$PORT "; then
            log_fatal "Port $PORT is already in use"
        fi
    fi
    
    log_info "Health checks completed successfully"
}

# Function to handle graceful shutdown
cleanup() {
    local exit_code=$?
    log_info "Received shutdown signal, cleaning up..."
    
    # Kill the application if it's running
    if [[ -n "$APP_PID" ]]; then
        log_info "Stopping VereinsKnete application (PID: $APP_PID)..."
        kill -TERM "$APP_PID" 2>/dev/null || true
        
        # Wait for graceful shutdown
        local count=0
        while kill -0 "$APP_PID" 2>/dev/null && [[ $count -lt 30 ]]; do
            sleep 1
            ((count++))
        done
        
        # Force kill if still running
        if kill -0 "$APP_PID" 2>/dev/null; then
            log_warn "Application did not shut down gracefully, forcing termination"
            kill -KILL "$APP_PID" 2>/dev/null || true
        fi
    fi
    
    log_info "Cleanup completed"
    exit $exit_code
}

# Set up signal handlers for graceful shutdown
trap cleanup SIGTERM SIGINT SIGQUIT

# Main startup sequence
main() {
    log_info "Starting VereinsKnete Home Assistant Add-on v1.0.0"
    log_info "=================================================="
    
    # Step 1: Read configuration from Home Assistant
    read_configuration
    
    # Step 2: Create directory structure for persistent data storage
    create_directory_structure
    
    # Step 3: Setup environment variables for Rust application
    setup_environment_variables
    
    # Step 4: Initialize database and run migrations
    initialize_database
    
    # Step 5: Perform pre-startup health checks
    perform_health_checks
    
    # Step 6: Start the application
    log_info "Starting VereinsKnete application..."
    log_info "Listening on http://$HOST:$PORT"
    log_info "Database: $DATABASE_URL"
    log_info "Invoice storage: $INVOICE_STORAGE_PATH"
    log_info "Log level: $LOG_LEVEL"
    
    # Start the application in the background to capture PID
    /usr/local/bin/vereinsknete &
    APP_PID=$!
    
    log_info "VereinsKnete started successfully (PID: $APP_PID)"
    log_info "Add-on is ready for use through Home Assistant ingress"
    
    # Wait for the application to finish
    wait $APP_PID
    local exit_code=$?
    
    if [[ $exit_code -eq 0 ]]; then
        log_info "VereinsKnete application exited normally"
    else
        log_error "VereinsKnete application exited with code: $exit_code"
    fi
    
    exit $exit_code
}

# Start the main function
main "$@"