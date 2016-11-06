#!/bin/bash

export PROXY_PATH=../proxy

#get container id
CID=`cat docker-tmp/cid`

docker cp $CID:/server/attestation_tpm/data docker-tmp/
cp docker-tmp/data/{hash,pubkey,pcrvals} $PROXY_PATH/attestation_tpm/data/
