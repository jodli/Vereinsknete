# syntax=docker/dockerfile:1

# Build stage for the diesel CLI tool
FROM rust:1.86 as diesel-builder

# Install dependencies for building diesel_cli with sqlite
RUN apt-get update && \
    apt-get install -y libsqlite3-dev pkg-config && \
    rm -rf /var/lib/apt/lists/*

# Install diesel_cli with sqlite support
RUN cargo install diesel_cli --no-default-features --features sqlite

# Copy the diesel binary to a separate output directory for easy extraction
RUN mkdir -p /out && cp /usr/local/cargo/bin/diesel /out/


# Build stage for the backend
FROM rust:1.86 AS backend-builder
WORKDIR /app

# Install required system dependencies for diesel CLI
RUN apt-get update && apt-get install -y \
    libsqlite3-dev \
    pkg-config \
    && rm -rf /var/lib/apt/lists/*

# Copy dependency files first for better caching
COPY backend/Cargo.toml backend/Cargo.lock ./
# Create a dummy main.rs to cache dependencies
RUN mkdir -p src && \
    echo "fn main() {}" > src/main.rs && \
    cargo build --release && \
    rm -rf src target/release/deps/backend*

COPY backend/src ./src
COPY backend/migrations ./migrations
COPY backend/diesel.toml ./
RUN cargo build --release

# Build stage for the frontend
FROM node:22.16.0 AS frontend-builder
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Final stage combining both services
FROM debian:12-slim
WORKDIR /app

RUN apt-get update && apt-get install -y \
    libsqlite3-0 \
    ca-certificates \
    fonts-liberation \
    fonts-dejavu-core \
    fontconfig \
    && rm -rf /var/lib/apt/lists/*

# Create necessary directories with proper permissions
RUN mkdir -p /app/data /app/invoices && \
    chmod 755 /app/data /app/invoices

# Copy backend binary and necessary files
COPY --from=backend-builder /app/target/release/backend /app/
COPY --from=backend-builder /app/migrations /app/migrations
COPY --from=diesel-builder /out/diesel /usr/local/bin/

# Copy frontend build
COPY --from=frontend-builder /app/build /app/public

EXPOSE 8080

ENV DATABASE_URL=/app/data/database.sqlite
ENV RUST_LOG=info
ENV RUST_ENV=production

# Initialize the database on first run
RUN echo '#!/bin/bash\n\
set -e\n\
\n\
echo "Starting VereinsKnete..."\n\
\n\
# Initialize database if it does not exist\n\
if [ ! -f "$DATABASE_URL" ]; then\n\
  echo "Database not found. Initializing database at $DATABASE_URL..."\n\
  diesel migration run --database-url="$DATABASE_URL" --migration-dir="/app/migrations"\n\
  if [ $? -eq 0 ]; then\n\
    echo "Database initialized successfully."\n\
  else\n\
    echo "Failed to initialize database." >&2\n\
    exit 1\n\
  fi\n\
else\n\
  echo "Database found at $DATABASE_URL."\n\
fi\n\
\n\
echo "Starting application..."\n\
exec /app/backend' > /app/entrypoint.sh && \
chmod +x /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]
