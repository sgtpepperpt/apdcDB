FROM ubuntu:12.04

RUN apt-get -qq update
RUN apt-get -qq -y install curl sudo
RUN sudo apt-get -y install apt-utils build-essential cmake git libgtk2.0-dev pkg-config libavcodec-dev libavformat-dev libswscale-dev python-dev python-numpy libtbb2 libtbb-dev libjpeg-dev libpng-dev libtiff-dev libjasper-dev libdc1394-22-dev nano bash-completion

RUN mkdir server
WORKDIR server

RUN mkdir libs
RUN mkdir cbir
RUN mkdir attestation_tpm

COPY libs/ libs/

## CBIR INSTALLATION ##
WORKDIR libs
RUN sudo ./install-libs-cbir.sh

WORKDIR ..
COPY cbir/ cbir/

WORKDIR cbir
RUN sudo ./install-cbir.sh

WORKDIR ..
COPY attestation_tpm/ attestation_tpm/

EXPOSE 9978
EXPOSE 7868
#ENTRYPOINT ./run_cbir.sh
