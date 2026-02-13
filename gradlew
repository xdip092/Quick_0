#!/usr/bin/env sh
DIR="$(CDPATH= cd -- "$(dirname "$0")" && pwd)"

if command -v pwsh >/dev/null 2>&1; then
  pwsh -NoProfile -ExecutionPolicy Bypass -File "$DIR/gradlew.ps1" "$@"
  exit $?
fi

echo "pwsh is required to run this wrapper script."
exit 1
