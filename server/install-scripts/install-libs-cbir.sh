#!/bin/bash

cd ../libs/cbir

sudo apt-get -y install libgmp3-dev

#opencv
mkdir opencv-3.0.0/build
cd opencv-3.0.0/build
cmake  -dwith_cuda=off -dwith_ffmpeg=off ../
make
sudo make install
