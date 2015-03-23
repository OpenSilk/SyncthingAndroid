#!/bin/bash

set -e
MYDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

git submodule update --init # --depth 1

pushd syncthing/src/github.com/syncthing/syncthing

export CGO_ENABLED=0
export ENVIRONMENT=android

go run build.go clean
go run build.go -goos linux -goarch arm -no-upgrade build

mv syncthing ${MYDIR}/app/src/main/assets/syncthing.arm
#git clean -f

popd