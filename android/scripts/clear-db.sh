#!/usr/bin/env bash
#
# Wipe the app's Room database so the next launch starts from onboarding.
#
# Usage:
#   ./scripts/clear-db.sh                  # default device
#   ./scripts/clear-db.sh --device SERIAL  # specific device
#
set -euo pipefail

PACKAGE="de.yogaknete.app"
DB_NAME="yoga_knete_database"

# Parse args
ADB_DEVICE_FLAG=()
while [[ $# -gt 0 ]]; do
  case "$1" in
    --device|-s)
      ADB_DEVICE_FLAG=(-s "$2")
      shift 2
      ;;
    *)
      echo "Unknown option: $1" >&2
      echo "Usage: $0 [--device SERIAL]" >&2
      exit 1
      ;;
  esac
done

ADB=(adb "${ADB_DEVICE_FLAG[@]}")

if ! command -v adb &>/dev/null; then
  echo "Error: adb not found. Make sure Android SDK platform-tools are on your PATH." >&2
  exit 1
fi

echo "==> Force-stopping $PACKAGE..."
"${ADB[@]}" shell am force-stop "$PACKAGE"

echo "==> Deleting Room database files..."
for suffix in "" "-wal" "-shm" "-journal"; do
  FILE="databases/${DB_NAME}${suffix}"
  "${ADB[@]}" shell run-as "$PACKAGE" rm -f "$FILE" 2>/dev/null && \
    echo "    Deleted $FILE" || true
done

echo ""
echo "Done! Next app launch will start from onboarding."
echo ""
