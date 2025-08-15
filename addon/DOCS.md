# VereinsKnete Add-on Documentation

## Installation

### Prerequisites

- Home Assistant OS, Supervised, or Container installation
- Supervisor add-on store access

### Installation Steps

1. **Add Repository**: Add this repository URL to your Home Assistant add-on store
2. **Install Add-on**: Find "VereinsKnete" in the add-on store and click "Install"
3. **Configure**: Review and adjust configuration options if needed
4. **Start**: Click "Start" to launch the add-on
5. **Access**: Use the "Open Web UI" button or access through the sidebar

## Configuration Options

### Database Configuration

- **database_path**: Location of the SQLite database file
  - Default: `/data/vereinsknete.db`
  - Must be within the `/data` directory for persistence

### Application Settings

- **port**: Internal port for the application
  - Default: `8080`
  - Usually doesn't need to be changed

- **log_level**: Logging verbosity
  - Options: `debug`, `info`, `warn`, `error`
  - Default: `info`
  - Use `debug` for troubleshooting

- **invoice_storage_path**: Directory for PDF invoices
  - Default: `/data/invoices`
  - Must be within the `/data` directory

### Example Configuration

```yaml
database_path: "/data/vereinsknete.db"
log_level: "info"
port: 8080
invoice_storage_path: "/data/invoices"
```

## Usage Guide

### Getting Started

1. **Profile Setup**: Configure your business information in the Profile section
2. **Add Clients**: Create client records with contact information and hourly rates
3. **Track Time**: Create sessions to track time spent on client work
4. **Generate Invoices**: Create professional PDF invoices from your sessions

### Client Management

- Add clients with name, address, and contact information
- Set default hourly rates for each client
- Edit client information as needed
- View client history and statistics

### Session Tracking

- Create sessions for specific clients
- Set start and end times
- Add descriptions for work performed
- Sessions automatically calculate billable hours

### Invoice Generation

- Generate PDF invoices from session data
- Customize invoice templates
- Download or email invoices to clients
- Track invoice status (draft, sent, paid)

## Troubleshooting

### Common Issues

#### Add-on Won't Start

1. Check the logs for error messages
2. Verify configuration options are valid
3. Ensure sufficient disk space is available
4. Try restarting Home Assistant

#### Database Issues

1. Check that the database path is writable
2. Verify the `/data` directory has proper permissions
3. Look for database corruption errors in logs

#### Web Interface Not Accessible

1. Verify the add-on is running
2. Check that ingress is enabled in configuration
3. Try refreshing the browser page
4. Check Home Assistant logs for proxy errors

### Log Analysis

Access logs through the Home Assistant add-on interface:

1. Go to Supervisor → VereinsKnete
2. Click on the "Log" tab
3. Look for error messages or warnings

Common log patterns:
- `[INFO] Starting VereinsKnete on port 8080` - Normal startup
- `[ERROR] Database connection failed` - Database issues
- `[WARN] Configuration validation failed` - Config problems

### Performance Optimization

- Use `info` or `warn` log level in production
- Regularly backup your data
- Monitor disk space usage
- Keep the add-on updated

## Data Management

### Backup and Restore

VereinsKnete data is automatically included in Home Assistant backups:

1. **Automatic Backups**: Data is included in scheduled backups
2. **Manual Backups**: Create backups before major changes
3. **Restore**: Data is restored with Home Assistant snapshots

### Data Location

All persistent data is stored in `/data/`:
- Database: `/data/vereinsknete.db`
- Invoices: `/data/invoices/`
- Configuration: `/data/options.json`

### Migration

When updating the add-on:
1. Database migrations run automatically
2. Existing data is preserved
3. New features are enabled automatically

## Security

### Access Control

- Access is controlled through Home Assistant authentication
- No separate login required
- Uses Home Assistant's ingress proxy for security

### Data Protection

- All data stored locally in Home Assistant
- No external data transmission
- SQLite database with file-level security

### Network Security

- Application runs on internal network only
- Accessed through Home Assistant's secure proxy
- No direct external access required

## Advanced Configuration

### Custom Database Location

```yaml
database_path: "/data/custom/vereinsknete.db"
```

### Debug Logging

```yaml
log_level: "debug"
```

### Custom Invoice Storage

```yaml
invoice_storage_path: "/data/custom_invoices"
```

## Support and Contributing

### Getting Help

1. Check this documentation first
2. Review the troubleshooting section
3. Check the GitHub issues page at https://github.com/jodli/vereinsknete-addon/issues
4. Create a new issue with logs and configuration

### Contributing

Contributions are welcome! Please see the main repository for development guidelines.

### Reporting Issues

When reporting issues, please include:
- Home Assistant version
- Add-on version
- Configuration (without sensitive data)
- Relevant log entries
- Steps to reproduce the issue