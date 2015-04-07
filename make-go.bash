#!/usr/bin/env bash

set -e

if [ -z "$TOOLCHAIN_ROOT" ]; then
    TOOLCHAIN_ROOT=/opt/android/ndk/toolchain-arm
fi

export CC_FOR_TARGET=${TOOLCHAIN_ROOT}/bin/arm-linux-androideabi-gcc
export CXX_FOR_TARGET=${TOOLCHAIN_ROOT}/bin/arm-linux-androideabi-g++
export CGO_ENABLED=1
export GOOS=android
export GOARCH=arm
export GOARM=7
#export CGO_CFLAGS="-fPIE"
#export CGO_LDFLAGS="-fPIE" #-pie is already added

git submodule update --init golang/go

pushd golang/go/src

./make.bash

# extra paranoia
if [ -e ./make.bash ]; then
    git clean -f
fi

popd