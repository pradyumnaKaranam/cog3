#!/bin/bash
SCRIPT_DIR=$(dirname $0)
QPROCESSOR_DIR="$SCRIPT_DIR/.."
INSTANCE_ID=$1

[ -z "$INSTANCE_ID" ] && echo "Usage: $0 INSTANCE_ID" >&2 && exit 101

PID_FILE="$SCRIPT_DIR/$INSTANCE_ID.pid"

if [ -f "$PID_FILE" ]; then
	QUEUE_PID=$(cat "$PID_FILE")
	if [ "$QUEUE_PID" -gt 1 ]; then
	  if ps -p $QUEUE_PID > /dev/null; then
			echo "Queue Processor $INSTANCE_ID running with PID $QUEUE_PID."
			exit 0
		else
			echo "Queue Processor $INSTANCE_ID not running."
			exit 110
		fi
	else
		echo "Invalid PID for Queue Processor $INSTANCE_ID"
		exit 103
	fi
else
	echo "No PID file for Queue Processor $INSTANCE_ID. Probably not running."
	exit 102
fi

