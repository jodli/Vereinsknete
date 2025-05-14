## Project Overview

*   **Product Name**: VereinsKnete
*   **Core Purpose**: Solves the problem of freelance service providers (initially sports instructors) struggling with time-consuming, error-prone, manual tracking of billable hours and invoice generation across different clients with varying rates.
*   **MVP Goal**: Enable users to quickly and accurately record billable sessions and generate a basic, correct invoice for a specific client, significantly reducing manual effort and errors.
*   **Target Audience**: Newly self-employed sports instructors overwhelmed by manual invoicing and basic bookkeeping. Potentially useful for other freelancers with similar hourly billing needs.

## Technical Specifications (from Tech Design Doc)

*   **Platform**: Web application (single-user, runs locally, accessed via a web browser).
*   **Tech Stack (Frontend)**:
    *   Language: JavaScript/TypeScript
    *   Framework/Library: React
    *   Styling: Tailwind CSS
*   **Tech Stack (Backend/Core)**:
    *   Language: Rust
    *   Web Framework: Actix-web
    *   ORM: Diesel
    *   Database: SQLite
*   **Key Libraries/APIs**:
    *   Rust: `chrono` (date/time), `serde` (serialization/deserialization).
    *   PDF Generation (Rust): `genpdf`, `printpdf`, or `lopdf` (to be chosen).
    *   Deployment: Docker (for containerization).
*   **Architecture Overview**: A single-user local web application with a React frontend and a Rust (Actix-web) backend. The backend handles business logic, interacts with a local SQLite database via Diesel ORM, and generates PDF invoices. Communication is via RESTful APIs.
*   **Data Handling Notes**:
    *   Storage: All user data (personal details, client info, sessions) stored in a local SQLite database file (e.g., `VereinsKnete.sqlite`) on the user's machine.
    *   Privacy: No data transmitted externally.
    *   Security (MVP): Relies on standard file system security of the user's machine.
*   **Error Handling Approach**:
    *   Backend: Use Rust's `Result` type. Map errors to HTTP status codes and user-friendly JSON error messages.
    *   Frontend: Display clear, user-friendly error messages from the API; avoid technical jargon.

## Core MVP Features & Implementation Plan (from PRD & Tech Design Doc)

### Feature: User Profile/Master Data Management
*   **Description**: Allow the user to enter and save their personal details (name, address, tax ID, bank details) required for invoices.
*   **Key Acceptance Criteria/User Story**: User can input and persist their invoicing details.
*   **Technical Implementation Notes**:
    *   Frontend: React form for input.
    *   Backend: API endpoint (e.g., `PUT /api/profile`, `GET /api/profile`) to save/retrieve user details. Store in a dedicated `user_profile` table in SQLite.
*   **Agent Implementation Steps (Suggested)**:
    1.  Backend: Define Rust struct for User Profile data (name, address, tax ID, bank details).
    2.  Backend: Create Diesel migration for a `user_profile` table (single row expected).
    3.  Backend: Implement Actix-web handlers for `GET /api/profile` (retrieve) and `PUT /api/profile` (create/update).
    4.  Backend: Implement service logic using Diesel to interact with the `user_profile` table.
    5.  Frontend: Create a React component with a form for "My Details".
    6.  Frontend: Implement state management for the form.
    7.  Frontend: Implement API calls to fetch profile on load and save on submit.

### Feature: Client Management
*   **Description**: Enable the user to add, view, and edit details for clients (name, address, contact person, default hourly rate for that client if applicable).
*   **Key Acceptance Criteria/User Story**: User can manage a list of their clients, including their specific details and default hourly rates.
*   **Technical Implementation Notes**:
    *   Frontend: Forms for adding/editing clients, list display (tabular).
    *   Backend: CRUD API endpoints for clients. Store in a `clients` table with fields like name, address, contact person, default_hourly_rate.
*   **Agent Implementation Steps (Suggested)**:
    1.  Backend: Define Rust struct for Client data.
    2.  Backend: Create Diesel migration for `clients` table (id, name, address, contact_person, default_hourly_rate).
    3.  Backend: Implement Actix-web handlers for CRUD operations on `/api/clients` (e.g., `POST`, `GET /`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`).
    4.  Backend: Implement service logic for client CRUD operations using Diesel.
    5.  Frontend: Create React components for:
        *   Adding/Editing a client (form).
        *   Displaying a list of clients (table).
    6.  Frontend: Implement API calls to manage clients (create, read, update, delete).

### Feature: Session Logging
*   **Description**: Allow the user to log individual billable sessions, including name, date, start time, end time (or duration), and associate them with a specific client.
*   **Key Acceptance Criteria/User Story**: "As a self-employed service provider... I want to record my session details (name, date, start/end time, client) immediately after each session, so that I can easily generate an accurate invoice..."
*   **Technical Implementation Notes**:
    *   Frontend: Form with name input, date picker, time inputs, client selection (dropdown populated from clients). Tabular display for session list.
    *   Backend: API to save session details. Store in a `sessions` table, linking to clients (foreign key). Use `chrono` for date/time.
*   **Agent Implementation Steps (Suggested)**:
    1.  Backend: Define Rust struct for Session data (include client_id FK, name, date, start_time, end_time/duration).
    2.  Backend: Create Diesel migration for `sessions` table.
    3.  Backend: Implement Actix-web handlers for `POST /api/sessions` (log new session) and `GET /api/sessions` (list sessions, potentially with filters).
    4.  Backend: Implement service logic to save session data (using `chrono`) and associate with a client.
    5.  Frontend: Create React component for Session Logging form (name, date, start/end time or duration, client dropdown).
    6.  Frontend: Populate client dropdown by fetching data from `/api/clients`.
    7.  Frontend: Implement API call to submit a new session.
    8.  Frontend: Create React component to display a list of logged sessions (table).

### Feature: Invoice Generation
*   **Description**: Enable the user to select a client and a date range (e.g., a specific month) to generate an invoice. The invoice will list all logged sessions for that client within that period, calculate the total amount due, and include user's and client's details.
*   **Key Acceptance Criteria/User Story**: User can select a client and period, and the system generates data for an invoice including all relevant sessions and total amount.
*   **Technical Implementation Notes**:
    *   Frontend: Interface to select client and date range.
    *   Backend: API endpoint that:
        *   Fetches user profile data.
        *   Fetches client data.
        *   Fetches all relevant sessions from the `sessions` table for the selected client and date range.
        *   Calculates total hours and amount due.
        *   Passes data to the PDF Generation Service.
*   **Agent Implementation Steps (Suggested)**:
    1.  Frontend: Create React component ("Generate Invoice") with:
        *   Client selection dropdown (populated from `/api/clients`).
        *   Date range pickers (e.g., for month/year or specific start/end dates).
        *   "Generate Invoice" button.
    2.  Backend: Design API endpoint (e.g., `POST /api/invoices/generate` or `GET /api/invoices/preview?clientId=X&startDate=Y&endDate=Z`) that takes client ID and date range.
    3.  Backend: Implement service logic for this endpoint:
        *   Fetch User Profile from `user_profile` table.
        *   Fetch Client details from `clients` table.
        *   Fetch relevant Sessions from `sessions` table (filter by client_id and date range).
        *   Calculate duration for each session and total hours.
        *   Calculate total amount (total hours * client's default_hourly_rate).
        *   Assemble all data required for the invoice (user details, client details, list of sessions with names/dates/durations/amounts, total amount).
        *   This data will then be passed to the PDF generation step (see next feature).

### Feature: Basic Invoice Viewing/Downloading (PDF)
*   **Description**: Allow the user to view the generated invoice and download it as a simple PDF document.
*   **Key Acceptance Criteria/User Story**: User receives a downloadable PDF invoice containing accurate information.
*   **Technical Implementation Notes**:
    *   Backend: PDF Generation Service (Rust module) uses a PDF library (e.g., `genpdf`) to construct the PDF. The API endpoint returns the PDF as a file stream (`application/pdf`).
    *   Frontend: Trigger download of the received PDF.
*   **Agent Implementation Steps (Suggested)**:
    1.  Backend: Choose and integrate a Rust PDF generation library (e.g., `genpdf`).
    2.  Backend: Create a PDF Generation Service/module in Rust. This service takes structured invoice data (user info, client info, session items, totals) as input.
    3.  Backend: Implement logic within the PDF Generation Service to create a standard, clean PDF layout.
    4.  Backend: Modify the invoice generation API endpoint (from previous feature) to call the PDF Generation Service and return the generated PDF as a byte stream with `Content-Type: application/pdf` and appropriate `Content-Disposition` headers for download.
    5.  Frontend: When the "Generate Invoice" button is clicked and the API call is made, on successful response, trigger a browser download of the PDF file.

## UI/UX Concept (from PRD)

*   Clean, modern, and highly intuitive web interface.
*   Simple Navigation: Clear sidebar or top navigation for "My Details," "Clients," "Log Session / Sessions List," "Generate Invoice / Invoices."
*   Form-Based Data Entry: Clear, well-labeled forms with appropriate input types (e.g., date pickers).
*   Action-Oriented Buttons: Prominent and clearly labeled buttons (e.g., "Save," "Add New," "Generate Invoice").
*   Tabular Data Display: Simple, scannable tables for lists of sessions, clients, and invoices.
*   Invoice Preview Area: A clean display of the generated invoice data before downloading (could be a summary of data that goes into the PDF).
*   Prioritize usability for users who are not highly tech-savvy.

## Out of Scope for MVP (from PRD)

*   User Accounts & Authentication (single-user, local application).
*   Cloud Sync/Backup.
*   Advanced Financial Calculations/Reporting (e.g., detailed tax calculations, profit/loss).
*   Direct Emailing of Invoices from the app.
*   Multiple User Profiles/Businesses within one instance.
*   Highly Customizable Invoice Templates (a standard, clean format will be used).
*   Integrations with other software (e.g., calendars, accounting tools).
*   Mobile-specific native apps (iOS/Android) – will be a web app accessible via mobile browser.
*   Client-type specific fields or logic (beyond basic contact/rate info).

## Key Agent Instructions

*   Agent: Please generate the MVP codebase based on the details above.
*   Prioritize implementing the features exactly as specified in the 'Core MVP Features & Implementation Plan' section.
*   Strictly adhere to the 'Technical Specifications' regarding platform, stack, and architecture.
*   Refer to the full PRD (`PRD-MVP.txt`) and Tech Design Doc (`Tech-Design-MVP.txt`) files in the project root for complete details if needed.
*   Create files and directory structures as logically required by the Tech Design Doc and implementation plan (e.g., `frontend/src/components`, `backend/src/handlers`, `backend/src/services`, `backend/src/models`).
*   Add comments to explain complex logic or important decisions.
*   For the backend, ensure Diesel schema and migrations are set up correctly for SQLite.
*   For the frontend, use functional components with hooks in React.