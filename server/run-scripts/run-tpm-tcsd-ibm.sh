#!/bin/bash

function run_tcsd {
	export TCSD_TCP_DEVICE_PORT=7869
	sudo killall tcsd
	sudo -E tcsd -e -f &
	sleep 2 # let it run
}

run_tcsd
