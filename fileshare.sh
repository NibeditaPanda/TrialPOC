#!/bin/bash

# Downloads files from the fileshare server and triggers imports.
# It is safe to run this script as often as you like:
# - It will abort right away if an import is already in progress (therefore 
#   not overwriting files which are currently being imported)
# - It won't trigger an import if no new files are available or if the new
#   files are identical to the last files that were imported.

SRC=/var/lib/jenkins/workspace/PriceService/fileshare/appl/priceservice/latest
TEMP=/appl/priceservice/latest
SERVICES=http://188.226.184.77:8080

usage() {
  echo "Usage: fileShare.sh server dest importTarget"
  echo ""
  echo " server - the fileshare server in user@host format"
  echo " dest - where to put the files on this box (where the services expect to find them)"
  echo " importTarget - 'rpm'"
}

doScp() {
  local from=$1
  local to=$2
  scp -o StrictHostKeyChecking=no $SERVER:$from $to
}


copyFile() {
  from=$1
  to=$2

  echo "Getting latest file matching $SERVER:$SRC/$from and copying to $DEST/$to"

  latest=$(ssh -o StrictHostKeyChecking=no $SERVER "ls -1 $SRC/$from" | tail -n1)

  if [ -z "$latest" ]; then
    echo "No file matching $from found on server"
    exit 1
  fi

  doScp $latest $TEMP/$to

  if [ "$?" -ne "0" ]; then
    echo "Failed to scp $latest to $TEMP/$to"
    exit 1
  fi

  mkdir -p $DEST
  if [[ "$from" == *gz ]];
  then
    cat $TEMP/$to | gzip -d > $DEST/$to
  else
    cp $TEMP/$to $DEST/$to
  fi
}

trigger() {
  target=$1

  numFiles=${#srcFiles[@]} 

  echo "Triggering $target ($numFiles files)"

  for ((i = 0; i < $numFiles; i++))
  do
    from="${srcFiles[$i]}"
    to="${destFiles[$i]}"
    copyFile $from $to
  done

  echo "Triggering $target import"
  status=$(curl -X POST -s -o /dev/null -w "%{http_code}" $SERVICES/admin/import)

  if [ "$status" -ne "200" ];
  then
    echo "Failed with http code $status - aborting"
    exit 1
  fi

}

doRpm() {
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

  trigger "rpm"
}

if [ "$#" -ne 3 ]; then
  usage
  exit 1
fi

SERVER=$1
DEST=$2

case "$3" in
  rpm) doRpm ;;
  *) usage && exit 1 ;;
esac
