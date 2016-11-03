#!/bin/bash

#should be defined in calling script ../run-server.sh anyway
if [ -z ${TPM_PATH+x} ]; then
	echo "ERROR: HOME_DIR_CBIR needs to be set!" && exit 1
fi

#for the server
#export TPM_PATH=/home/pepper/apdc/server/attestation_tpm/tpm_data
export TPM_PORT=7869

#for the client (tpmbios)
export TPM_SERVER_NAME=localhost
export TPM_SERVER_PORT=$TPM_PORT

fuser -k $TPM_PORT/tcp

cd ../libs/tpm/ibm/tpm
./tpm_server &
sleep 2 #let it run fully
tpmbios
