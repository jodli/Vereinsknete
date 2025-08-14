#!/usr/bin/with-contenv bashio

# Placeholder startup script for VereinsKnete Home Assistant Add-on
# This will be properly implemented in task 3: "Create startup script with configuration management"
# 
# Current functionality:
# - Basic environment setup
# - Directory creation
# - Application startup
#
# TODO (Task 3):
# - Read Home Assistant configuration from options.json
# - Implement database initialization and migration logic
# - Add proper logging and error handling
# - Create directory structure for persistent data storage

set -e

echo "Starting VereinsKnete Home Assistant Add-on..."

# Basic environment setup (will be enhanced in task 3)
export RUST_LOG="${RUST_LOG:-info}"
export DATABASE_URL="${DATABASE_URL:-sqlite:///data/vereinsknete.db}"
export PORT="${PORT:-8080}"
export INVOICE_STORAGE_PATH="${INVOICE_STORAGE_PATH:-/data/invoices}"

# Create data directories if they don't exist
mkdir -p /data
mkdir -p /data/invoices

# Start the application
echo "Starting VereinsKnete on port $PORT"
echo "Database: $DATABASE_URL"
echo "Invoice storage: $INVOICE_STORAGE_PATH"

exec /usr/local/bin/vereinsknete