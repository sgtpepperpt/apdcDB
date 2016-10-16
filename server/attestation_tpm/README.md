#HOW TO INITIALIZE AND GENERATE QUOTES WITH TPM
tpm_clear -z --force &&\
tpm_setenable --enable --force &&\
tpm_setactive --active
tpm_takeownership -z -y

#############
#INITIALIZATION
#server-side: generate keys; then send pubkey, hash and pcrvals files to proxy
tpm_mkuuid uuid
tpm_mkaik -z blob-aik pubkey
tpm_loadkey blob-aik uuid
tpm_getpcrhash uuid hash pcrvals 10 11

#USAGE
#server-side: generate quote (this is what the dispatcher does) and send it to the requester
tpm_getquote -p pcrvals uuid nonce quote 10 11

#client or proxy-side: verify quote with files
tpm_verifyquote pubkey hash nonce quote
