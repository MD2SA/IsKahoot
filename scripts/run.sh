#!/usr/bin/env bash
#
# Builds the project and runs either the server or a client.
#
# Usage:
#   ./scripts/run.sh server
#   ./scripts/run.sh client <username> <team-id> <game-code>
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT_DIR"

if [[ $# -lt 1 ]]; then
    echo "Usage: $0 <server|client> [arguments...]" >&2
    exit 1
fi

TARGET="$1"
shift

case "$TARGET" in
    server) MAIN_CLASS="server.Main" ;;
    client) MAIN_CLASS="client.Main" ;;
    *)
        echo "Unknown target '$TARGET'. Use 'server' or 'client'." >&2
        exit 1
        ;;
esac

CLASSPATH_FILE="target/classpath.txt"

if [[ "${ISKAHOOT_SKIP_BUILD:-0}" != "1" ]]; then
    ./mvnw -q -DskipTests package
fi

if [[ ! -f "$CLASSPATH_FILE" || "pom.xml" -nt "$CLASSPATH_FILE" ]]; then
    ./mvnw -q dependency:build-classpath -Dmdep.outputFile="$CLASSPATH_FILE"
fi

exec java -cp "target/classes:$(< "$CLASSPATH_FILE")" "$MAIN_CLASS" "$@"
