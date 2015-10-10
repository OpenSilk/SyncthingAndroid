#!/usr/bin/env bash

set -e

if [ -z "$TOOLCHAIN_ROOT" ]; then
    TOOLCHAIN_ROOT=/opt/android/ndk/toolchain-arm
fi

MYDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ASSETSDIR=${MYDIR}/app/src/main/assets/

unset GOPATH #Set by build.go
export GOROOT=${MYDIR}/golang/go
export PATH=${GOROOT}/bin:${PATH}
export CC=${TOOLCHAIN_ROOT}/bin/arm-linux-androideabi-gcc
export CXX=${TOOLCHAIN_ROOT}/bin/arm-linux-androideabi-g++
export CGO_ENABLED=1
#export GOOS=android
#export GOARCH=arm
export GOARM=7

git submodule update --init syncthing/src/github.com/syncthing/syncthing

if [ ! -x ${GOROOT}/bin/android_arm/go ]; then
    echo Need to build go for android
    exit 1
fi

pushd syncthing/src/github.com/syncthing/syncthing

git am -3 ../../../../../patches/syncthing/cgo/*

go run build.go -goos=android -goarch=arm clean
go run build.go -goos=android -goarch=arm -no-upgrade build

mv syncthing ${ASSETSDIR}/syncthing.arm
chmod 644 ${ASSETSDIR}/syncthing.arm
#git clean -f

popd

#Build syncthing-inotify

git submodule update --init syncthing/src/github.com/syncthing/syncthing-inotify

export GOOS=android
export GOARCH=arm

pushd syncthing/src/github.com/syncthing/syncthing-inotify


git am -3 ../../../../../patches/syncthing-inotify/godeps/*

export GOPATH=$(pwd)/Godeps/_workspace:${MYDIR}/syncthing

go clean
go build -ldflags "-w -X main.Version=$(git describe --abbrev=0 --tags) -extldflags '-fPIE -pie'"

mv syncthing-inotify ${ASSETSDIR}/syncthing-inotify.arm
chmod 644 ${ASSETSDIR}/syncthing-inotify.arm

echo "Build Complete"
