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

ARCHIVE_LOG=$LOG_PATH
ARCHIVE_ERR=$ERROR_PATH

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

purge_archive() {

  local_path=$1
  local_retention=$2

  filename=$(find $local_path -maxdepth 1 -name "*.log*" -type f -mtime +$local_retention)

  if [ -z "$filename" ]; 
  then
    echo "$(date '+%Y-%m-%d %T') : No files to purge!!" >> $LOG_FILE
    echo "No files to purge in $local_path..."
    return 0
  fi

  for i in $filename
  do
    temp=$(basename $i)
    rm $i
    RC=$?
    if [ "$RC" -ne "0" ]; 
    then
      echo "$(date '+%Y-%m-%d %T') : Exiting the $PROG_NAME. Check the error log in $ERR_FILE" >> $LOG_FILE
      echo "$(date '+%y/%m/%d %T') : Return code: ${RC} : ERROR : Failed to purge the file $temp" >> $ERR_FILE
      exit 1
    fi

    echo "$(date '+%Y-%m-%d %T') : File purged : $temp" >> $LOG_FILE
  done

  return 0
}

# Purge the archived logs
purge_archive "$ARCHIVE_LOG" "$LOG_RETENTION"

# Purge the archived error
purge_archive "$ARCHIVE_ERR" "$ERROR_RETENTION"

echo "$(date '+%Y-%m-%d %T') : Job $PROG_NAME completed successfully" >> $LOG_FILE
echo "Job $PROG_NAME completed successfully"

exit $?
