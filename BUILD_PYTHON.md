
## Python for Android

Python can be built for android using the termux python package.

Prerequisites : git, docker

    git clone git@github.com:termux/termux-packages.git
    cd termux-packages

create a file `build-python.sh` with below content

    #!/bin/bash
    # use i686 for x86
    export TERMUX_ARCH=arm
    export TERMUX_PREFIX=/data/youtubedl-android/usr
    export TERMUX_ANDROID_HOME=/data/youtubedl-android/home
    ./build-package.sh python

Make file executable

    chmod +x ./build-python.sh

Build Package

    ./scripts/run-docker.sh ./clean.sh
    ./scripts/run-docker.sh ./build-python.sh

This will create several `.deb` files in `debs/` directory.  
I have found the following packages to be sufficient for youtube-dl to work.

    python_3.7.2-1_arm.deb
    libandroid-support_24_arm.deb
    libutil_0.4_arm.deb
    libffi_3.2.1-2_arm.deb
    openssl_1.1.1a_arm.deb
    ca-certificates_20180124_all.deb

The python zip archive as used in youtubedl-android can be created using the following commands.

    cd debs
    dpkg-deb -xv python_3.7.2-1_arm.deb .
    dpkg-deb -xv libandroid-support_24_arm.deb .
    dpkg-deb -xv libutil_0.4_arm.deb .
    dpkg-deb -xv libffi_3.2.1-2_arm.deb .
    dpkg-deb -xv openssl_1.1.1a_arm.deb .
    dpkg-deb -xv ca-certificates_20180124_all.deb .
    cd data/youtubedl-android
    zip --symlinks -r /tmp/python3_7_arm.zip usr/lib usr/etc


## add mutagen

https://github.com/yt-dlp/yt-dlp#dependencies

To build or add `mutagen` for embedding thumbnail go to [mutagen](https://github.com/quodlibet/mutagen) then download the latest version

Unzip `mutagen-1.45.1.tar.gz` then you will find many files and folders, Delete all files and folders except the `mutagen` folder

Rename `libpython.zip.so` to `libpython.zip` then open it and add mutagen folder to the following path `/usr/lib/python3.8/site-packages/` when finished rename `libpython.zip` to `libpython.zip.so`