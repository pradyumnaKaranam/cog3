#!/bin/bash
SCRIPT_DIR=$(dirname $0)
echo $SCRIPT_DIR
QPROCESSOR_DIR=$(greadlink -f "$SCRIPT_DIR/..")
echo $QPROCESSOR_DIR
INSTANCE_ID=$1

[ -z "$INSTANCE_ID" ] && echo "Usage: $0 INSTANCE_ID" >&2 && exit 101

set -e

if "$SCRIPT_DIR/status.sh" "$INSTANCE_ID" > /dev/null
	then
		echo "Queue Processor $INSTANCE_ID already running."
		exit 0
fi

echo "Starting Queue Processor $INSTANCE_ID...";
		
PID_FILE="$SCRIPT_DIR/$INSTANCE_ID.pid"

nohup java -DCOGASSIST_HOME="$QPROCESSOR_DIR/home" -Dsolr.solr.home="$QPROCESSOR_DIR/../digdeep/solr" -Xmx3600M -cp "$QPROCESSOR_DIR/lib/*" com.ibm.research.cogassist.qprocessor.CAQueueProcessor "$INSTANCE_ID" "$QPROCESSOR_DIR/plugins" "$QPROCESSOR_DIR/logs" 1>> "$QPROCESSOR_DIR/logs/queue.log" 2>&1 &

echo $! > "$PID_FILE"

echo "Queue Processor $INSTANCE_ID started."
