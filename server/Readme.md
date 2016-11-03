#Server Deployment
##Installing the modules indivually
The server can be installed as separate modules, which are provided in this folder. You should analyze the scripts on _install-scripts_. First you shuld install the libs needed for each component, and then the component itself.

Check for absolute paths in these installation scripts.

##Using a Docker image
A Docker image containing the server's components ready to use is also provided. Simply create the image from the Dockerfile, and the server is ready to use.
Note: You need to download OpenCV-3.0.0 and extract it to _./libs/cbir/opencv-3.0.0_. You can find it on [OpenCV's official website](http://opencv.org/downloads.html).

Currently included
[*]CBIR server
[ ]TPM Attestation
[ ]MySQL server

**To build Docker image:**
		docker build -t server .

**To run:**
		docker run -it -p 9978:9978 -p 7868:7868 _your-container-id_
