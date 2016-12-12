#!/bin/bash
SCRIPTS_DIR=$(dirname $0)
QPROCESSOR_MYSQL_HOSTNAME=localhost
QPROCESSOR_MYSQL_DATABASE=digdeep
QPROCESSOR_MYSQL_USERNAME=digdeep
QPROCESSOR_MYSQL_PASSWORD=digdeep

if [ $# -ne 1 ]; then
	echo "Usage: $0 TASK_ID"
fi

TASK_ID=$1

INSTANCE_ID=$(echo "select processor_id from catasks where id = '$TASK_ID'" | mysql $QPROCESSOR_MYSQL_DATABASE --host=$QPROCESSOR_MYSQL_HOSTNAME --user=$QPROCESSOR_MYSQL_USERNAME --password=$QPROCESSOR_MYSQL_PASSWORD -N -A);

if "$SCRIPTS_DIR/stop.sh" "$INSTANCE_ID"; then
	
	echo "update catasks set status = 'ABORTED' where id = '$TASK_ID'" | mysql $QPROCESSOR_MYSQL_DATABASE --host=$QPROCESSOR_MYSQL_HOSTNAME --user=$QPROCESSOR_MYSQL_USERNAME --password=$QPROCESSOR_MYSQL_PASSWORD && echo "Task status set to ABORTED."

	"$SCRIPTS_DIR/start.sh" "$INSTANCE_ID"
fi



