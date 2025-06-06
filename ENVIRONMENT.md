# Environment Configuration

VereinsKnete supports multiple environments with different configurations for development and production.

## Available Environments

### Development Environment
- **Database**: Local SQLite file (`vereinsknete.db`)
- **Logging**: Debug level with verbose output
- **Frontend**: Runs on development server with hot reloading
- **Backend**: Serves API only, no static files
- **CORS**: Permissive for local development

### Production Environment
- **Database**: SQLite file in `data/` directory
- **Logging**: Info level for production
- **Frontend**: Built static files served by backend
- **Backend**: Serves both API and static files
- **CORS**: Configured for production use

## Environment Files

### Backend Environment Files
- `.env.development` - Development configuration
- `.env.production` - Production configuration
- `.env` - Active environment (created by env.sh script)

### Frontend Environment Files
- `.env.development` - Development configuration (with API URL pointing to localhost:8080)
- `.env.production` - Production configuration (with relative API URL)
- `.env` - Active environment (created by env.sh script)

## Usage

### Quick Start - Development
```bash
./env.sh development
./dev.sh
```

### Quick Start - Production Build
```bash
./build-prod.sh
```

### Manual Environment Setup

#### Switch to Development
```bash
./env.sh development
```

#### Switch to Production
```bash
./env.sh production
```

### Running in Different Environments

#### Development Mode
```bash
# Set up development environment
./env.sh development

# Start development servers (backend + frontend)
./dev.sh
```

#### Production Mode
```bash
# Build for production
./build-prod.sh

# Run production server
cd backend
RUST_ENV=production ./target/release/backend
```

#### Docker Production
```bash
# Build Docker image
docker build -t vereinsknete .

# Run container
docker run -p 8080:8080 -v $(pwd)/data:/app/data vereinsknete
```

## Environment Variables

### Backend Variables
- `RUST_ENV` - Environment mode (`development` or `production`)
- `DATABASE_URL` - Path to SQLite database file
- `ENVIRONMENT` - Application environment (controls static file serving)
- `RUST_LOG` - Logging level
- `HOST` - Server bind address (default: `0.0.0.0`)
- `PORT` - Server port (default: `8080`)

### Frontend Variables
- `GENERATE_SOURCEMAP` - Enable/disable source maps
- `REACT_APP_API_URL` - Backend API URL
- `REACT_APP_ENV` - Environment identifier

## Local Environment Overrides

You can create local environment files that won't be committed to git:
- `backend/.env.local` - Local backend overrides
- `frontend/.env.local` - Local frontend overrides

These files will be loaded in addition to the environment-specific files and can override any settings for your local development needs.
