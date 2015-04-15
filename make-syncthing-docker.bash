#!/usr/bin/env bash

set -e

MYDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ASSETSDIR=${MYDIR}/app/src/main/assets/

#Update submodule
git submodule update --init syncthing/src/github.com/syncthing/syncthing

#Apply patches
pushd syncthing/src/github.com/syncthing/syncthing
git am -3 ../../../../../patches/*
popd

#Do build
docker run --rm -t -u $(id -u) -v ${MYDIR}/syncthing:/opt/workspace syncthing-build-android

#Copy synching
mv ${MYDIR}/syncthing/src/github.com/syncthing/syncthing/syncthing ${ASSETSDIR}/syncthing.arm
chmod 644 ${ASSETSDIR}/syncthing.arm