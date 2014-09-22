#!/bin/bash

# Downloads files from the fileshare server and copy it into dev server
set -o pipefail
. ./config.sh

RC=$?
if [[ $RC -ne 0 ]] ; then
  # Error exit
  echo "$(date '+%y/%m/%d %T') : Return code: ${RC} : ERROR : $0 failed calling config script for variable setup"
  exit 1
fi

STAMP=`date +"%Y%m%d"`
PROG_NAME=$(basename $0 .sh)

LOG_FILE=$LOG_PATH/${PROG_NAME}_log_${STAMP}.log
ERR_FILE=$ERROR_PATH/${PROG_NAME}_err_${STAMP}.log

SSH_ERR=$SSH_TMP/${PROG_NAME}_err_${STAMP}

USER="$(id -u -n)"

usage() {
  echo "Usage: fileShare.sh server dest"
  echo ""
  echo "server - the fileshare server in user@host format"
  echo "dest - where to put the files on this box (where the services expect to find them)"
}

if [ "$#" -ne 2 ]; then
  usage
  exit 1 
fi

if [ -z "$LOG_PATH" ]; 
then
      echo "Log path is not set. Please set the LOG_PATH"
      exit 1
fi

if [ -z "$ERROR_PATH" ]; 
then
      echo "Error path is not set. Please set the ERROR_PATH"
      exit 1
fi

echo "Job $PROG_NAME started by $USER"
echo "$(date '+%Y-%m-%d %T') : Job $PROG_NAME started by $USER" >> $LOG_FILE

SERVER=$1
DEST=$2

getfilenames() {

  srcFiles=(
    "tsl_rpm_price_srvc_price_zone_*.csv.gz" 
    "tsl_rpm_price_srvc_prom_zone_*.csv.gz"
    "tsl_rpm_price_srvc_store_zone_*.csv.gz"
    "tsl_rpm_price_srvc_prom_extract_*.csv.gz"
    "CH_offers_*.csv.gz"
  )

  destFiles=(
    "PRICE_ZONE.csv"
    "PROM_ZONE.csv"
    "STORE_ZONE.csv"
    "PROM_EXTRACT.csv"
    "PROM_DESC_EXTRACT_full_dump.csv"
  )
  
  numFiles=${#srcFiles[@]}

}

checkServer() {

  echo "$(date '+%Y-%m-%d %T') : Begin : Checking for the required files in the server" >> $LOG_FILE

  for ((i = 0; i < $numFiles; i++))
  do

    from="${srcFiles[$i]}"
    
    exec 6>&1
    exec 1> SSH_ERR
    exec 2>&1
    
    # src_time_stamp contains file name and the time stamp
    src_time_stamp=$(ssh -o StrictHostKeyChecking=no $SERVER "ls -l $SRC/$from | tr -s ' ' | cut -d' ' -f6-9 " | tail -n1)
	  RC=$?

	  exec 1>&6 6>&-

	  sshout=$(cat SSH_ERR)
	
    if [[ -z "$src_time_stamp" || ! -z $sshout ]];
    then
      echo "$(date '+%Y-%m-%d %T') : Exiting the $PROG_NAME. Check the error log in $ERR_FILE" >> $LOG_FILE
      echo "$(date '+%y/%m/%d %T') : ERROR : Failed to ssh !!" >> $ERR_FILE
	    echo "$(date '+%y/%m/%d %T') : Return code: ${RC} : ERROR Occured is : >$sshout< !!" >> $ERR_FILE
      exit 1
    fi
    
    # cut out the time stamp
    time_stamp=$( echo "$src_time_stamp" | cut -d ' ' -f1-3 )
    # cut out the file name
    src_filename[i]=$( echo "$src_time_stamp" | cut -d ' ' -f4 )

    if [ -z "$src_filename[i]" ]; 
    then
      echo "$(date '+%Y-%m-%d %T') : Exiting the $PROG_NAME. Check the error log in $ERR_FILE" >> $LOG_FILE
      echo "$(date '+%y/%m/%d %T') : ERROR : No file matching $from found on server" >> $ERR_FILE
      exit 1
    fi

    src_date=$(date -d "$time_stamp" +%s)
    curr_date=`date +%s`

    if [ "$(((curr_date-src_date)/3600))" -gt 24 ];
    then
      echo "$(date '+%Y-%m-%d %T') : Exiting the $PROG_NAME. Check the error log in $ERR_FILE" >> $LOG_FILE
      echo "$(date '+%y/%m/%d %T') : ERROR : The file $from is older than 24 hours" >> $ERR_FILE
      exit 1
    else
      echo "$(date '+%Y-%m-%d %T') : File $from found in $SRC" >> $LOG_FILE
    fi

  done
}

copyFile() {
  
  numFiles=${#src_filename[@]}

  if [ "$numFiles" -ne 5 ]; 
  then
    echo "$(date '+%Y-%m-%d %T') : Exiting the $PROG_NAME. Check the error log in $ERR_FILE" >> $LOG_FILE
    echo "$(date '+%y/%m/%d %T') : ERROR : Required number of files are not found in $SRC" >> $ERR_FILE
    exit 1
  fi

  for ((i = 0; i < $numFiles; i++))
  do

    from="${src_filename[$i]}"
    to="${destFiles[$i]}"
    
    echo "$(date '+%Y-%m-%d %T') : Getting latest file matching $SERVER:$from and copying to $DEST/$to" >> $LOG_FILE
    
    doScp $from $TEMP/$to
    
    mkdir -p $DEST
    if [[ "$from" == *gz ]];
    then
      cat $TEMP/$to | gzip -d > $DEST/$to
    else
      cp $TEMP/$to $DEST/$to
    fi
    
  done

  #Delete the files in temporary folder
  rm $TEMP/*

}

doScp() {
  local from=$1
  local to=$2
  scp -o StrictHostKeyChecking=no $SERVER:$from $to

  RC=$?
  if [ "$RC" -ne "0" ]; 
  then
    echo "$(date '+%Y-%m-%d %T') : Exiting the $PROG_NAME. Check the error log in $ERR_FILE" >> $LOG_FILE
    echo "$(date '+%y/%m/%d %T') : Return code: ${RC} : ERROR : Failed to scp $from to $to" >> $ERR_FILE
    exit 1
  fi
}

archiveFiles() {
  numOfFiles=${#src_filename[@]}

  echo "$(date '+%Y-%m-%d %T') : Archiving $numOfFiles to $ARCHIVE" >> $LOG_FILE

  for ((i = 0; i < $numOfFiles; i++))
  do
    pattern="${src_filename[$i]}"

    if [[ $pattern == *CH_offers* ]];
    then
      append_date=CH_offers_desc_${STAMP}.csv.gz
      ssh -o StrictHostKeyChecking=no $SERVER "mv $pattern $ARCHIVE/$append_date"
    fi

    ssh -o StrictHostKeyChecking=no $SERVER "mv $pattern $ARCHIVE/"
  done
}

getfilenames

checkServer

copyFile

archiveFiles

echo "$(date '+%Y-%m-%d %T') : Job $PROG_NAME completed successfully" >> $LOG_FILE
echo "Job $PROG_NAME completed successfully"

exit $?