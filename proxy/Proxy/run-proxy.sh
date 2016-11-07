#!/bin/bash

export OPENCV_PATH=/home/pepper/apdc/proxy/opencv-2.4.10/build/lib/

java -jar -Djava.library.path=$OPENCV_PATH ProxyDispatcher.jar $@

$SHELL
