##Syncthing on Android

This app aims to be a full featured [Syncthing](https://syncthing.net/) client comparable to the web ui.

Secondary objectives include:

* Showcase real world usage of:
  * [Mortar](https://github.com/square/mortar) + [Dagger2](https://github.com/google/dagger)
  * [RxJava](https://github.com/ReactiveX/RxJava) + [Retrolamba](https://github.com/orfjackal/retrolambda)
  * The new [Data Binding Library](https://developer.android.com/tools/data-binding/guide.html)
* CGO support for Syncthing on android.

###Custom Settings

Syncthing runs with a number of custom settings to optimise your experience and to avoid conflicts with other applications (e.g. [Syncthing for Android application](https://github.com/syncthing/syncthing-android)). In particular:
  - Default GUI address is set to 127.0.0.1:8385 (TLS on)
  - Default Rescan Interval is set to 86400 seconds (1 day)
  - Default Ignore Permissions are enabled
  - Default folder is set to Environment.DIRECTORY_DCIM

###Building

#####Syncthing binary

Requirements

* You'll need build-essential or base-devel (including i386 support)

```bash
sudo apt-get install libc6:i386 libstdc++6:i386 lib32z1 libsdl1.2debian:i386
```

* Android sdk. Set `ANDROID_HOME`

```bash
export ANDROID_HOME=/opt/android/sdk
# Install Android SDK Tools
# Install Android SDK Platform-tools
# Install Android SDK Build-tools, revision 22.0.1
# Install Android Support Library, revision 22.1
# Install Android Support Repository, revision 13
./android list sdk --all
./android update sdk --all -t "<selected nr1,nr2,...>" -u
```

* Android ndk. Set `ANDROID_NDK` to root of ndk install

* Building Syncthing

```bash
# You only need to run these once (or whenever the submodules are updated)

# You must have go 1.5+ installed on the host to bootstrap the cross compilation
# the scripts expect the install to be at /usr/lib/go if yours differs you can
# set GOROOT_BOOTSTRAP to the proper location

# If ANDROID_NDK is not set it defaults to /opt/android/ndk/android-ndk-r10e

# This will build go, syncthing, and syncthing-inotify for arm/x86/amd64
# Binaries will be installed to app/src/main/assets
./make-all.bash

# OR If you are building for your self and know what arch you want.

# Build for android/arm
./make-go.bash arm
./make-syncthing.bash arm
# Build for android/386 (x86)
./make-go.bash 386
./make-syncthing.bash 386
# Build for android/amd64 (x86_64) (only useful for emulator right now)
./make-go.bash amd64
./make-syncthing.bash amd64

```

Alternatively use docker (This is broken)

```bash
#Make the image (only need to run once)
./make-docker-image.bash
#Build syncthing (only need to run once or whenever the submodules are updated)
./make-syncthing-docker.bash
```

#####App

```bash
# Build apk
./gradlew app:assembleDebug
```

###Contributing

Project is managed through gerrit `review.opensilk.org`

Easy way

```bash
mkdir OpenSilk && cd OpenSilk
repo init -u https://github.com/OpenSilk/Manifest.git
repo sync
cd SyncthingAndroid
repo start mybranch .
#make changes
repo upload .
```

Hard way
[git push](https://gerrit-review.googlesource.com/Documentation/user-upload.html#_git_push)

Pull requests are ignored
