# 💰 VereinsKnete

A modern web application for freelance service providers to track billable hours and generate professional invoices. Built with Rust and React. 🏃‍♂️💼

## ✨ Features

- 👤 User profile management with multilingual support (German/English)
- 🤝 Client management with full CRUD operations
- ⏱️ Session tracking with time logging
- 📄 Professional PDF invoice generation
- 📊 Dashboard with overview metrics
- 📱 Responsive design for all devices

## 🛠️ Tech Stack

- **Backend**: Rust + Actix-web + Diesel ORM + SQLite
- **Frontend**: React 19 + TypeScript + Tailwind CSS
- **Testing**: Comprehensive test coverage for both frontend and backend

## 🚀 Quick Start

### Prerequisites
- Rust (1.70+) and Cargo
- Node.js (18+) and npm
- SQLite and libsqlite3-dev
- Diesel CLI (optional, only needed for creating new migrations): `cargo install diesel_cli --no-default-features --features sqlite`

### Development Setup

```bash
# Clone and setup
git clone <repository-url>
cd VereinsKnete

# Install dependencies and start development servers
./dev.sh
```

The `dev.sh` script starts both backend (`:8080`) and frontend (`:3000`) servers in tmux.

### Manual Setup

```bash
# Backend
cd backend
# Database migrations now run automatically on startup
cargo run

# Frontend (new terminal)
cd frontend
npm install && npm start
```

> **Note**: Database migrations are now handled automatically when the application starts. The application will create the database file and run all pending migrations on first startup.

## 🐳 Docker

```bash
# Development
docker-compose up -d

# Production
docker build -t vereinsknete .
docker run -p 8080:8080 -v $(pwd)/data:/app/data vereinsknete
```

> **Note**: When running in Docker, database migrations are handled automatically. The application will create the database and run all migrations on container startup.

## 🗄️ Database Management

VereinsKnete uses SQLite with automatic database migrations:

- **Automatic Setup**: Database and tables are created automatically on first startup
- **Migration Handling**: All pending migrations run automatically when the application starts
- **No Manual Setup Required**: Simply run `cargo run` and the database will be ready
- **Development**: Database file is created at `backend/vereinsknete.db`
- **Production**: Database location is configurable via `DATABASE_URL` environment variable

### Creating New Migrations (Development Only)

```bash
cd backend
# Create a new migration (requires Diesel CLI)
diesel migration generate migration_name

# Edit the generated up.sql and down.sql files
# Migrations will be applied automatically on next startup
```

## 🧪 Testing

```bash
# Frontend tests
cd frontend && npm test

# Backend tests  
cd backend && cargo test
```

## 📁 Project Structure

```
VereinsKnete/
├── backend/           # Rust backend (Actix-web + Diesel + SQLite)
│   ├── src/
│   │   ├── handlers/  # API endpoints
│   │   ├── models/    # Data models
│   │   ├── services/  # Business logic
│   │   └── main.rs
│   └── migrations/    # Database migrations
├── frontend/          # React frontend (TypeScript + Tailwind)
│   └── src/
│       ├── components/ # UI components
│       ├── pages/     # Page components
│       ├── services/  # API integration
│       └── i18n/      # Translations
├── specs/             # Project documentation
└── dev.sh            # Development startup script
```

## 📚 Documentation

- **[ENVIRONMENT.md](ENVIRONMENT.md)** - Environment configuration and deployment
- **[specs/](specs/)** - Technical specifications and requirements
- **Development Guidelines** - See `.kiro/steering/` for frontend and backend patterns

---

**VereinsKnete** - Simple freelance time tracking and invoicing 💰