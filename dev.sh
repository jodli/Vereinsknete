#!/bin/bash

# Set colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if tmux is installed
if ! command -v tmux &> /dev/null; then
    echo -e "${RED}tmux is required but not installed. Please install tmux first.${NC}"
    exit 1
fi

# Create a new tmux session
SESSION_NAME="vereinsknete-dev"

# Kill existing session if it exists
tmux kill-session -t $SESSION_NAME 2>/dev/null

# Create new session
echo -e "${GREEN}Starting development environment...${NC}"
tmux new-session -d -s $SESSION_NAME

# Set up backend pane
tmux rename-window -t $SESSION_NAME "development"
tmux send-keys -t $SESSION_NAME "cd $(pwd)/backend" C-m
tmux send-keys -t $SESSION_NAME "echo -e \"${YELLOW}Starting backend server...${NC}\"" C-m
tmux send-keys -t $SESSION_NAME "cargo run" C-m

# Split the window for frontend
tmux split-window -h -t $SESSION_NAME
tmux send-keys -t $SESSION_NAME "cd $(pwd)/frontend" C-m
tmux send-keys -t $SESSION_NAME "echo -e \"${YELLOW}Starting frontend development server...${NC}\"" C-m
tmux send-keys -t $SESSION_NAME "npm start" C-m

# Attach to the session
tmux attach-session -t $SESSION_NAME

echo -e "${GREEN}Development environment stopped.${NC}"
