#!/bin/bash

export GOROOT_BOOTSTRAP=/usr/local/opt/go/libexec
export ANDROID_NDK=/Users/$(whoami)/Library/Android/sdk/ndk-bundle
./make-all.bash
