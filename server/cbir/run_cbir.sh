#!/bin/bash

export LD_LIBRARY_PATH=/usr/local/lib

fuser -k 9978/tcp
./Server MIE
