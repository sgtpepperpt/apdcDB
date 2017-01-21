#!/bin/bash

CURR_DIR=$(pwd)
IMAGE_HASH=$1
shift

rm -rf docker-tmp/*

docker run -it							\
--cidfile="docker-tmp/cid"				\
-p 7868:7868 -p 9978:9978				\
-v $CURR_DIR/storage:/server/storage	\
$IMAGE_HASH $@

#--name server_container
