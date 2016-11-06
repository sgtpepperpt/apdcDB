#!/bin/bash

#get container id
CID=`cat docker-tmp/cid`

docker cp $CID:/server/attestation_tpm/data docker-tmp/
cp docker-tmp/data/{hash,pubkey,pcrvals} ../proxy/attestation_tpm/data/
