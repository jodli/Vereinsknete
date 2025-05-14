# syntax=docker/dockerfile:1

# Build stage for the backend
FROM rust:1.73 as backend-builder
WORKDIR /app
COPY backend/Cargo.toml backend/Cargo.lock ./
# Create a dummy main.rs to cache dependencies
RUN mkdir -p src && \
    echo "fn main() {}" > src/main.rs && \
    cargo build --release && \
    rm -rf src target/release/deps/vereinsknete*

COPY backend/src ./src
COPY backend/migrations ./migrations
COPY backend/diesel.toml ./
RUN cargo build --release

# Build stage for the frontend
FROM node:18 as frontend-builder
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Final stage combining both services
FROM debian:bookworm-slim
WORKDIR /app

RUN apt-get update && apt-get install -y \
    libsqlite3-0 \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Create necessary directories
RUN mkdir -p /app/fonts /app/pdf_output /app/data

# Copy backend binary and necessary files
COPY --from=backend-builder /app/target/release/vereinsknete /app/
COPY --from=backend-builder /app/migrations /app/migrations
COPY backend/fonts/ /app/fonts/

# Copy frontend build
COPY --from=frontend-builder /app/build /app/public

EXPOSE 8080

ENV DATABASE_URL=/app/data/database.sqlite
ENV RUST_LOG=info

# Initialize the database on first run
RUN echo '#!/bin/bash\n\
if [ ! -f "$DATABASE_URL" ]; then\n\
  echo "Initializing database..."\n\
  diesel migration run --database-url="$DATABASE_URL"\n\
fi\n\
exec /app/vereinsknete' > /app/entrypoint.sh && \
chmod +x /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]
