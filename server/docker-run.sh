#!/bin/bash

rm -rf docker-tmp/*
docker run -it --cidfile="docker-tmp/cid" -p 7868:7868 -p 9978:9978 $1
