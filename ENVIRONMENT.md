# Environment Configuration

VereinsKnete supports development and production environments with automatic configuration management.

## Environments

| Environment | Database | Frontend | Backend | Logging |
|-------------|----------|----------|---------|---------|
| **Development** | `vereinsknete.db` | Dev server (:3000) | API only (:8080) | Debug |
| **Production** | `data/vereinsknete.db` | Static files | API + Static (:8080) | Info |

## Quick Commands

```bash
# Development (starts both backend and frontend in tmux)
./dev.sh

# Manual development setup
cd backend && cargo run          # Backend (:8080)
cd frontend && npm start         # Frontend (:3000)

# Production build
cd frontend && npm run build     # Build frontend
cd backend && cargo build --release  # Build backend

# Docker
docker-compose up -d                                                    # Development
docker build -t vereinsknete . && docker run -p 8080:8080 vereinsknete  # Production
```

## Environment Management

Environment files are automatically loaded based on the `RUST_ENV` variable:

- **Development**: Loads `.env.development` files (default)
- **Production**: Loads `.env.production` files when `RUST_ENV=production`

The backend automatically selects the appropriate environment file, and the frontend uses the corresponding `.env` file.

## Key Environment Variables

### Backend
- `RUST_ENV` - `development` or `production`
- `DATABASE_URL` - SQLite database path
- `RUST_LOG` - Logging level
- `PORT` - Server port (default: 8080)

### Frontend  
- `REACT_APP_API_URL` - Backend API endpoint
- `GENERATE_SOURCEMAP` - Source map generation

## Local Overrides

Create `.env.local` files for personal settings (not committed to git):
- `backend/.env.local` - Backend overrides
- `frontend/.env.local` - Frontend overrides

## Environment Files Structure

```
backend/
├── .env.development    # Development configuration
├── .env.production     # Production configuration  
└── .env               # Current active environment

frontend/
├── .env.development    # Development configuration
├── .env.production     # Production configuration
└── .env               # Current active environment
```
