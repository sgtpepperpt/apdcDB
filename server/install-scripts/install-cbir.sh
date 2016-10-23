#!/bin/bash

cd ../cbir

#install a compatible gcc for c++11 (ubuntu 12 doesn't support it natively)
sudo apt-get -y install python-software-properties
sudo add-apt-repository -y ppa:ubuntu-toolchain-r/test
sudo apt-get update
sudo apt-get -y install gcc-4.7 g++-4.7
sudo update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-4.7 60 --slave /usr/bin/g++ g++ /usr/bin/g++-4.7

#clear old files and make new
sudo make clean
make
