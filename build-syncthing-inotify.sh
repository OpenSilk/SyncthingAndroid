#!/bin/bash -e
# Make a different copy for x86 and ARM (See https://github.com/golang/go/issues/8161 and https://github.com/golang/go/issues/)
unset GOROOT

# Build the syncthing-inotify library
ORIG=$(pwd)
mkdir -p bin

# Load submodules
if [ ! -e "syncthing/src/github.com/syncthing/syncthing-inotify/.git" ]; then
        git submodule update --init --recursive
fi

# Check for GOLANG installation
if [ -z $GOROOT ] || [[ $(go version) != go\ version\ go1.4* ]] ; then
        mkdir -p "build"
        tmpgo='build/go'
        if [ ! -f "$tmpgo/bin/go" ]; then
                # Download GOLANG v1.4.1
                wget -O go.src.tar.gz http://storage.googleapis.com/golang/go1.4.2.src.tar.gz
                sha1=$(sha1sum go.src.tar.gz)
                if [ "$sha1" != "460caac03379f746c473814a65223397e9c9a2f6  go.src.tar.gz" ]; then
                        echo "go.src.tar.gz SHA1 checksum does not match!"
                        exit 1
                fi
                mkdir -p $tmpgo
                tar -xzf go.src.tar.gz --strip=1 -C $tmpgo
                rm go.src.tar.gz
                # Build GO for host
                pushd $tmpgo/src
                GOARM=5 GO386=387 ./make.bash --no-clean
                popd
        fi
        # Add GO to the environment
        export GOROOT="$(pwd)/$tmpgo"
fi

# Add GO compiler to PATH
export PATH=$GOROOT/bin:$PATH
# Disable CGO (no dynamic linking)
export CGO_ENABLED=0

# Prepare GOLANG to cross-compile for android arm
# GOOS=android not supported by Golang yet
if [ ! -f $GOROOT/bin/linux_arm/go ]; then
        pushd $GOROOT/src
        # Build GO for cross-compilation
        GOOS=linux GOARCH=arm GOARM=5 ./make.bash --no-clean
        popd
fi

# Prepare GOLANG to cross-compile for android i386.
# GOOS=android not supported by Golang yet
if [ ! -f $GOROOT/bin/linux_386/go ]; then
        pushd $GOROOT/src
        # Build GO for cross-compilation
        GOOS=linux GOARCH=386 GO386=387 ./make.bash --no-clean
        popd
fi

# Setup GOPATH
cd "syncthing/"
export GOPATH="$(pwd)"

# Install godep
$GOROOT/bin/go get github.com/tools/godep
export PATH="$(pwd)/bin":$PATH

# Setup syncthing and clean
export ENVIRONMENT=android
pushd src/github.com/syncthing/syncthing-inotify
set +e
go get
set -e

# ARM
# GOOS=android not supported by Golang yet
GOOS=linux GOARCH=arm GOARM=5 go build -ldflags "-w -X main.Version `git describe --abbrev=0 --tags`"
mv syncthing-inotify $ORIG/app/libs/armeabi/libsyncthing-inotify.so

# X86
# GOOS=android not supported by Golang yet
GOOS=linux GOARCH=386 GO386=387 go build -ldflags "-w -X main.Version `git describe --abbrev=0 --tags`"
mv syncthing-inotify $ORIG/app/libs/x86/libsyncthing-inotify.so

popd

