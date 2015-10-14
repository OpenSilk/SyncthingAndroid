#!/usr/bin/env bash

set -e

if [ -z "$TOOLCHAIN_ROOT" ]; then
    TOOLCHAIN_ROOT=/opt/android/ndk/toolchains/x86
fi

MYDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ASSETSDIR=${MYDIR}/app/src/main/assets/

unset GOPATH #Set by build.go
export GOROOT=${MYDIR}/golang/go
export PATH=${GOROOT}/bin:${PATH}
#export CC=${TOOLCHAIN_ROOT}/bin/i386-linux-android-gcc
#export CXX=${TOOLCHAIN_ROOT}/bin/i386-linux-android-g++
#export CGO_ENABLED=1
export CGO_ENABLED=0
export GO386=387

git submodule update --init syncthing/src/github.com/syncthing/syncthing

if [ ! -x ${GOROOT}/bin/linux_386/go ]; then
    echo Need to build go for x86
    exit 1
fi

pushd syncthing/src/github.com/syncthing/syncthing

git am -3 ../../../../../patches/syncthing/cgo/*

go run build.go -goos=linux -goarch=386 clean
go run build.go -goos=linux -goarch=386 -no-upgrade build

mv syncthing ${ASSETSDIR}/syncthing.x86
chmod 644 ${ASSETSDIR}/syncthing.x86
#git clean -f

popd

#Build syncthing-inotify

git submodule update --init syncthing/src/github.com/syncthing/syncthing-inotify

export GOOS=linux
export GOARCH=386

pushd syncthing/src/github.com/syncthing/syncthing-inotify


git am -3 ../../../../../patches/syncthing-inotify/godeps/*

export GOPATH=$(pwd)/Godeps/_workspace:${MYDIR}/syncthing

go clean
go build -ldflags "-w -X main.Version=$(git describe --abbrev=0 --tags) -extldflags '-fPIE -pie'"

mv syncthing-inotify ${ASSETSDIR}/syncthing-inotify.x86
chmod 644 ${ASSETSDIR}/syncthing-inotify.x86

echo "Build Complete"
