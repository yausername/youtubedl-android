#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
: "${ANDROID_NDK_ROOT:?Set ANDROID_NDK_ROOT to the Android NDK checkout}"
: "${PYTHON_TARGET_ROOT:?Set PYTHON_TARGET_ROOT to the extracted Termux Python usr root}"
: "${PYTHON_LAUNCHER_SRC:?Set PYTHON_LAUNCHER_SRC to python314-armv7-launcher.c}"

NDK="$ANDROID_NDK_ROOT"
TOOLCHAIN="$NDK/toolchains/llvm/prebuilt/linux-x86_64"
TARGET="$PYTHON_TARGET_ROOT"
OUT="$ROOT/library/src/main/jniLibs/armeabi-v7a/libpython.so"
SRC="$PYTHON_LAUNCHER_SRC"

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
