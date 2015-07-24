#!/bin/bash -e
# Make a different copy for x86 and ARM (See https://github.com/golang/go/issues/8161 and https://github.com/golang/go/issues/9114)
unset GOROOT
./build-syncthing-cgo-arm.sh
./build-syncthing-cgo-x86.sh
