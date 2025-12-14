#!/usr/bin/env bash

if [ -z "$1" ]; then
    echo -n "Insert game id: "
    read GAME;
    GAME="${GAME:-0}"
else
    GAME="$1"
fi

./sim.sh 2 2 $GAME


# jrc -r client.Main test0 0 $GAME >/dev/null 2>&1 &
# PID0=$!
# jrc -r client.Main test1 0 $GAME >/dev/null 2>&1 &
# PID1=$!
# jrc -r client.Main test2 1 $GAME >/dev/null 2>&1 &
# PID2=$!
# jrc -r client.Main test3 1 $GAME >/dev/null 2>&1 &
# PID3=$!
#
# echo -n "Press anything to kill all processes"
# read
#
# kill $PID0 $PID1 $PID2 $PID3 >/dev/null 2>&1
