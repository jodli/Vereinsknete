# Environment Configuration

VereinsKnete supports development and production environments with automatic configuration management.

## Environments

| Environment | Database | Frontend | Backend | Logging | Migrations | Notes |
|-------------|----------|----------|---------|---------|------------|-------|
| **Development** | `vereinsknete.db` | Dev server (:3000) | API only (:8080) | Debug | Auto | Local development |
| **Production** | `data/vereinsknete.db` | Static files | API + Static (:8080) | Info | Auto | Standalone deployment |
| **Home Assistant** | `/data/vereinsknete.db` | Static files | API + Static (:8080) | Info | Auto | Ingress integration |

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
- `DATABASE_URL` - SQLite database path (automatically created if not exists)
- `RUST_LOG` - Logging level
- `PORT` - Server port (default: 8080)
- `HOST` - Server host (default: 0.0.0.0 for Home Assistant compatibility)

### Database Management
- **Automatic Migrations**: Database schema is automatically created and updated on application startup
- **No Manual Setup**: Database file and tables are created automatically if they don't exist
- **Migration Embedding**: All migrations are embedded in the binary for reliable deployment

### Frontend  
- `REACT_APP_API_URL` - Backend API endpoint (uses relative URLs in production for ingress compatibility)
- `GENERATE_SOURCEMAP` - Source map generation (disabled in production for smaller bundle size)
- `INLINE_RUNTIME_CHUNK` - Runtime chunk inlining (disabled for Home Assistant compatibility)

## Home Assistant Integration

### Ingress Configuration
The application is optimized for Home Assistant's ingress proxy system:

- **Relative URLs**: All API calls use relative paths in production (`/api/*`)
- **No Authentication**: Relies on Home Assistant's authentication system
- **Responsive Design**: Optimized for Home Assistant's interface constraints
- **Bundle Optimization**: Minimized bundle size for faster loading in add-on environment

### Build Configuration
For Home Assistant add-on builds, use the optimized build command:

```bash
cd frontend
npm run build:addon  # Optimized build for Home Assistant
```

This build configuration:
- Disables source maps to reduce bundle size
- Uses relative API URLs for ingress compatibility
- Optimizes chunk splitting for add-on environment

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
