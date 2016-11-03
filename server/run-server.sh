#!/bin/bash
#set this to the server base folder (full path)
export BASE_DIR=/home/pepper/apdc/server

#for the attestation module - to generate the hashes
export CBIR_LOCATION=$BASE_DIR/cbir
export MYSQL_LOCATION=/usr/bin/mysql

#for the cbir server (full path)
export HOME_DIR_CBIR=$CBIR_LOCATION

#to run the tpm software
export TPM_PATH=$BASE_DIR + /attestation_tpm/tpm_data

#start daemons for attestation
cd run-scripts
./run-tpm-server-ibm.sh
./run-tpm-tcsd-ibm.sh
cd ..

#start the attestation dispatcher
cd attestation_tpm
./dispatcher -w &
sleep 1
cd ..

#start the cbir server
cd run-scripts
./run-cbir.sh &
cd ..
