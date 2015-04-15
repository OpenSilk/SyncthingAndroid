##Syncthing on Android

###Building

Requirements

* You'll need build-essential or base-devel
* Android ndk set `TOOLCHAIN_ROOT` or update scripts to point to yours

```bash
# You only need to run these once (or whenever the submodules are updated)

# Build go for android
./make-go.bash
# Build syncthing for android
./make-syncthing.bash
```

Alternatively use docker

```bash
#Make the image (only need to run once)
./make-docker-image.bash
#Build syncthing (only need to run once or whenever the submodules are updated)
./make-syncthing-docker.bash
```

```bash
# Build apk
./gradlew assembleDebug
```

###Contributing

Project is managed through gerrit `review.opensilk.org`

Easy way

```bash
mkdir OpenSilk && cd OpenSilk
repo init -u https://github.com/OpenSilk/Manifest.git
repo sync
cd SyncthingAndroid
#make changes
repo upload .
```

Hard way
[git push](https://gerrit-review.googlesource.com/Documentation/user-upload.html#_git_push)

Pull requests are ignored