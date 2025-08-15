# VereinsKnete Home Assistant Add-on

![Supports aarch64 Architecture][aarch64-shield] ![Supports amd64 Architecture][amd64-shield]

[aarch64-shield]: https://img.shields.io/badge/aarch64-yes-green.svg
[amd64-shield]: https://img.shields.io/badge/amd64-yes-green.svg

## About

VereinsKnete is a modern freelance time tracking and invoicing application that helps service providers manage their business operations. This Home Assistant add-on packages the full-stack Rust+React application for easy installation and management through the Home Assistant Supervisor interface.

## Features

- **Time Tracking**: Track billable hours with session management
- **Client Management**: Organize clients with contact information and hourly rates
- **Invoice Generation**: Create professional PDF invoices automatically
- **Dashboard**: Overview of recent sessions, earnings, and business metrics
- **Multi-language Support**: Available in English and German
- **Responsive Design**: Works on desktop, tablet, and mobile devices

## Installation

1. Add this repository to your Home Assistant add-on store
2. Install the "VereinsKnete" add-on
3. Configure the add-on options if needed
4. Start the add-on
5. Access VereinsKnete through the Home Assistant sidebar

## Configuration

### Option: `database_path`

The path where the SQLite database will be stored. Default: `/data/vereinsknete.db`

### Option: `log_level`

The log level for the application. Options: `debug`, `info`, `warn`, `error`. Default: `info`

### Option: `port`

The port the application will run on. Default: `8080`

### Option: `invoice_storage_path`

The directory where generated PDF invoices will be stored. Default: `/data/invoices`

## Usage

After installation and startup, VereinsKnete will be available through the Home Assistant sidebar. The application provides:

1. **Dashboard**: Overview of your freelance business
2. **Clients**: Manage client information and hourly rates
3. **Sessions**: Track time spent on client work
4. **Invoices**: Generate and manage PDF invoices
5. **Profile**: Configure your business information

## Data Persistence

All data is stored in Home Assistant's persistent data volumes and will survive add-on updates and restarts. Your client information, session records, and generated invoices are automatically backed up with Home Assistant's backup system.

## Support

For issues and feature requests, please visit the [GitHub repository](https://github.com/jodli/vereinsknete-addon).

## License

This add-on is licensed under the MIT License.

## Container Image

Images are published to GitHub Container Registry for both `linux/amd64` and `linux/arm64`:

```
ghcr.io/jodli/vereinsknete-addon:<tag>
```

Where `<tag>` corresponds to a released application version (e.g. `1.2.0`) or `latest`.