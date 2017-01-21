#Server Deployment
##Installing the modules indivually
The server can be installed as separate modules, which are provided in this folder. You should analyze the scripts on _install-scripts_. First you should install the libs needed for each component, and then the component itself.

Check for absolute paths in these installation scripts.

##Using a Docker image
A Docker image containing the server's components ready to use is also provided. Simply create the image from the Dockerfile, and the server is ready to use.

However, each time the server is run - and a new container is created - the TPM identity is reset and you will need to copy some attestation files to the proxy. This is done automatically by the _docker\_get\_files.sh_ script.

Follow these instructions to run:

**To build Docker image:**
> docker build -t server .

**To run:**
> ./docker\_run.sh _your-image-id_

**To get the TPM files in the proxy:**
> ./docker\_get\_files.sh

Check, in the script, if the path is correct.

###Currently included
- [x] CBIR server
- [x] TPM Attestation
- [ ] MySQL server


##About the libraries
For the **Attestation Module** you will need the following libraries (included):
* [IBM's Software Trusted Module](http://ibmswtpm.sourceforge.net/)
* [Trousers 0.3.7](https://sourceforge.net/projects/trousers/files/trousers/)
* [TPM-Tools 1.3.3](https://sourceforge.net/projects/trousers/files/tpm-tools/)
* [TPM Quote Tools 1.0.2](https://sourceforge.net/projects/tpmquotetools/)
* ~~[TPM-Emulator](https://github.com/PeterHuewe/tpm-emulator)~~ _Note:_ Due to issues with the Docker deployment, we've deprecated this library in favour of IBM's. However, installation scripts and tips on TPM-Emulator's usage are included as legacy if you're interested.


For the **CBIR Server** you will need the to download OpenCV 3.0.0 and extract it to _./libs/cbir/opencv-3.0.0_. You can find it on [OpenCV's official website](http://opencv.org/downloads.html). You don't need to compile it, that's dealt by the installation scripts. If you have an error with _AVCodecID_, check (http://stackoverflow.com/questions/31663498/opencv-3-0-0-make-error-with-ffmpeg).
