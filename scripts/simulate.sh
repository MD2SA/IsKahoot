#!/usr/bin/env bash
#
# Launches a configurable number of simulated clients against a running server.
#
# Usage: ./scripts/simulate.sh [teams] [team-size] [game-code]
#        ./scripts/simulate.sh 2 2 ABCD
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT_DIR"

TEAMS="${1:-}"
TEAM_SIZE="${2:-}"
GAME_ID="${3:-}"

if [[ -z "$TEAMS" ]]; then
    read -rp "Number of teams: " TEAMS || true
fi
TEAMS="${TEAMS:-2}"

if [[ -z "$TEAM_SIZE" ]]; then
    read -rp "Size of each team: " TEAM_SIZE || true
fi
TEAM_SIZE="${TEAM_SIZE:-2}"

if [[ -z "$GAME_ID" ]]; then
    read -rp "Game code: " GAME_ID || true
fi
GAME_ID="${GAME_ID:-ABCD}"

if ! [[ "$TEAMS" =~ ^[0-9]+$ && "$TEAM_SIZE" =~ ^[0-9]+$ ]]; then
    echo "Teams and team size must be integers." >&2
    exit 1
fi

if (( TEAMS <= 0 || TEAM_SIZE <= 0 )); then
    echo "Teams and team size must be greater than zero." >&2
    exit 1
fi

TOTAL=$(( TEAMS * TEAM_SIZE ))
LOG_DIR="target/sim-logs"

# Build once and reuse it for every simulated client.
./mvnw -q -DskipTests package

mkdir -p "$LOG_DIR"

PIDS=()
cleanup() {
    for pid in "${PIDS[@]:-}"; do
        kill "$pid" 2>/dev/null || true
    done
}
trap cleanup EXIT INT TERM

echo "Starting $TOTAL clients (${TEAMS} teams of ${TEAM_SIZE}) for game ${GAME_ID}..."
for (( i = 0; i < TOTAL; i++ )); do
    USER="test$i"
    TEAM=$(( i / TEAM_SIZE ))
    ISKAHOOT_SKIP_BUILD=1 ./scripts/run.sh client "$USER" "$TEAM" "$GAME_ID" \
        > "$LOG_DIR/client-$i.log" 2>&1 &
    PIDS+=("$!")
done

echo "Clients are running. Logs are in $LOG_DIR/"
read -rp "Press Enter to stop all clients..." || true
