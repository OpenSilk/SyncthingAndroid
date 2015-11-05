#!/usr/bin/env bash

set -e

RESET=1

if [ -z "$TOOLCHAIN_ROOT" ]; then
    TOOLCHAIN_ROOT=/opt/android/ndk/toolchains/
fi

MYDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ASSETSDIR=${MYDIR}/app/src/main/assets/

case "$1" in
    arm)
        export CC=${TOOLCHAIN_ROOT}/arm/bin/arm-linux-androideabi-gcc
        export CXX=${TOOLCHAIN_ROOT}/arm/bin/arm-linux-androideabi-g++
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

unset GOPATH #Set by build.go
export GOROOT=${MYDIR}/golang/dist/go-${GOOS}-${GOARCH}
export PATH=${GOROOT}/bin:${PATH}

if [ ! -x ${GOROOT}/bin/${GOOS}_${GOARCH}/go ]; then
    echo Need to build go for ${GOOS}-${GOARCH}
    exit 1
fi

if [ $RESET -eq 1 ]; then
    git submodule update --init syncthing/src/github.com/syncthing/syncthing
fi

pushd syncthing/src/github.com/syncthing/syncthing

git am -3 ../../../../../patches/syncthing/cgo/*
if [ $CGO_ENABLED -eq 0 ]; then
    git am -3 ../../../../../patches/syncthing/netgo/*
    netgo="-netgo"
fi

_GOOS=$GOOS
unset GOOS
_GOARCH=$GOARCH
unset GOARCH

go run build.go -goos=${_GOOS} -goarch=${_GOARCH} clean

if [ $CGO_ENABLED -eq 0 ]; then
    go run build.go -goos=${_GOOS} -goarch=${_GOARCH} -no-upgrade -netgo build
else
    go run build.go -goos=${_GOOS} -goarch=${_GOARCH} -no-upgrade build
fi

export GOOS=$_GOOS
export GOARCH=$_GOARCH

mv syncthing ${ASSETSDIR}/syncthing.${GOARCH}
chmod 644 ${ASSETSDIR}/syncthing.${GOARCH}

if [[ RESET -eq 1 && -e ./build.go ]]; then
    git clean -f
fi

popd

if [ $RESET -eq 1 ]; then
    git submodule update --init syncthing/src/github.com/syncthing/syncthing
fi

#Build syncthing-inotify

if [ $RESET -eq 1 ]; then
    git submodule update --init syncthing/src/github.com/syncthing/syncthing-inotify
fi

pushd syncthing/src/github.com/syncthing/syncthing-inotify

git am -3 ../../../../../patches/syncthing-inotify/godeps/*

export GOPATH=$(pwd)/Godeps/_workspace:${MYDIR}/syncthing

go clean

if [ $CGO_ENABLED -eq 0 ]; then
    go build -tags netgo -ldflags "-w -X main.Version=$(git describe --abbrev=0 --tags)"
else
    go build -ldflags "-w -X main.Version=$(git describe --abbrev=0 --tags) -extldflags '-fPIE -pie'"
fi

mv syncthing-inotify ${ASSETSDIR}/syncthing-inotify.${GOARCH}
chmod 644 ${ASSETSDIR}/syncthing-inotify.${GOARCH}

popd

if [ $RESET -eq 1 ]; then
    git submodule update --init syncthing/src/github.com/syncthing/syncthing-inotify
fi

echo "Build Complete"
