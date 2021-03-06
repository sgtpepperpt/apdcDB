#!/bin/bash

#install a compatible gcc for c++11 (ubuntu 12 doesn't support it natively)
function update_gcc {
	#gcc --version | sed '1q;d' | grep -oE '[^ ]+$'
	sudo apt-get -y install python-software-properties
	sudo add-apt-repository -y ppa:ubuntu-toolchain-r/test
	sudo apt-get update
	sudo apt-get -y install gcc-4.7 g++-4.7
	sudo update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-4.7 60 --slave /usr/bin/g++ g++ /usr/bin/g++-4.7
}


