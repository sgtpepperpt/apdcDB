#!/bin/bash

if [ "$EUID" -ne 0  ]
then
	echo "Pls root"
	exit
fi

echo "Starting server services"

#run tpm emulators
cd attestation_tpm/
sudo ./run_tpm.sh
sleep 1

#run attestation dispatcher
./main -w &

#run cbir server
cd ../cbir/
./run_cbir.sh &

