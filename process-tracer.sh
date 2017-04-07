#!/bin/bash

DB_NAME="process-tracer.db"
DB_TABLE_NAME="traces"

sqlite3 ${DB_NAME} "CREATE TABLE IF NOT EXISTS ${DB_TABLE_NAME} (id INTEGER PRIMARY KEY AUTOINCREMENT, process_correlation_id TEXT, timestamp INTEGER, rss_mem_kb INTEGER);"

while :
do
	TIMESTAMP=$(date +%s)
	PIDS_RESULT=$(ps -ef | grep -v grep | grep "processCorrelationId" | awk '{print $2}')

	# transform the ps-result into an array
	IFS=', ' read -r -a PIDS <<< $PIDS_RESULT

	if [[ ! "$PIDS[0]" ]]
	then
		echo "no processes with processCorrelationId flag found"
	else
		echo "found processes flagged with 'processCorrelationId': ${PIDS[@]}"
		for PID in "${PIDS[@]}"; do
			echo "getting details for process with pid '${PID}'".
			IFS=', ' read -r -a PARAMS <<< $(ps -eo pid,cmd | grep $PID)
			# get the process correlation id from params
			for PARAM in "${PARAMS[@]}"; do
				if [[ $PARAM == *processCorrelationId* ]];
				then
					PROCESS_CORRELATION_ID=$(echo $PARAM | cut -d '=' -f2)
				fi
			done
			RSS_MEM_KB=$(cat /proc/$PID/status | grep VmRSS | cut -d ':' -f2 | xargs | cut -d ' ' -f1)
			# echo "current RSS of process ${PID} is ${RSS_MEM_KB}kb"
 			# echo "${TIMESTAMP},${RSS_MEM_KB}" >> ./rss_mem_kb_for_$CORR_ID
 			sqlite3 ${DB_NAME} "INSERT INTO ${DB_TABLE_NAME} (process_correlation_id, timestamp, rss_mem_kb) values ('${PROCESS_CORRELATION_ID}', ${TIMESTAMP}, ${RSS_MEM_KB});"
		done
	fi
	sleep 2
done


