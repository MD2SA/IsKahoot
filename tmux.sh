#!/bin/bash

# set session name
SESSION="kahoot"

# create detached session
tmux new-session -d -s $SESSION -n code

# window 0: nvim
tmux send-keys -t $SESSION:0 "nvim ." C-m

# window 1: server cmd
tmux new-window -t $SESSION -n server

# window 2: client cmd
tmux new-window -t $SESSION -n client

# focus on code
tmux select-window -t $SESSION:code

# attach session after setup
tmux attach-session -t $SESSION

