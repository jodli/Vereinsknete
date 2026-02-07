#!/usr/bin/env bash
#
# Push seed data to an Android device/emulator so it can be imported
# via the app's Datensicherung (backup) screen.
#
# Usage:
#   ./scripts/seed-db.sh                  # default device
#   ./scripts/seed-db.sh --device SERIAL  # specific device
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SEED_FILE="$SCRIPT_DIR/seed-data.json"
PACKAGE="de.yogaknete.app"
DEST="/sdcard/Download/yogaknete_seed.json"

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

if [[ ! -f "$SEED_FILE" ]]; then
  echo "Error: Seed file not found: $SEED_FILE" >&2
  exit 1
fi

echo "==> Force-stopping $PACKAGE..."
"${ADB[@]}" shell am force-stop "$PACKAGE"

echo "==> Pushing seed data to $DEST..."
"${ADB[@]}" push "$SEED_FILE" "$DEST"

echo ""
echo "Done! Now import in the app:"
echo "  1. Open the app"
echo "  2. Go to Datensicherung (backup screen)"
echo "  3. Tap 'Datei auswaehlen', pick 'yogaknete_seed.json' from Downloads"
echo "  4. Select 'Ersetzen' and tap 'Importieren'"
echo ""
