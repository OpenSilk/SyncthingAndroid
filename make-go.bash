#!/usr/bin/env bash

set -e

RESET=1

MYDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ -z "$ANDROID_NDK" ]; then
    ANDROID_NDK=/opt/android/ndk/android-ndk-r10e
fi

if [ -z "$GOROOT_BOOTSTRAP" ]; then
    export GOROOT_BOOTSTRAP=/usr/lib/go
fi

case "$1" in
    arm)
        TOOLCHAIN_ROOT="${MYDIR}"/golang/build/toolchains/arm
        TOOLCHAIN_PLATFORM=16
        TOOLCHAIN_ARCH=arm
        export CC_FOR_TARGET="${TOOLCHAIN_ROOT}"/bin/arm-linux-androideabi-gcc
        export CXX_FOR_TARGET="${TOOLCHAIN_ROOT}"/bin/arm-linux-androideabi-g++
        export CGO_ENABLED=1
        export GOOS=android
        export GOARCH=arm
        export GOARM=7
        ;;
    386)
        TOOLCHAIN_ROOT="${MYDIR}"/golang/build/toolchains/386
        TOOLCHAIN_PLATFORM=16
        TOOLCHAIN_ARCH=x86
        export CC_FOR_TARGET="${TOOLCHAIN_ROOT}"/bin/i686-linux-android-gcc
        export CXX_FOR_TARGET="${TOOLCHAIN_ROOT}"/bin/i686-linux-android-g++
        export CGO_ENABLED=1
        export GOOS=android
        export GOARCH=386
        export GO386=387
        ;;
    amd64)
        TOOLCHAIN_ROOT="${MYDIR}"/golang/build/toolchains/amd64
        TOOLCHAIN_PLATFORM=21
        TOOLCHAIN_ARCH=x86_64
        export CC_FOR_TARGET="${TOOLCHAIN_ROOT}"/bin/x86_64-linux-android-gcc
        export CXX_FOR_TARGET="${TOOLCHAIN_ROOT}"/bin/x86_64-linux-android-g++
        export CGO_ENABLED=1
        export GOOS=android
        export GOARCH=amd64
        ;;
    *)
        echo "Must specify either arm or 386 or amd64"
        exit 1
esac

if [ -d "${TOOLCHAIN_ROOT}" ]; then
    rm -r "${TOOLCHAIN_ROOT}"
fi
mkdir -p "${TOOLCHAIN_ROOT}"
"${ANDROID_NDK}"/build/tools/make-standalone-toolchain.sh --platform=android-${TOOLCHAIN_PLATFORM} \
        --arch=${TOOLCHAIN_ARCH}  --install-dir="${TOOLCHAIN_ROOT}"


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

set +e
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
    git clean -fd
    popd
fi

popd

if [ $RESET -eq 1 ]; then
    git submodule update --init golang/go
fi

echo "Complete"
