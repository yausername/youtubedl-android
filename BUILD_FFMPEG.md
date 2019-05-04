
## FFmpeg for Android

FFmpeg can be built for android using the termux ffmpeg package.

Prerequisites : git, docker

    git clone git@github.com:termux/termux-packages.git
    cd termux-packages
    
create a file `build-ffmpeg.sh` with below content

    #!/bin/bash
    export TERMUX_ARCH=arm
    export TERMUX_PREFIX=/data/youtubedl-android/usr
    export TERMUX_ANDROID_HOME=/data/youtubedl-android/home
    ./build-package.sh ffmpeg
    
Make file executable

    chmod +x ./build-ffmpeg.sh
    
Build Package

    ./scripts/run-docker.sh ./build-ffmpeg.sh
    
This will create several `.deb` files in `debs/` directory.
`debs/*dev*.deb` debs can be safely removed as we don't need them.


The ffmpeg zip archive as used in youtubedl-android can be created using the following commands.

    cd debs
    find . -type f -exec dpkg-deb -xv {} . \;
    cd data/youtubedl-android
    # fix broken symlinks if exists in usr/bin/
    rm -rf usr/share/man
    zip -r /tmp/ffmpeg_arm.zip usr/
    
