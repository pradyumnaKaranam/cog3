#!/bin/bash
SCRIPT_DIR=$(dirname $0)
QPROCESSOR_DIR="$SCRIPT_DIR/.."

for pid_file in $(ls "$SCRIPT_DIR"/*.pid 2> /dev/null); do	
	pid_file_basename=$(basename "$pid_file")
	instance_id=${pid_file_basename%.*}
	if "$SCRIPT_DIR"/stop.sh $instance_id; then
		[ -f "$pid_file" ] && rm -f "$pid_file" && echo "Removed stray PID file for Queue Processor $instance_id."
		"$SCRIPT_DIR/disown.sh" "$instance_id"
	fi
done

