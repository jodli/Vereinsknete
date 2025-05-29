# GitHub Actions CI/CD Pipeline

This directory contains GitHub Actions workflows for the VereinsKnete project, implementing comprehensive CI/CD practices following industry standards.

## 🔄 Workflows Overview

### 1. **CI (`ci.yml`)**
**Trigger:** Push/PR to `main` or `develop` branches

**Purpose:** Core continuous integration pipeline that validates all code changes.

**Jobs:**
- `test-backend` - Rust backend testing, linting, and building
- `test-frontend` - React frontend testing, linting, and building
- `docker-build-test` - Docker image build verification

**Features:**
- ✅ Rust formatting (`cargo fmt --check`) and linting (`cargo clippy`)
- ✅ Backend tests with `cargo test` and release builds
- ✅ Frontend tests with coverage (runs with `--passWithNoTests`)
- ✅ Caching for faster builds (Rust dependencies via `rust-cache`, npm packages)
- ✅ ESLint support (runs `npm run lint --if-present`)
- ✅ Docker build verification with GitHub Actions cache
- ✅ System dependencies installation (SQLite dev libraries, Diesel CLI)
- ✅ Node.js 22 setup with npm cache

### 2. **Docker Publish (`docker-publish.yml`)**
**Trigger:** Push to `main`, tags `v*`, releases, or manual dispatch

**Purpose:** Build and publish Docker images to GitHub Container Registry (GHCR).

**Jobs:**
- `build-and-push` - Multi-platform Docker build and push to GHCR

**Features:**
- 🐳 Multi-platform builds (linux/amd64, linux/arm64)
- 🏷️ Intelligent tagging (branch, PR, SHA with branch prefix, semver patterns, latest)
- 🚀 Manual workflow dispatch option with configurable push control
- ⚡ GitHub Actions cache optimization (build cache from/to GHA)
- 🏗️ Build arguments for metadata injection (BUILDTIME, VERSION, REVISION)
- 🔒 Secure image publishing with proper permissions (contents: read, packages: write)

**Image Registry:** `ghcr.io/[repository]`

**Tagging Strategy:**
- Branch pushes: `branch-name`
- Pull requests: `pr-123`
- SHA-based: `branch-sha123456`
- Semantic versions: `v1.2.3`, `1.2`, `1`
- Latest: applied to default branch (main) only

### 3. **Release (`release.yml`)**
**Trigger:** Git tags matching `v*`

**Purpose:** Automated release management and documentation.

**Jobs:**
- `create-release` - Generate changelog and create GitHub release

**Features:**
- 📋 Automatic changelog generation from git commits between tags
- 🏷️ GitHub release creation with detailed notes and Docker usage instructions
- � Release notes with Docker run commands
- 🔖 Support for prerelease detection (versions with `-`)
- 📜 Full git history fetch for accurate changelog generation
- 🐳 Automatic Docker image reference in release notes

## 🔧 Setup Requirements

### 1. Repository Settings

Enable the following repository settings:

- **Actions permissions:** Allow all actions and reusable workflows
- **Workflow permissions:** Read and write permissions
- **Allow GitHub Actions to create and approve pull requests**

### 2. Required Secrets

The workflows use built-in `GITHUB_TOKEN` which is automatically provided. No additional secrets are required.

### 3. GitHub Container Registry

Images are published to GitHub Container Registry (GHCR) automatically. To use published images:

```bash
# Pull the latest image
docker pull ghcr.io/[repository]:latest

# Or use a specific version
docker pull ghcr.io/[repository]:v1.0.0
```

## 🚀 Usage Examples

### Running with Published Docker Image

```bash
# Using docker run
docker run -d \
  --name vereinsknete \
  -p 8080:8080 \
  -v ./data:/app/data \
  -v ./data/invoices:/app/invoices \
  ghcr.io/[repository]:latest

# Using a specific version
docker run -d \
  --name vereinsknete \
  -p 8080:8080 \
  -v ./data:/app/data \
  -v ./data/invoices:/app/invoices \
  ghcr.io/[repository]:v1.0.0
```

### Development Workflow

```bash
# 1. Make changes to your code
# 2. Push to develop branch - triggers CI workflow
git push origin develop

# 3. Create pull request to main - triggers CI workflow
# 4. Merge to main - triggers CI and Docker publish workflows

# 5. Create a release tag - triggers release workflow
git tag v1.0.0
git push origin v1.0.0

# 6. Manually trigger Docker publish (if needed)
gh workflow run docker-publish.yml
```

### Manual Workflow Triggers

```bash
# Trigger Docker build and publish manually
gh workflow run docker-publish.yml

# Create a release (triggers release workflow)
git tag v1.0.0
git push origin v1.0.0
```

## 📊 Monitoring and Insights

### Actions Tab
- Monitor workflow runs and success rates
- View detailed logs for debugging
- Track build performance and duration

### Releases Section
- View all releases with automatically generated changelogs
- Access Docker image information and usage examples

### Packages Section
- View published Docker images in GitHub Container Registry
- Monitor image download statistics
- Manage image retention policies

## 🛠️ Customization

### Adding Deployment Targets

The current workflow focuses on building and publishing Docker images. To add deployment logic, you can extend the `docker-publish.yml` workflow:

```yaml
deploy-staging:
  name: Deploy to Staging
  runs-on: ubuntu-latest
  needs: build-and-push
  if: github.ref == 'refs/heads/main'
  environment: staging

  steps:
  - name: Deploy to staging
    run: |
      # Add your deployment logic here
      # Examples:
      # - Update Kubernetes manifests
      # - Trigger deployment webhook
      # - Update environment variables
      # - Run database migrations

deploy-production:
  name: Deploy to Production
  runs-on: ubuntu-latest
  needs: build-and-push
  if: github.event_name == 'release'
  environment: production

  steps:
  - name: Deploy to production
    run: |
      # Add your production deployment logic here
```

### Adding Environment-Specific Configurations

Use GitHub Environments to add:
- Environment-specific secrets
- Deployment protection rules
- Required reviewers for production deployments

### Adding More Quality Checks

You can create additional workflows for code quality:

```yaml
# Example: .github/workflows/code-quality.yml
name: Code Quality
on: [push, pull_request]
jobs:
  eslint:
    # ESLint, Prettier, TypeScript checks
  rust-audit:
    # cargo audit, cargo deny
```

## 🏷️ Best Practices Implemented

- **Container Publishing:** Automated Docker image publishing to GitHub Container Registry
- **Caching:** Intelligent caching for faster builds (Rust dependencies, npm packages, Docker layers)
- **Multi-platform:** Docker images for multiple architectures (amd64, arm64)
- **Semantic Versioning:** Automated tagging and release management
- **Quality Gates:** Code quality checks (formatting, linting) before merging
- **Automation:** Minimal manual intervention required
- **Observability:** Comprehensive logging and metadata injection
- **Security:** Proper permissions and secure image publishing

## 🆘 Troubleshooting

### Common Issues

1. **Docker build fails:** Check if base images are up to date and system dependencies are available
2. **Rust compilation errors:** Ensure Diesel CLI installation and SQLite dependencies are correct
3. **Frontend test failures:** Verify Node.js version compatibility and npm dependencies
4. **Permission issues:** Verify repository settings and workflow permissions

### Getting Help

- Check workflow logs in the Actions tab
- Consult GitHub Actions documentation
- Open an issue for project-specific problems
