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
echo "Triggering Import operation" >> $LOG_FILE

log_temp=$LOG_PATH/log_temp


status=$(curl -X POST -s -o $log_temp -w "%{http_code}" http://$SERVER_IP:$PORT_SERVICE/admin/import)


if [ "$status" == "000" ];
	then
	echo "$(date '+%Y-%m-%d %T') : Import failed due to connectivity error,check error log" >> $LOG_FILE
	echo "$(date '+%Y-%m-%d %T') : curl exited with status code : $status" >> $ERR_FILE
	echo "the status is" $status
	echo "Job $PROG_NAME failed..!!"
	exit 1
fi

curl_response=$(cat $log_temp)

echo "$(date '+%Y-%m-%d %T') : Response from curl is: >" $curl_response >> $LOG_FILE

echo "the status is" $status

if [ "$status" == "200" ];
	then
	echo "$(date '+%Y-%m-%d %T') : Import started successfully" >> $LOG_FILE

elif [ "$status" == "500" ];
	  then
	    echo "$(date '+%Y-%m-%d %T') : Import activity is aborted -Internal server error" >> $LOG_FILE
	    echo "$(date '+%Y-%m-%d %T') : ERROR :exited with status :$status " >> $ERR_FILE
	    echo "Job $PROG_NAME failed..!!"
	    exit 1
else
	echo "$(date '+%Y-%m-%d %T') : Import activity failed-exited with status:$status " >>  $ERR_FILE
	echo "Job $PROG_NAME failed..!!"
	exit 1
fi
#***********************************polling mechanism for import in progress check**********************************#
while [[ true ]]; do

	#sleep period
	sleep $SLEEP_PERIOD
	#REST call to check if import in progress or not
	status=$(curl -X GET  -s -o $log_temp -w "%{http_code}" http://$SERVER_IP:$PORT_SERVICE/admin/importInProgress)

	if [ "$status" == "000" ];
	then
		echo "$(date '+%Y-%m-%d %T') : Import progress check failed check error file" >> $LOG_FILE
		echo "$(date '+%Y-%m-%d %T') : curl exited with status code : $status" >> $ERR_FILE
		echo "the status is" $status
		echo "Job $PROG_NAME failed..!!"
		exit 1

	elif [ "$status" == "200" ]; 
      then
		output=$(cat $log_temp)

	    #check if import is in progress
		# echo $output | grep  "import.*:.*progress" 

		ret=$(echo $output | grep -c "import.*:.*progress" )

		#if in progress sleep 60 and the check again
		#if [[ "$?" -ne "0" ]]; then
		if [[ "$ret" -eq "0" ]]; 
            then
			#echo $output | grep "import.*:.*aborted"
             ret=$(echo $output | grep -c "import.*:.*aborted")

			#if [[ "$?" == "0" ]]; then
			if [[ "$ret" -ne "0" ]]; 
                  then
				#import has errored,log and exit 1
				echo "$(date '+%Y-%m-%d %T') : Import activity is aborted ,check script error log and adapter error logs for more info" >> $LOG_FILE
				echo "$(date '+%Y-%m-%d %T') : ERROR from adapter is $output" >> $ERR_FILE
				echo "Job $PROG_NAME failed..!!"
				exit 1
			else
				# Import is successful!!!
				echo "$(date '+%Y-%m-%d %T') : Import Completed successfully" >> $LOG_FILE
                        echo "$(date '+%Y-%m-%d %T') : Output from restcall is >$output<" >> $LOG_FILE
				break
			fi
		
		else
			echo "Import still in progress"
		fi
	
	else
	      echo "$(date '+%Y-%m-%d %T') : Import progress check failed check error file" >> $LOG_FILE
       	  echo "$(date '+%Y-%m-%d %T') : Import activity failed-exited with status:$status " >>  $ERR_FILE
       	  echo "$(date '+%Y-%m-%d %T') : Import activity failed-exited with status:$status "
	      echo "Job $PROG_NAME failed..!!"
      	  exit 1
	fi
done

echo "Job $PROG_NAME completed successfully"
echo "$(date '+%Y-%m-%d %T') : Job $PROG_NAME Completed Successfully" >> $LOG_FILE

exit $?