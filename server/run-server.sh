#!/bin/bash
#set this to the server base folder (full path)
export BASE_DIR=/server #home/pepper/apdc

#for the attestation module - to generate the hashes
export CBIR_LOCATION=$BASE_DIR/cbir
export MYSQL_LOCATION=/usr/bin/mysql

#for the cbir server (full path)
export HOME_DIR_CBIR=$BASE_DIR/storage/cbir/

#to run the tpm software
export TPM_PATH=$BASE_DIR/storage/tpm

FIRST_CONFIG=false

#process arguments
for i in "$@"
do
case $i in
    --f|--first)
    FIRST_CONFIG=true
    ;;
    *)
    echo "Usage: run_server.sh [-f|--first]\n-f\tFirst configuration (create and setup TPM)"
    ;;
esac
done

#start daemons for attestation
cd run-scripts
./run-tpm-server-ibm.sh
./run-tpm-tcsd-ibm.sh
cd ..

#start the attestation dispatcher
cd attestation_tpm
./dispatcher -w &
sleep 1

if [ $FIRST_CONFIG ]; then
	sudo ./tpm-first-setup.sh
	sleep 2	
fi

cd ..        

#start the cbir server
cd run-scripts
./run-cbir.sh &
cd ..

#keep alive
while true
do
	sleep 10
done
