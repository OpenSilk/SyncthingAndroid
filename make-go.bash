#!/usr/bin/env bash

set -e

RESET=1

MYDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ -z "$TOOLCHAIN_ROOT" ]; then
    TOOLCHAIN_ROOT=/opt/android/ndk/toolchains/
fi
if [ -z "$GOROOT_BOOTSTRAP" ]; then
    export GOROOT_BOOTSTRAP=/usr/lib/go
fi

case "$1" in
    arm)
        export CC_FOR_TARGET=${TOOLCHAIN_ROOT}/arm/bin/arm-linux-androideabi-gcc
        export CXX_FOR_TARGET=${TOOLCHAIN_ROOT}/arm/bin/arm-linux-androideabi-g++
        export CGO_ENABLED=1
        export GOOS=android
        export GOARCH=arm
        export GOARM=7
        ;;
    386)
        export CC_FOR_TARGET=${TOOLCHAIN_ROOT}/386/bin/i686-linux-android-gcc
        export CXX_FOR_TARGET=${TOOLCHAIN_ROOT}/386/bin/i686-linux-android-g++
        export CGO_ENABLED=1
        export GOOS=android
        export GOARCH=386
        export GO386=387
        ;;
    amd64)
        export CC_FOR_TARGET=${TOOLCHAIN_ROOT}/amd64/bin/x86_64-linux-android-gcc
        export CXX_FOR_TARGET=${TOOLCHAIN_ROOT}/amd64/bin/x86_64-linux-android-g++
        export CGO_ENABLED=1
        export GOOS=android
        export GOARCH=amd64
        ;;
    *)
        echo "Must specify either arm or 386 or amd64"
        exit 1
esac

#TODO figure out why --depth 1 never works right
if [ $RESET -eq 1 ]; then
    git submodule update --init golang/go
fi

unset GOPATH

export GOROOT_FINAL=${MYDIR}/golang/dist/go-${GOOS}-${GOARCH}

if [ -d "$GOROOT_FINAL" ]; then
    rm -r "$GOROOT_FINAL"
fi
mkdir -p "$GOROOT_FINAL"

pushd golang/go/src

if [ "$GOARCH" = "386" ]; then
    git am -3 ../../../patches/golang/386/*
fi

set +e
./clean.bash
rm -r ../bin
rm -r ../pkg
set -e

if [ ! -e ../VERSION ]; then
    echo "$(git describe --tags)" > ../VERSION
fi

./make.bash --no-banner
cp -a ../bin "${GOROOT_FINAL}"/
cp -a ../pkg "${GOROOT_FINAL}"/
cp -a ../src "${GOROOT_FINAL}"/

if [[ $RESET -eq 1 && -e ./make.bash ]]; then
    pushd ../
    git clean -f
    popd
fi

popd

if [ $RESET -eq 1 ]; then
    git submodule update --init golang/go
fi

echo "Complete"