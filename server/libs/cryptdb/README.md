# INSTALLATION INSTRUCTIONS FOR CRYPTDB ON A DBaaS SERVICE
# by Guilherme Borges
# July 2016
# NOVA-LINCS FCT-UNL

CryptDB needs, on the server side (DBMS), two different libraries. I provide all the needed files on the "cryptdb-lib" folder, but you may need to recompile the libraries if you have a different architecture. I provide instructions on what I did below.

###############################################################################
1. EDB.SO 

This library is created while installing CryptDB locally, and can be found on "/usr/lib/mysql/plugin/edb.so".

- You need to transfer this file to the corresponding MySQL plugin folder on the server (e.g., in Google Cloud: "/opt/bitnami/mysql/lib/plugin/edb.so").

- Just in case, cd to the directory and own the file with:
sudo chmod 777 edb.so 
sudo chown root:root edb.so

###############################################################################
2. NTL (Number Theory Library)

You need to download and compile a library called NTL (libntl), which can be found on http://www.shoup.net/ntl/doc/tour-unix.html. I transfered the library files (on cryptdb-lib/ntl/) to the server (/usr/lib/ folder) and it worked (on a Google Cloud Bitnami MySQL DBaaS with the following specs: Debian 8 OS, MySQL (5.6.31) and x86_64 architecture.

However, should you need to compile the ntl library again, follow these instructions:

- Download one of the older versions (I tried to use ntl-5.5, I cannot guarantee newer versions will work as some signatures change. CryptDB even expects an older version (5.4.2), but 5.5 should work. You can try and download a newer version, but make sure it has the option to compile itself into a shared library (SHARED flag on configure script) - this is very important! I did not use 5.4.2 because of this exact reason: MySQL will need a shared library). If you can compile 5.4.2 and then convert the static library to a shared one, even better.

- To make sure you have the needed packages, first of all run:
sudo apt-get install libgmp3-dev libtool libtool-bin

- Transfer the file to the server and extract it. Then it's compile time:

gunzip ntl-5.5.tar.gz
tar xf ntl-5.5.tar
cd ntl-5.5/src
./configure SHARED=on PREFIX=/usr
make
make check
sudo make install

- cd to /usr/lib and create a static link with the 5.4.2 version in the filename (even if it's another version)
sudo ln -s libntl.so libntl-5.4.2.so


Then you can try and run CryptDB's proxy with it's backend address pointing to the cloud's ip. Hope it works!
