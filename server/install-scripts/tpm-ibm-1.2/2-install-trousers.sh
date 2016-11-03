#!/bin/bash

SERVER_PATH="/home/pepper/apdc/server"
#SERVER_PATH="/server"

#for the server
export TPM_PATH=/home/pepper/apdc/server/attestation_tpm/tpm_data
export TPM_PORT=7869

#for the client (tpmbios)
export TPM_SERVER_NAME=localhost
export TPM_SERVER_PORT=$TPM_PORT

#for the tcsd
export TCSD_TCP_DEVICE_PORT=$TPM_PORT

#start tpm server
cd ../../libs/tpm/ibm/tpm
./tpm_server > /dev/null &
sleep 2 #let it run
tpmbios

#make and install trousers
cd ../../trousers-0.3.7/
sh bootstrap.sh
export PKG_CONFIG_PATH=/usr/lib64/pkgconfig
CFLAGS="-L/usr/lib64 -L/opt/gnome/lib64" LDFLAGS="-L/usr/lib64 -L/opt/gnome/lib64" ./configure --libdir="/usr/local/lib64"
make
sudo make install
exit
#install tools
cd ../tpm-tools-1.3.8/
sh ./bootstrap.sh
./configure
make
sudo make install

#install libtspi
sudo apt-get -y install libtspi-dev libtspi1
