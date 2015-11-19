#!/usr/bin/env bash

set -e

MYDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

${MYDIR}/make-go.bash arm
${MYDIR}/make-go.bash 386
${MYDIR}/make-go.bash amd64

${MYDIR}/make-syncthing.bash arm
${MYDIR}/make-syncthing.bash 386
${MYDIR}/make-syncthing.bash amd64