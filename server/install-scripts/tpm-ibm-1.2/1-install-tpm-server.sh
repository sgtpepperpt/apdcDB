#!/bin/bash

#for the server
export TPM_PATH=/server/attestation_tpm/tpm_data #/home/pepper/apdc
export TPM_PORT=7869

#for the client (tpmbios)
export TPM_SERVER_NAME=localhost
export TPM_SERVER_PORT=$TPM_PORT

#kill tpm_server if running
kill -9 $(lsof -t -i:$TPM_PORT)

#delete older tpm configurations
cd ../../attestation_tpm/tpm_data/
rm -rf *.permall
cd -

#make tpm_server
cd ../../libs/tpm/ibm/tpm
cp makefile-tpm Makefile
make

#make utilities
cd ../libtpm
./autogen
./configure
make && make install
cd ../tpm

#first run
./tpm_server > /dev/null &
SERVER_PID=$!
sleep 2 #let it run

../libtpm/utils/tpmbios
kill $SERVER_PID

#second run
./tpm_server > /dev/null &
SERVER_PID=$!
sleep 2 #let it run

../libtpm/utils/tpmbios
createek
../libtpm/utils/nv_definespace -in ffffffff -sz 0
kill $SERVER_PID
