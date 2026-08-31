#!/usr/bin/env bash
set -euo pipefail

NDK="$HOME/lib/android-ndk-r29"
TOOLCHAIN="$NDK/toolchains/llvm/prebuilt/linux-x86_64"
TARGET="$HOME/Downloads/dev/python314-armv7-root/data/data/com.termux/files/usr"
OUT="$HOME/Downloads/dev/yausernameYTDLP/library/src/main/jniLibs/armeabi-v7a/libpython.so"
SRC="$HOME/Downloads/dev/python314-armv7-launcher.c"

"$TOOLCHAIN/bin/armv7a-linux-androideabi24-clang" \
  "$SRC" \
  -I"$TARGET/include/python3.14" \
  -L"$TARGET/lib" \
  -Wl,-rpath,'$ORIGIN/../packages/python/usr/lib' \
  -lpython3.14 \
  -landroid-support \
  -o "$OUT"

file "$OUT"
readelf -d "$OUT" | grep NEEDED
