#!/usr/bin/env bash

set -e

MYDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ -z "$TOOLCHAIN_ROOT" ]; then
    TOOLCHAIN_ROOT=/opt/android/ndk/toolchains/
fi

case "$1" in
    arm)
        export CC_FOR_TARGET=${TOOLCHAIN_ROOT}/arm/bin/arm-linux-androideabi-gcc
        export CXX_FOR_TARGET=${TOOLCHAIN_ROOT}/arm/bin/arm-linux-androideabi-g++
        export CGO_ENABLED=1
        export GOOS=android
        export GOARCH=arm
        export GOARM=7
        #export CGO_CFLAGS="-fPIE"
        #export CGO_LDFLAGS="-fPIE" #-pie is already added
        ;;
    x86)
        #export CC_FOR_TARGET=${TOOLCHAIN_ROOT}/bin/i686-linux-android-gcc
        #export CXX_FOR_TARGET=${TOOLCHAIN_ROOT}/bin/i686-linux-android-g++
        #export CGO_ENABLED=1 #TODO fails
        export CGO_ENABLED=0
        export GOOS=linux
        export GOARCH=386
        export GO386=387
        #export CGO_CFLAGS="-fPIE"
        #export CGO_LDFLAGS="-fPIE" #-pie is already added
        ;;
    *)
        echo "Must specify either arm or x86"
        exit 1
esac

#TODO figure out why --depth 1 never works right
git submodule update --init golang/go1.4
git submodule update --init golang/go

unset GOPATH

#Build go 1.4 for bootstrap
pushd golang/go1.4/src

set +e
export GOROOT=${MYDIR}/golang/go1.4
./clean.bash
unset GOROOT
rm -r ../bin
rm -r ../pkg
set -e
./make.bash

if [ -e ./make.bash ]; then
    git clean -f
fi

popd

echo "Built Go 1.4"

#Build go 1.5 with bootstraped 1.4
export GOROOT_BOOTSTRAP=${MYDIR}/golang/go1.4

export GOROOT_FINAL=${MYDIR}/golang/dist/go-${GOOS}-${GOARCH}

if [ -d "$GOROOT_FINAL" ]; then
    rm -r "$GOROOT_FINAL"
fi
mkdir -p "$GOROOT_FINAL"

pushd golang/go/src

if [ $CGO_ENABLED -eq 0 ]; then
    git am -3 ../../../patches/golang/netgohacks/*
fi

set +e
./clean.bash
rm -r ../bin
rm -r ../pkg
set -e
./make.bash
cp -a ../bin "${GOROOT_FINAL}"/
cp -a ../pkg "${GOROOT_FINAL}"/
cp -a ../src "${GOROOT_FINAL}"/

popd

echo "Complete"