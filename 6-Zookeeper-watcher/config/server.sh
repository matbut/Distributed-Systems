#!/usr/bin/env bash
./bin/zkServer.sh $1 zoo1.cfg
./bin/zkServer.sh $1 zoo2.cfg
./bin/zkServer.sh $1 zoo3.cfg