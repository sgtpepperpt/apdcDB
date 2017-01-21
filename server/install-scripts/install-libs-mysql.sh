#!/bin/bash

if [ -z ${MYSQL_PASS+x} ]; then
	echo "ERROR: MYSQL_PASS needs to be set!" && exit 1
fi

echo "mysql-server mysql-server/root_password password $MYSQL_PASS" | sudo debconf-set-selections
echo "mysql-server mysql-server/root_password_again password $MYSQL_PASS" | sudo debconf-set-selections

sudo apt-get -y install mysql-server-5.5
