#!/bin/bash

if [ "$EUID" -ne 0  ]
then
	echo "Pls root"
	exit
fi

cd attestation_tpm

#kill if running
sudo rm -f /run/tpm/tpmd_socket:0
sudo kill `ps -ef | grep tpmd | grep -v grep | awk '{print $2}'`
sudo killall tcsd

tpmd -d clear
tcsd -e -f &
