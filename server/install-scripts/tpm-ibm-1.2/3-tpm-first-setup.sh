#!/bin/bash

SERVER_PATH="/home/pepper/apdc/server"
#SERVER_PATH="/server"

cd ../../attestation_tpm/data

tpm_takeownership -z -y
tpm_mkuuid uuid
tpm_mkaik -z blob-aik pubkey
tpm_loadkey blob-aik uuid
tpm_getpcrhash uuid hash pcrvals 10 11
