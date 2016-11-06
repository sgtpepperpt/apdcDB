FROM ubuntu:12.04

RUN apt-get update
RUN apt-get -qq -y install curl sudo
RUN apt-get -y install apt-utils build-essential cmake git libgtk2.0-dev pkg-config libavcodec-dev libavformat-dev libswscale-dev python-dev python-numpy libtbb2 libtbb-dev libjpeg-dev libpng-dev libtiff-dev libjasper-dev libdc1394-22-dev bash-completion ruby automake autoconf libtool nano lsof psmisc

RUN mkdir server
WORKDIR server

RUN mkdir libs
RUN mkdir cbir
RUN mkdir -p attestation_tpm/data/tmp
RUN mkdir install-scripts
RUN mkdir run-scripts

COPY libs/ libs/
COPY cbir/ cbir/
COPY attestation_tpm/ attestation_tpm/
COPY install-scripts/ install-scripts/
COPY run-scripts/ run-scripts/
COPY run-server.sh run-server.sh

## CBIR INSTALLATION ##
WORKDIR install-scripts
RUN sudo ./install-libs-cbir.sh
WORKDIR ..

WORKDIR install-scripts
RUN sudo ./install-cbir.sh
WORKDIR ..

## TPM INSTALLATION ##
WORKDIR install-scripts/tpm-ibm-1.2
RUN sudo ./1-install-tpm-server.sh
RUN sudo ./2-install-trousers.sh
WORKDIR ../..

## DISPATCHER INSTALLATION ##
WORKDIR install-scripts
RUN sudo ./install-tpm.sh
WORKDIR ..

#cbir, attestation, mysql
EXPOSE 9978:9978
EXPOSE 7868:7868
#EXPOSE 3306

ENTRYPOINT ./run-server.sh
