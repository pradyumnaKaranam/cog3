#!/bin/bash
QPROCESSOR_MYSQL_HOSTNAME=localhost
QPROCESSOR_MYSQL_DATABASE=digdeep
QPROCESSOR_MYSQL_USERNAME=digdeep
QPROCESSOR_MYSQL_PASSWORD=digdeep

if [ $# -ne 1 ]; then
	echo "Usage: $0 TASK_ID"
fi

TASK_ID=$1

echo "update catasks set status = 'QUEUED', time_started = NULL, time_completed = NULL, last_update_time = NOW() where id = '$TASK_ID'" | mysql $QPROCESSOR_MYSQL_DATABASE --host=$QPROCESSOR_MYSQL_HOSTNAME --user=$QPROCESSOR_MYSQL_USERNAME --password=$QPROCESSOR_MYSQL_PASSWORD
