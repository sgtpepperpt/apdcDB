#!/bin/bash

#start tpm services
cd ../../run-scripts
export TPM_PATH=/server/attestation_tpm/tpm_data
./run-tpm-server-ibm.sh
./run-tpm-tcsd-ibm.sh

#go to data folder
cd ../attestation_tpm/data

tpm_takeownership -z -y
tpm_mkuuid uuid
echo "a"
tpm_mkaik -z blob-aik pubkey
echo "b"
tpm_loadkey blob-aik uuid
echo "c"
tpm_getpcrhash uuid hash pcrvals 10 11
echo "d"
