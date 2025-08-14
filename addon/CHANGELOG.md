# Changelog

All notable changes to the VereinsKnete Home Assistant Add-on will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial Home Assistant add-on implementation
- Multi-architecture support (aarch64, amd64)
- Ingress integration for seamless Home Assistant access
- Configurable database and invoice storage paths
- Comprehensive logging and error handling

### Changed
- Adapted VereinsKnete for containerized deployment
- Optimized for Home Assistant environment

### Security
- Integrated with Home Assistant's authentication system
- Secured data storage in persistent volumes

## [1.0.0] - 2025-01-14

### Added
- Initial release of VereinsKnete Home Assistant Add-on
- Full-stack Rust+React application packaging
- Time tracking and session management
- Client management with contact information
- PDF invoice generation
- Multi-language support (English, German)
- Responsive web interface
- Dashboard with business metrics
- Data persistence across updates
- Automatic database migrations
- Home Assistant Supervisor integration
- Ingress proxy support
- Configurable logging levels
- Multi-architecture container builds
- Comprehensive documentation

### Features
- **Client Management**: Add, edit, and manage client information
- **Session Tracking**: Track billable hours with start/end times
- **Invoice Generation**: Create professional PDF invoices
- **Dashboard**: Overview of recent activity and earnings
- **Profile Management**: Configure business information
- **Data Export**: Export data for backup or migration
- **Responsive Design**: Works on all device sizes

### Technical
- Rust backend with Actix-web framework
- React 19 frontend with TypeScript
- SQLite database for data storage
- Docker multi-stage builds
- Home Assistant ingress integration
- Automated CI/CD pipeline
- Security hardening and validation

### Documentation
- Complete installation guide
- Configuration reference
- Troubleshooting guide
- API documentation
- Development setup instructions

---

## Release Notes

### Version 1.0.0

This is the initial release of the VereinsKnete Home Assistant Add-on. The add-on packages the complete VereinsKnete application for easy installation and management through Home Assistant.

**Key Features:**
- Seamless integration with Home Assistant
- No separate authentication required
- Automatic data backup with Home Assistant
- Multi-architecture support
- Professional invoice generation
- Comprehensive time tracking

**Installation:**
1. Add the add-on repository to Home Assistant
2. Install the VereinsKnete add-on
3. Configure options if needed
4. Start the add-on
5. Access through the Home Assistant sidebar

**Upgrade Path:**
This is the initial release. Future updates will include automatic migration of existing data.

**Known Issues:**
- None at this time

**Support:**
For issues or questions, please visit the GitHub repository or check the documentation.