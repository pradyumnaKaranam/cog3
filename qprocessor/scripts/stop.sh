#!/bin/bash
SCRIPT_DIR=$(dirname $0)
QPROCESSOR_DIR="$SCRIPT_DIR/.."
INSTANCE_ID=$1

[ -z "$INSTANCE_ID" ] && echo "Usage: $0 INSTANCE_ID" >&2 && exit 101

if "$SCRIPT_DIR/status.sh" "$INSTANCE_ID"
	then
	
		PID_FILE="$SCRIPT_DIR/$INSTANCE_ID.pid"
		QUEUE_PID=$(cat "$PID_FILE")
	
		echo "Stopping Queue Processor $INSTANCE_ID..."
	
		if kill $QUEUE_PID
			then
				echo "Queue Processor $INSTANCE_ID stopped."
				rm -f "$PID_FILE" 2>/dev/null
				exit 0
			else
				echo "Could not stop Queue Processor $INSTANCE_ID."
				exit 103
		fi
	
	else
		exit 0
fi
