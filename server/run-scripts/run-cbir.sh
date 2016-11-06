#!/bin/bash

cd ../cbir

export LD_LIBRARY_PATH=/usr/local/lib

#The full path to the cbir server (defined in calling script ../run-server.sh)
#export HOME_DIR_CBIR="/server/cbir/"

if [ -z ${HOME_DIR_CBIR+x} ]; then
	echo "ERROR: HOME_DIR_CBIR needs to be set!" && exit 1
fi

kill -9 $(lsof -t -i:9978)
#fuser -k 9978/tcp

./Server MIE
