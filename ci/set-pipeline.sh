#!/bin/sh
echo y | fly -t home sp -p home-moneygr -c pipeline.yml -l ../../credentials.yml
