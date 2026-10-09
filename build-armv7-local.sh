#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

cd "$ROOT"

: "${JAVA_HOME:?Set JAVA_HOME to a Java 17 JDK}"
: "${ANDROID_NDK_ROOT:?Set ANDROID_NDK_ROOT to the Android NDK checkout}"
: "${PYTHON_TARGET_ROOT:?Set PYTHON_TARGET_ROOT to the extracted Termux Python usr root}"
: "${PYTHON_LAUNCHER_SRC:?Set PYTHON_LAUNCHER_SRC to python314-armv7-launcher.c}"
: "${PYTHON_BUNDLE_SRC:?Set PYTHON_BUNDLE_SRC to the prepared ARMv7 Python runtime bundle}"
: "${FFMPEG_MAKER_ROOT:?Set FFMPEG_MAKER_ROOT to the ffmpeg-android-maker checkout}"

export PATH="$JAVA_HOME/bin:$PATH"

echo "=== Python launcher ==="
./library/build-python-launcher-armv7.sh

echo
echo "=== Python bundle ==="
./library/build-python-bundle-armv7.sh

echo
echo "=== aria2 package ==="
./aria2c/package-armv7.sh

echo
echo "=== FFmpeg package ==="
./ffmpeg/package-armv7.sh

echo
echo "=== Build library AAR ==="
./gradlew :library:clean :library:assembleRelease --rerun-tasks

echo
echo "=== Build FFmpeg AAR ==="
./gradlew :ffmpeg:clean :ffmpeg:assembleRelease --rerun-tasks

echo
echo "Done."
