##Syncthing on Android

###Building

Requirements

* I guess you'll need build-essential or base-devel (maybe more)
* Android sdk
* Android ndk set `TOOLCHAIN_ROOT` or update scripts to point to yours

```bash
# Build go for android
./make-go.bash

# Build syncthing for android
./make-syncthing.bash

# You only need to run the above once (or whenever the submodules are updated)

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