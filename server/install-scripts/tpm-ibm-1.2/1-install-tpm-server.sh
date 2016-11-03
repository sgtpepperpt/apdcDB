#!/bin/bash

SERVER_PATH="/home/pepper/apdc/server"
#SERVER_PATH="/server"

#for the server
export TPM_PATH=/home/pepper/apdc/server/attestation_tpm/tpm_data
export TPM_PORT=7869

#for the client (tpmbios)
export TPM_SERVER_NAME=localhost
export TPM_SERVER_PORT=$TPM_PORT

#kill tpm_server if running
fuser -k $TPM_PORT/tcp

#delete older tpm configurations
cd ../../attestation_tpm/tpm_data/
rm -rf *.permall
cd -

#make tpm_server
cd ../../libs/tpm/ibm/tpm
cp makefile-tpm Makefile
make

#first run
./tpm_server > /dev/null &
SERVER_PID=$!
sleep 2 #let it run

tpmbios
kill $SERVER_PID

#second run
./tpm_server > /dev/null &
SERVER_PID=$!
sleep 2 #let it run

tpmbios
createek
../libtpm/utils/nv_definespace -in ffffffff -sz 0
kill $SERVER_PID
