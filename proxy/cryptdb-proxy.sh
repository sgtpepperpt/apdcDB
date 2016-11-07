#!/bin/bash

#This script runs the CryptDB proxy
#usage: ./proxy.sh <proxy-address> <proxy-backend-to-mysql> <cryptdb-path> <cryptdb-pass>
PROXY_IP=$1
MYSQL_IP=$2

# prepare proxy launch
export EDBDIR=$3 #/home/pepper/apdc/proxy/cryptdb
export CRYPTDB_PASS=$4
#Y1VzkAmF
fuser -k 3307/tcp

# now launch
$EDBDIR/bins/proxy-bin/bin/mysql-proxy				\
--plugins=proxy										\
--event-threads=4									\
--max-open-files=1024								\
--proxy-lua-script=$EDBDIR/mysqlproxy/wrapper.lua	\
--proxy-address=$PROXY_IP:3307						\
--proxy-backend-addresses=$MYSQL_IP:3306

$SHELL
