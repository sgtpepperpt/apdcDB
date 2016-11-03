#!/bin/bash

export TCSD_TCP_DEVICE_PORT=7869
sudo killall tcsd
sudo -E tcsd -e -f
