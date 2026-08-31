#!/usr/bin/env bash
set -euo pipefail

ROOT="$HOME/Downloads/dev/yausernameYTDLP"

cd "$ROOT"

export JAVA_HOME=/usr/lib/jvm/java-17-temurin-jdk
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
echo "=== Copy AARs into Fermux ==="
cp \
  "$ROOT/library/build/outputs/aar/library-release.aar" \
  "$HOME/CodeProjects/app/libs/youtubedl-android-local.aar"

cp \
  "$ROOT/ffmpeg/build/outputs/aar/ffmpeg-release.aar" \
  "$HOME/CodeProjects/app/libs/ffmpeg-android-local.aar"

echo
echo "Done."
