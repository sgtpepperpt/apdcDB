#!/bin/bash

. util.sh

update_gcc

cd ../cbir

#clear old files and make new
sudo make clean
make
