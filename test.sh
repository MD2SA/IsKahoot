#!/usr/bin/env bash

echo -n "Insert game id: "
read GAME;
GAME="${GAME:-0}"

jrc -r client.Main test0 0 $GAME >/dev/null 2>&1 &
jrc -r client.Main test1 0 $GAME >/dev/null 2>&1 &
jrc -r client.Main test2 1 $GAME >/dev/null 2>&1 &
jrc -r client.Main test3 1 $GAME >/dev/null 2>&1 &

wait
