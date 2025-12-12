#!/usr/bin/env bash

if [ -z "$1" ]; then
    echo -n "Number of teams: "
    read TEAMS
    TEAMS="${TEAMS:-0}"
else
    TEAMS="$1"
fi

if [ -z "$2" ]; then
    echo -n "Size of each team: "
    read TEAM_SIZE
    TEAM_SIZE="${TEAM_SIZE:-1}"
else
    TEAM_SIZE="$2"
fi

if [ -z "$3" ]; then
    echo -n "Insert GAME id: "
    read GAME
    GAME="${GAME:-2}"
else
    GAME="$3"
fi

# Validar inteiros
if ! [[ "$TEAMS" =~ ^[0-9]+$ ]] || ! [[ "$TEAM_SIZE" =~ ^[0-9]+$ ]]; then
    echo "TEAMS e TEAM_SIZE devem ser inteiros."
    exit 1
fi

if (( TEAMS <= 0 || TEAM_SIZE <= 0 )); then
    echo "TEAMS e TEAM_SIZE devem ser > 0."
    exit 1
fi

TOTAL=$(( TEAMS * TEAM_SIZE ))

echo "Launching $TOTAL users in background..."

PIDS=()

for (( i=0; i<TOTAL; i++ )); do
    USER="test$i"
    TEAM=$(( i / TEAM_SIZE ))

    jrc -r client.Main "$USER" "$TEAM" "$GAME" > "log.tmp" 2>&1 &
    PIDS+=($!)
done

rm log.tmp

echo "All processes started."

echo -n "Press anything to kill all processes"
read

kill "${PIDS[@]}"

