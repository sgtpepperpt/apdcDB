FROM ubuntu:12.04

RUN apt-get update
RUN apt-get -qq -y install curl sudo
RUN apt-get -y install apt-utils build-essential cmake git libgtk2.0-dev pkg-config bash-completion ruby

RUN mkdir proxy
WORKDIR proxy

RUN mkdir libs
RUN mkdir Proxy
RUN mkdir scripts

COPY libs/ libs/
COPY Proxy/ Proxy/
COPY scripts/ scripts/

## DISPATCHER INSTALLATION ##
WORKDIR scripts
RUN sudo ./install-cryptdb-proxy.sh
WORKDIR ..

EXPOSE 5482

#ENTRYPOINT ./run-server.sh
