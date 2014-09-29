#!/bin/bash

SRC=$PSHOME/data/in
ARCHIVE=$PSHOME/data/in_arch
TEMP=$PSHOME/data/files
SSH_TMP=$PSHOME/data/tmp
LOG_PATH=$PSHOME/log
ERROR_PATH=$PSHOME/error

#In days
LOG_RETENTION=1
ERROR_RETENTION=1

#username:password for ps_item_purge.sh
UP=PriceService:tesco

#couchbase server ip
SERVER_IP=172.28.152.211
log_temp=$PSHOME/log

#port on which the view creation REST api is running
PORT_VIEW=8092
PORT_SERVICE=8081

#Design doc for view creation
DESIGN_DOC_NAME=prod_designdoc_item

#sleep time for ps_import.sh script
SLEEP_PERIOD=60


