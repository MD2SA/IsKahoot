#!/usr/bin/env bash

echo -n "Insert GAME id: "
read GAME
GAME="${GAME:-0}"

echo -n "Number of teams: "
read TEAMS

echo -n "Size of each team: "
read TEAM_SIZE

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

for (( i=0; i<TOTAL; i++ )); do
    USER="test$i"
    TEAM=$(( i / TEAM_SIZE ))

    # No terminal, só background
    nohup jrc -r client.Main "$USER" "$TEAM" "$GAME" > "log.tmp" 2>&1 &
done

rm log.tmp

echo "All processes started."

echo -n "Press anything to kill all processes"
read
killall java

