
# Home Assistant Add-on Development Guide
*A Complete Reference for Converting Fullstack Web Apps to Home Assistant Add-ons*

## Overview
Home Assistant add-ons are containerized applications that extend Home Assistant's functionality. They run as Docker containers and are managed through the Supervisor panel. Add-ons can be anything from MQTT brokers to web applications, making them perfect for integrating fullstack Rust+React applications.

## Core Concepts

### Add-on Architecture
- **Container-based**: Add-ons are Docker containers published to registries (GitHub Container Registry, Docker Hub)
- **Supervisor-managed**: Home Assistant Supervisor handles lifecycle, configuration, and networking
- **Repository structure**: GitHub repositories can contain multiple add-ons for community sharing

### File Structure
```
addon_name/
â”œâ”€â”€ translations/
â”‚   â””â”€â”€ en.yaml
â”œâ”€â”€ apparmor.txt
â”œâ”€â”€ build.yaml
â”œâ”€â”€ CHANGELOG.md
â”œâ”€â”€ config.yaml          # Main configuration
â”œâ”€â”€ DOCS.md
â”œâ”€â”€ Dockerfile           # Container definition
â”œâ”€â”€ icon.png
â”œâ”€â”€ logo.png
â”œâ”€â”€ README.md
â””â”€â”€ run.sh              # Startup script
```

## Configuration (`config.yaml`)

### Essential Fields
```yaml
name: "Your App Name"
version: "1.0.0"
slug: "your_app"
description: "Your application description"
arch:
  - aarch64
  - amd64
  - armhf
  - armv7
  - i386
```

### Key Configuration Options
- **`image`**: Container image reference (e.g., `repo/{arch}-addon-name`)
- **`ports`**: Port mappings (`8080/tcp: 8080`)
- **`ingress`**: Enable Home Assistant's ingress proxy (recommended: `true`)
- **`ingress_port`**: Port for ingress (default: 8099)
- **`webui`**: Web interface URL pattern
- **`startup`**: Startup type (`application`, `system`, `services`)
- **`boot`**: Auto-start behavior (`auto`, `manual`)

### Security & Permissions
- **`host_network`**: Access to host network (rating penalty: -1)
- **`privileged`**: Elevated privileges (various ratings)
- **`auth_api`**: Access to Home Assistant auth (+1 rating)
- **`hassio_api`**: Access to Supervisor API
- **`homeassistant_api`**: Access to Home Assistant API

### Storage & Data
- **`map`**: Volume mappings (share, ssl, homeassistant_config)
- **`options`**: User-configurable options schema
- **`schema`**: JSON schema for option validation

## Docker Configuration

### Dockerfile Structure
```dockerfile
ARG BUILD_FROM
FROM $BUILD_FROM

# Install dependencies
RUN apk add --no-cache     curl     jq

# Copy application files
COPY . /app
WORKDIR /app

# Copy startup script
COPY run.sh /
RUN chmod a+x /run.sh

CMD ["/run.sh"]
```

### Build Arguments
- **`BUILD_FROM`**: Base image for dynamic builds
- **`BUILD_VERSION`**: Add-on version from config.yaml
- **`BUILD_ARCH`**: Current build architecture

### Multi-Architecture Support
Use `{arch}` placeholder in image names:
```yaml
image: "your-registry/app-{arch}-addon"
```

## Development Workflow

### 1. Local Development (Recommended: VS Code Devcontainer)
- **Setup**: Use VS Code with Remote-Containers extension
- **Template**: Available devcontainer templates for add-on development
- **Environment**: Includes Supervisor and Home Assistant for testing
- **Mapping**: Add-ons appear in "Local Add-ons" section
- **Port**: Home Assistant accessible on port 8123

### 2. Remote Development
- **SSH Add-on**: Access Home Assistant via SSH
- **Samba Add-on**: File system access via network share
- **Path**: Copy add-ons to `/addons` directory
- **Testing**: Reload Supervisor to detect changes

### 3. Local Build Testing
```bash
# Build locally
docker build   --build-arg BUILD_FROM="ghcr.io/home-assistant/amd64-base:latest"   -t local/my-addon   .

# Run locally
docker run   --rm   -v /tmp/test_data:/data   -p 8080:8080   local/my-addon
```

## Publishing Options

### 1. Pre-built Containers (Recommended)
**Advantages**:
- Fast installation for users
- Lower failure rate
- Better user experience
- Reduced wear on user's storage

**Process**:
1. Build images for all architectures
2. Push to container registry
3. Users download ready-to-run containers

### 2. Local Build
**Use Cases**:
- Early development/testing
- Proof of concept
- Community interest validation

**Disadvantages**:
- Slow installation
- Higher failure rate
- More storage wear
- Will be marked in add-on store as warning

## Container Registry Setup

### GitHub Container Registry (Recommended)
```bash
# Build and push with Home Assistant builder
docker run   --rm   --privileged   -v ~/.docker:/root/.docker   ghcr.io/home-assistant/amd64-builder   --all   -t addon-folder   -r https://github.com/username/addon-repo   -b main
```

### Docker Hub
- Requires Docker Hub account
- Configure credentials in Home Assistant (for private images)
- Path: Supervisor â†’ Add-on Store â†’ Registries

### GitHub Actions Example
```yaml
name: Publish
on:
  release:
    types: [published]
jobs:
  publish:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Login to DockerHub
        uses: docker/login-action@v2
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}
      - name: Build and publish
        uses: home-assistant/builder@2022.11.0
        with:
          args: |
            --aarch64             --target /data/addon-name
```

## Repository Management

### Repository Structure
```
repository/
â”œâ”€â”€ addon1/
â”‚   â”œâ”€â”€ config.yaml
â”‚   â”œâ”€â”€ Dockerfile
â”‚   â””â”€â”€ ...
â”œâ”€â”€ addon2/
â”‚   â”œâ”€â”€ config.yaml
â”‚   â”œâ”€â”€ Dockerfile
â”‚   â””â”€â”€ ...
â””â”€â”€ repository.yaml
```

### Repository Configuration (`repository.yaml`)
```yaml
name: "Your Add-on Repository"
url: "https://github.com/username/addon-repo"
maintainer: "Your Name <email@example.com>"
```

### Installation by Users
1. Navigate to Supervisor â†’ Add-on Store
2. Click three dots (top right) â†’ Repositories
3. Add repository URL
4. Click "Save"

**Pro Tip**: Generate a `my.home-assistant.io` link for one-click installation.

## Communication & Integration

### Home Assistant Integration
- **Services API**: Access to Home Assistant services
- **Auth API**: User authentication integration
- **Discovery**: Auto-discovery of add-on services
- **Ingress**: Seamless web UI integration

### Network Communication
- **Host Network**: Direct access to host networking
- **Internal Network**: Isolated container networking
- **Port Mapping**: Expose specific ports

### Configuration Access
```bash
# Read add-on options
OPTIONS_FILE="/data/options.json"
USERNAME=$(jq --raw-output '.username // empty' $OPTIONS_FILE)
```

## Security Considerations

### Add-on Rating System
**Positive Ratings**:
- Using `ingress: true` (+2, overrides auth_api)
- Using `auth_api: true` (+1)
- CodeNotary signing (+1)
- Custom AppArmor profile (+1)

**Negative Ratings**:
- Privileged capabilities (-1)
- Host network access (-1)
- Manager role (-1)
- Admin role (-2)
- Host PID namespace (-2)

### Best Practices
- Use ingress for web interfaces
- Minimize privileged access
- Implement proper authentication
- Follow principle of least privilege
- Use AppArmor profiles when possible

## Testing Strategy

### Local Testing Workflow
1. **Development**: Use VS Code devcontainer
2. **Build Testing**: Local Docker builds
3. **Integration Testing**: Test with real Home Assistant
4. **User Testing**: Deploy to test users via repository
5. **Production**: Push to container registry

### Debugging
- **Logs**: `docker logs addon_name`
- **Shell Access**: `docker exec -it addon_name /bin/bash`
- **Configuration**: Check `/data/options.json`
- **Supervisor API**: Use for advanced debugging

## Rust + React Specific Considerations

### Multi-stage Docker Build
```dockerfile
# Build stage
FROM rust:1.70 as builder
COPY backend/ /backend
WORKDIR /backend
RUN cargo build --release

FROM node:18 as frontend-builder
COPY frontend/ /frontend
WORKDIR /frontend
RUN npm install && npm run build

# Runtime stage
ARG BUILD_FROM
FROM $BUILD_FROM

# Copy binaries
COPY --from=builder /backend/target/release/app /usr/local/bin/
COPY --from=frontend-builder /frontend/dist/ /app/static/

# Copy startup script
COPY run.sh /
RUN chmod a+x /run.sh

EXPOSE 8080
CMD ["/run.sh"]
```

### Configuration Integration
```bash
#!/usr/bin/with-contenv bashio

# Read configuration
CONFIG_PATH="/data/options.json"
API_KEY=$(bashio::config 'api_key')
PORT=$(bashio::config 'port' 8080)

# Start application
exec /usr/local/bin/app   --api-key="$API_KEY"   --port="$PORT"   --static-dir="/app/static"
```

### Ingress Setup for React Apps
```yaml
# config.yaml
ingress: true
ingress_port: 8080
ingress_entry: "/"
panel_icon: "mdi:web"
webui: "http://[HOST]:[PORT:8080]"
```

## Migration Checklist

### Pre-Migration
- [ ] Identify application dependencies
- [ ] Document configuration options
- [ ] Plan data persistence strategy
- [ ] Design user configuration schema

### Implementation
- [ ] Create basic add-on structure
- [ ] Write Dockerfile with multi-stage build
- [ ] Configure ingress and networking
- [ ] Implement configuration handling
- [ ] Add proper logging and error handling

### Testing
- [ ] Test local builds
- [ ] Verify configuration options
- [ ] Test ingress integration
- [ ] Validate multi-architecture support
- [ ] Perform user acceptance testing

### Publishing
- [ ] Set up container registry
- [ ] Configure automated builds
- [ ] Create repository structure
- [ ] Write comprehensive documentation
- [ ] Prepare for community feedback

## Resources & References

- **Official Documentation**: https://developers.home-assistant.io/docs/add-ons/
- **Example Repository**: https://github.com/home-assistant/addons-example
- **Builder Tool**: https://github.com/home-assistant/builder
- **Community Examples**: https://github.com/hassio-addons/
- **Devcontainer Template**: Multiple available on GitHub

## Conclusion

Converting a Rust+React fullstack application to a Home Assistant add-on requires careful consideration of containerization, configuration management, and Home Assistant integration patterns. The ingress system provides seamless web UI integration, while the Supervisor handles lifecycle management. Focus on using pre-built containers for production deployments and leverage the VS Code devcontainer for efficient development workflows.
