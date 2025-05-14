# VereinsKnete

VereinsKnete is a web application that helps freelance service providers (mainly sports instructors) track billable hours and generate professional invoices.

## Features

- User profile management
- Client management
- Session tracking
- Invoice generation as PDF
- Modern, responsive UI

## Tech Stack

### Backend
- Rust with Actix-web
- Diesel ORM with SQLite
- PDF generation with genpdf

### Frontend
- React with TypeScript
- React Router for navigation
- Tailwind CSS for styling
- HeroIcons for UI icons

## Development Setup

### Prerequisites
- Rust (1.70+)
- Node.js (16+)
- SQLite
- libsqlite3-dev
- Diesel CLI

### Setting Up the Backend

1. Install Rust and Cargo from [rustup.rs](https://rustup.rs/)
2. Install the required system packages:
   ```
   sudo apt-get update && sudo apt-get install -y libsqlite3-dev
   ```
3. Install the Diesel CLI:
   ```
   cargo install diesel_cli --no-default-features --features sqlite
   ```
4. Navigate to the backend directory:
   ```
   cd backend
   ```
5. Set up the database:
   ```
   diesel setup --database-url=database.sqlite
   diesel migration run --database-url=database.sqlite
   ```
6. Run the backend server:
   ```
   cargo run
   ```

### Setting Up the Frontend

1. Install Node.js from [nodejs.org](https://nodejs.org/)
2. Navigate to the frontend directory:
   ```
   cd frontend
   ```
3. Install dependencies:
   ```
   npm install
   ```
4. Start the development server:
   ```
   npm start
   ```

## Docker Deployment

The application includes Docker and Docker Compose configurations for easy deployment:

1. Make sure Docker and Docker Compose are installed
2. Build and run the application:
   ```
   docker-compose up -d
   ```

The application will be available at http://localhost:8080

## Project Structure

```
VereinsKnete/
├── backend/             # Rust backend
│   ├── migrations/      # Database migrations
│   ├── src/             # Source code
│   │   ├── handlers/    # API request handlers
│   │   ├── models/      # Data models
│   │   ├── services/    # Business logic
│   │   ├── schema/      # Database schema
│   │   └── main.rs      # Application entry point
│   └── Cargo.toml       # Package dependencies
├── frontend/            # React frontend
│   ├── public/          # Static files
│   ├── src/             # Source code
│   │   ├── components/  # Reusable UI components
│   │   ├── pages/       # Application pages
│   │   ├── services/    # API service
│   │   └── types/       # TypeScript types
│   └── package.json     # Package dependencies
├── Dockerfile           # Docker build instructions
└── docker-compose.yml   # Docker Compose configuration
```
