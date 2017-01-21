#PROXY
The proxy is available as an Eclipse project.

You need to install OpenCV 2.4.10 and link it to the project before running. Variables need to be set up in the .properties files.

If you already have the .jar compiled, set the path to the OpenCV library in the _run-proxy.sh_ script and run it.

##Usage
**Normal server usage:**
> ProxyConnector [-u] [-l]

###Flags
* **u**: unenrypted (no CryptDB) mode
* **l**: local mode (instead of cloud)

##Special test modes
* **[--attest  <server-host>]** Attestation mode
* **[--cbir  <server-host>]** CBIR test mode _NOT FULLY IMPLEMENTED_
