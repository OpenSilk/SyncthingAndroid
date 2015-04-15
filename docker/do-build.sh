#!/usr/bin/env bash

if [ -n "$1" ]; then
    echo Skipping build. Do what you want.
    exec "$@"
elif [ ! -d src/github.com/syncthing/syncthing ]; then
        echo "Fatal: Must mount GOPATH at /opt/workspace"
        exit 1
else
    export CC=${TOOLCHAIN_ROOT}/bin/arm-linux-androideabi-gcc
    export CXX=${TOOLCHAIN_ROOT}/bin/arm-linux-androideabi-g++
    export CGO_ENABLED=1
    export GOARM=7

    cd src/github.com/syncthing/syncthing
    go run build.go -goos=android -goarch=arm clean
    go run build.go -goos=android -goarch=arm -no-upgrade build
fi