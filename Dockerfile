# syntax=docker/dockerfile:1

# Home Assistant Add-on Multi-stage Dockerfile for VereinsKnete
# Optimized for Home Assistant Supervisor with Alpine Linux base

# Build argument for Home Assistant base image
ARG BUILD_FROM=ghcr.io/home-assistant/amd64-base:latest
ARG RUST_VERSION=1.86
ARG NODE_VERSION=22

# Rust build stage - optimized for Alpine Linux
FROM rust:${RUST_VERSION}-alpine AS rust-builder
WORKDIR /backend

# Install build dependencies for Rust compilation
RUN apk add --no-cache \
    musl-dev \
    sqlite-dev \
    pkgconfig \
    openssl-dev \
    && rm -rf /var/cache/apk/*

# Copy dependency files first for better Docker layer caching
COPY backend/Cargo.toml backend/Cargo.lock ./

# Create dummy source to cache dependencies
RUN mkdir -p src && \
    echo "fn main() {}" > src/main.rs && \
    cargo build --release && \
    rm -rf src target/release/deps/backend*

# Copy actual source code and build
COPY backend/src ./src
COPY backend/migrations ./migrations
COPY backend/diesel.toml ./

# Build the release binary with optimizations
RUN cargo build --release && \
    strip target/release/backend

# Install diesel CLI for database migrations
RUN cargo install diesel_cli --no-default-features --features sqlite-bundled

# Node.js build stage - optimized for production
FROM node:${NODE_VERSION}-alpine AS frontend-builder
WORKDIR /frontend

# Install build dependencies
RUN apk add --no-cache \
    python3 \
    make \
    g++ \
    && rm -rf /var/cache/apk/*

# Copy package files and install dependencies
COPY frontend/package*.json ./
RUN npm ci --no-audit --no-fund

# Copy source code and build optimized production bundle
COPY frontend/ ./

# Set production environment variables for optimal build
ENV NODE_ENV=production \
    GENERATE_SOURCEMAP=false \
    INLINE_RUNTIME_CHUNK=false

RUN npm run build && \
    # Remove source maps and unnecessary files for smaller image
    find build -name "*.map" -delete && \
    find build -name "*.txt" -delete && \
    # Remove test files that might have been included
    find build -name "*.test.*" -delete && \
    # Optimize static assets with compression
    find build -type f -name "*.js" -exec gzip -9 -k {} \; && \
    find build -type f -name "*.css" -exec gzip -9 -k {} \; && \
    find build -type f -name "*.html" -exec gzip -9 -k {} \;

# Runtime stage - Home Assistant base image
FROM $BUILD_FROM

# Install runtime dependencies with security updates
RUN apk add --no-cache \
    sqlite \
    sqlite-libs \
    libgcc \
    libstdc++ \
    ca-certificates \
    tzdata \
    bash \
    wget \
    && apk upgrade --no-cache \
    && rm -rf /var/cache/apk/* /tmp/* /var/tmp/*

# Create application user for security (non-root execution)
RUN addgroup -g 1000 vereinsknete && \
    adduser -D -s /bin/bash -u 1000 -G vereinsknete vereinsknete

# Create necessary directories with proper permissions
RUN mkdir -p /app/static /data /share && \
    chown -R vereinsknete:vereinsknete /app /data /share && \
    chmod 755 /app /data /share

# Copy binaries from build stages
COPY --from=rust-builder --chown=vereinsknete:vereinsknete \
    /backend/target/release/backend /usr/local/bin/vereinsknete
COPY --from=rust-builder --chown=vereinsknete:vereinsknete \
    /usr/local/cargo/bin/diesel /usr/local/bin/diesel
COPY --from=rust-builder --chown=vereinsknete:vereinsknete \
    /backend/migrations /app/migrations

# Copy frontend build
COPY --from=frontend-builder --chown=vereinsknete:vereinsknete \
    /frontend/build /app/static

# Copy startup script (will be created in next task)
COPY --chown=vereinsknete:vereinsknete run.sh /usr/local/bin/run.sh
RUN chmod +x /usr/local/bin/run.sh

# Set proper file permissions for security
RUN chmod +x /usr/local/bin/vereinsknete /usr/local/bin/diesel

# Expose the application port
EXPOSE 8080

# Set environment variables for Home Assistant add-on
ENV RUST_LOG=info \
    RUST_BACKTRACE=1 \
    PATH="/usr/local/bin:$PATH"

# Health check for Home Assistant Supervisor
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1

# Use non-root user for security
USER vereinsknete

# Labels for Home Assistant add-on metadata
LABEL \
    io.hass.name="VereinsKnete" \
    io.hass.description="Freelance time tracking and invoicing application" \
    io.hass.arch="amd64|aarch64" \
    io.hass.type="addon" \
    io.hass.version="1.0.0" \
    maintainer="VereinsKnete Team" \
    org.opencontainers.image.title="VereinsKnete Home Assistant Add-on" \
    org.opencontainers.image.description="A fullstack Rust+React application for freelance time tracking and invoicing" \
    org.opencontainers.image.vendor="VereinsKnete" \
    org.opencontainers.image.licenses="MIT"

# Start the application
CMD ["/usr/local/bin/run.sh"]
