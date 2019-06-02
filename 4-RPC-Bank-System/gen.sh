#!/usr/bin/env bash
OUT=./gen
ICE_FILE=slice/bankSystem.ice
PROTO_FILE=proto/bankService.proto
PROTOC_PATH=./proto/protoc-gen-grpc-java.exe

slice2java --output-dir ${OUT} ${ICE_FILE}

protoc -I=. --java_out=${OUT} --plugin=protoc-gen-grpc-java=${PROTOC_PATH} --grpc-java_out=${OUT} ${PROTO_FILE}
