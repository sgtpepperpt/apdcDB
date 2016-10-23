#!/bin/bash

cd cbir

export LD_LIBRARY_PATH=/usr/local/lib
export HOME_DIR_CBIR="/server/cbir/"
#export HOME_DIR_CBIR="/home/pepper/apdc/server/cbir/"

fuser -k 9978/tcp
./Server MIE
