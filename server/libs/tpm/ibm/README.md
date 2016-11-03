cd libs/tpm/ibm/tpm

#tpm server
export TPM_PATH=/home/pepper/apdc/server/attestation_tpm/tpm_data
export TPM_PORT=7869
./tpm_server

#tpm client
export TPM_SERVER_NAME=localhost
export TPM_SERVER_PORT=7869
tpmbios

#first time
createek
cd ../libtpm/utils
./nv_definespace -in ffffffff -sz 0

#tcsd
export TCSD_TCP_DEVICE_PORT=7869
sudo -E tcsd -e -f
