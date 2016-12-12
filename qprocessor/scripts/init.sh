#!/bin/bash
SCRIPTS_DIR=$(dirname $0)
QPROCESSOR_DIR=$SCRIPTS_DIR/..
TOMCAT_DIR=$QPROCESSOR_DIR/../tomcat7

[ -z "$1" ] && echo "Usage: $0 NUM_INSTANCES" >&2 && exit 1

for i in $(seq 1 "$1"); do
	$SCRIPTS_DIR/start.sh $i || exit 4
	sleep 1
done

