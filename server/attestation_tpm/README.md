#INITIALIZATION
Before using, you need to initialize the TPM with its keys and get the expected hashes, which the proxy needs to attest the server.

This process is done automatically by the _3-tpm-first-setup.sh_ script in the _install-scripts_ folder. However, we present it here for clarity, and in case you want to execute the steps manually.


**server-side: generate keys; then send pubkey, hash and pcrvals files to proxy**
>tpm\_takeownership -z -y _(done only once, when the TPM is installed)_
>tpm_mkuuid uuid
>tpm_mkaik -z blob-aik pubkey
>tpm_loadkey blob-aik uuid
>tpm_getpcrhash uuid hash pcrvals 10 11 #at this point, the PCR registers must contain the desired value

#USAGE
**server-side: generate quote (this is what the dispatcher does) and send it to the requester**
>tpm_getquote -p pcrvals uuid nonce quote 10 11

**client or proxy-side: verify quote with files**
>tpm_verifyquote pubkey hash nonce quote

#HOW TO INITIALIZE AND GENERATE QUOTES WITH TPM
**NOTE:** This part applies only to an installation using the TPM-Emulator.
    tpm_clear -z --force &&\
    tpm_setenable --enable --force &&\
    tpm_setactive --active
    tpm_takeownership -z -y

############################
