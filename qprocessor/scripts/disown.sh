#!/bin/bash
SCRIPT_DIR=$(dirname $0)
QPROCESSOR_DIR="$SCRIPT_DIR/.."
INSTANCE_ID=$1
QPROCESSOR_MYSQL_HOSTNAME=localhost
QPROCESSOR_MYSQL_DATABASE=digdeep
QPROCESSOR_MYSQL_USERNAME=digdeep
QPROCESSOR_MYSQL_PASSWORD=digdeep

[ -z "$INSTANCE_ID" ] && echo "Usage: $0 INSTANCE_ID" >&2 && exit 101

echo "UPDATE catasks SET status='QUEUED' and processor_id = null where status='RUNNING' and processor_id = '$INSTANCE_ID';" | mysql $QPROCESSOR_MYSQL_DATABASE -h $QPROCESSOR_MYSQL_HOSTNAME -u $QPROCESSOR_MYSQL_USERNAME --password=$QPROCESSOR_MYSQL_PASSWORD && echo "All tasks for Queue Processor $INSTANCE_ID have been disowned.";
