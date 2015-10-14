#!/usr/bin/env bash

set -e

MYDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ -z "$TOOLCHAIN_ROOT" ]; then
    TOOLCHAIN_ROOT=/opt/android/ndk/toolchains/x86
fi

#TODO figure out why --depth 1 never works right
git submodule update --init golang/go1.4
git submodule update --init golang/go

unset GOPATH
#export CC_FOR_TARGET=${TOOLCHAIN_ROOT}/bin/i686-linux-android-gcc
#export CXX_FOR_TARGET=${TOOLCHAIN_ROOT}/bin/i686-linux-android-g++
#export CGO_ENABLED=1 #TODO fails
export CGO_ENABLED=0
export GOOS=linux
export GOARCH=386
export GO386=387
#export CGO_CFLAGS="-fPIE"
#export CGO_LDFLAGS="-fPIE" #-pie is already added

#Build go 1.4 for bootstrap
pushd golang/go1.4/src

export GOROOT=${MYDIR}/golang/go1.4
set +e
./clean.bash
unset GOROOT
set -e
./make.bash

# extra paranoia
if [ -e ./make.bash ]; then
    git clean -f
fi

popd

echo "Built Go 1.4"

#Build go 1.5 with bootstraped 1.4
export GOROOT_BOOTSTRAP=${MYDIR}/golang/go1.4
export GOROOT=${MYDIR}/golang/dist/go-x86

pushd golang/go/src

set +e
./clean.bash
set -e
./make.bash

popd

echo "Complete"