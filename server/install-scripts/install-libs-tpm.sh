#!/bin/bash

SERVER_PATH="/server"

cd ../libs/tpm

#install tpm-emulator
sudo apt-get -y install libgmp3-dev linux-headers-$(uname -r)
cd $SERVER_PATH/tpm-lib/tpm-emulator-master/
mkdir build
cd build/
cmake ../
make
sudo depmod -a
echo "aa"
sudo make install
echo "aa"
sudo depmod -a
sudo modprobe tpmd_dev
exit
#fazer restart
sudo modprobe tpmd_dev
sudo tpmd -f &

#trousers
sudo apt-get -y install trousers
sudo chown tss /dev/tpm0
sudo chgrp tss /dev/tpm0
gksu gedit /etc/init.d/trousers
# http://stackoverflow.com/questions/21957979/installing-trousers-for-tpm-emulator
# e tirar o chuid
sudo apt-get -y install trousers
sudo apt-get -y install tpm-tools
sudo apt-get -y install libtspi-dev libtspi1


tpm_version 
cd $SERVER_PATH/tpm-quote-tools-1.0.2/
./configure 
make
make check
sudo make install
make installcheck

#installation finished, start services

sudo $SERVER_PATH/../run_tpm.sh

tpm_clear --force
tpm_setenable --enable --force
tpm_setactive --active

tpm_takeownership -z -y

#cd ../data/
#dir
#tpm_mkaik 
#tpm_mkaik -z aikblob pubkey
