#!/bin/bash

sudo apt-get -y install build-essential locate cmake git libgtk2.0-dev pkg-config libavcodec-dev libavformat-dev libswscale-dev python-dev python-numpy libtbb2 libtbb-dev libjpeg-dev libpng-dev libtiff-dev libjasper-dev libdc1394-22-dev ant openjdk-7-jdk

cd ../libs

#opencv
mkdir opencv-2.4.10/build
cd opencv-2.4.10/build

export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/
sudo -E cmake -dwith_cuda=off -dwith_ffmpeg=off	\
-DJAVA_AWT_LIBRARY=`find / -name libawt.so`		\
-DJAVA_AWT_INCLUDE_PATH=`find / -name jawt.h`	\
-DJAVA_INCLUDE_PATH=`find / -name jni.h`		\
-DJAVA_INCLUDE_PATH2=`find / -name jni_md.h` 	\
..

make
sudo make install
