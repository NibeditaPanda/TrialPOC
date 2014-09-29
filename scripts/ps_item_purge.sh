#!/bin/bash

. $PSHOME/profile

RC=$?
if [[ $RC -ne 0 ]] ; then
  # Error exit
  echo "$(date '+%y/%m/%d %T') : Return code: ${RC} : ERROR : $0 failed calling profile script for variable setup"
  exit 1
fi

. $PSHOME/usr/local/scripts/config.sh

RC=$?
if [[ $RC -ne 0 ]] ; then
  # Error exit
  echo "$(date '+%y/%m/%d %T') : Return code: ${RC} : ERROR : $0 failed calling config script for variable setup"
  exit 1
fi

STAMP=`date +"%Y%m%d"`
PROG_NAME=$(basename $0 .sh)
USER="$(id -u -n)"

LOG_FILE=$LOG_PATH/${PROG_NAME}_log_${STAMP}.log
ERR_FILE=$ERROR_PATH/${PROG_NAME}_err_${STAMP}.log

if [[ -z "$LOG_PATH" ]]; 
then
      echo "Log path is not set. Please set the LOG_PATH."
      exit 1
fi

if [[ -z "$ERROR_PATH" ]];  
then
      echo "Error path is not set. Please set the ERROR_PATH."
      exit 1
fi

echo "Job $PROG_NAME started by $USER"
echo "$(date '+%Y-%m-%d %T') : Job $PROG_NAME started by $USER" >> $LOG_FILE
echo "Triggering Purge operation" >> $LOG_FILE

status=$(curl -X POST -s -o $log_temp -w "%{http_code}"  http://$UP@$SERVER_IP:$PORT_SERVICE/itempurge/purge )


if [ "$status" == "000" ];
	then
	echo "$(date '+%Y-%m-%d %T') : Purge failed due to connectivity error,check error log" >> $LOG_FILE
	echo "$(date '+%Y-%m-%d %T') : curl exited with status code : $status" >> $ERR_FILE
	echo "the status is" $status
	echo "Job $PROG_NAME failed..!!"
	exit 1
fi

curl_response=$(cat $log_temp)

echo "$(date '+%Y-%m-%d %T') : Response from curl is: " $curl_response >> $LOG_FILE

echo "the status is" $status

if [ "$status" == "200" ];
	then
	echo "$(date '+%Y-%m-%d %T') : Purge completed successfully" >> $LOG_FILE

elif [ "$status" == "500" ];
	  then
	    echo "$(date '+%Y-%m-%d %T') : Purge activity is aborted -Internal server error" >> $LOG_FILE
	    echo "$(date '+%Y-%m-%d %T') : ERROR :exited with status :$status " >> $ERR_FILE
	    echo "Job $PROG_NAME failed..!!"
	    exit 1
else
	echo "$(date '+%Y-%m-%d %T') : Purge activity failed-exited with status:$status " >>  $ERR_FILE
	echo "Job $PROG_NAME failed..!!"
	exit 1
fi

echo "Job $PROG_NAME completed successfully"
echo "Job $PROG_NAME completed successfully" >> $LOG_FILE


 echo "done"