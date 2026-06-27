#!/usr/bin/env bash
# Make youtubedl-android's bundled native packages (python, ffmpeg, aria2c) load on
# Android 6.0 (API 23) — fixes #304 and extends it to ffmpeg/aria2c.
#
# For each module + ABI:
#   1. Compile api23_shim.c (backfills libc functions Bionic only added at API 24+:
#      lockf/preadv/pwritev [+ *64 on 32-bit] for python; getifaddrs/freeifaddrs for
#      ffmpeg/aria2c) and add it as a DT_NEEDED of the package.
#   2. Page-pad every .so to a 4096-byte boundary (the strict API-23 linker refuses a final
#      LOAD segment whose contents end exactly at EOF; newer Bionic zero-fills, old doesn't).
# ffmpeg additionally gets libc++_shared.so bundled (its C++ libs need it; aria2c already ships it).
#
# Prereqs: Android NDK (ANDROID_NDK_HOME or ANDROID_HOME/ndk/<v>), patchelf, zip, unzip.
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"; ROOT="$(cd "$HERE/.." && pwd)"
NDK="${ANDROID_NDK_HOME:-$(ls -d "${ANDROID_HOME:-$HOME/Android/Sdk}"/ndk/* 2>/dev/null | sort -V | tail -1)}"
TC="$NDK/toolchains/llvm/prebuilt/linux-x86_64/bin"
SYSLIB="$NDK/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/lib"
PATCHELF="${PATCHELF:-patchelf}"
declare -A CC=( [arm64-v8a]=aarch64-linux-android23-clang [armeabi-v7a]=armv7a-linux-androideabi23-clang [x86]=i686-linux-android23-clang [x86_64]=x86_64-linux-android23-clang )
declare -A CXX=( [arm64-v8a]=aarch64-linux-android [armeabi-v7a]=arm-linux-androideabi [x86]=i686-linux-android [x86_64]=x86_64-linux-android )
command -v "$PATCHELF" >/dev/null || { echo "ERROR: patchelf not found (set PATCHELF=)"; exit 1; }
[ -x "$TC/${CC[x86_64]}" ] || { echo "ERROR: NDK clang not found (set ANDROID_NDK_HOME)"; exit 1; }

pad_one(){ local f="$1" sz; sz=$(stat -c%s "$f"); local p=$(( (4096 - sz % 4096) % 4096 )); [ "$p" -gt 0 ] && head -c "$p" /dev/zero >> "$f" || true; }

# $1=module dir  $2=zip name  $3=loader stubs (space-sep)  $4=bundle libc++ (yes/no)
patch_module(){
  local jnidir="$ROOT/$1/src/main/jniLibs" zipname="$2" loaders="$3" addcxx="$4"
  for abi in arm64-v8a armeabi-v7a x86 x86_64; do
    local zip="$jnidir/$abi/$zipname"; [ -f "$zip" ] || continue
    local d; d="$(mktemp -d)"; unzip -qo "$zip" -d "$d"
    "$TC/${CC[$abi]}" -shared -fPIC -O2 -o "$d/usr/lib/libshim.so" "$HERE/api23_shim.c"
    [ "$addcxx" = yes ] && cp "$SYSLIB/${CXX[$abi]}/libc++_shared.so" "$d/usr/lib/libc++_shared.so"
    local mainlib; mainlib="$(find "$d/usr/lib" -maxdepth 1 -name 'libpython3*.so.*' | head -1 || true)"
    [ -n "$mainlib" ] && "$PATCHELF" --add-needed libshim.so "$mainlib"
    while IFS= read -r so; do pad_one "$so"; done < <(find "$d" -type f -name '*.so*')
    ( cd "$d" && rm -f "$zip" && zip --symlinks -qr -X "$zip" usr ); rm -rf "$d"
    for ldr in $loaders; do
      local lp="$jnidir/$abi/$ldr"; [ -f "$lp" ] || continue
      "$PATCHELF" --remove-rpath "$lp" 2>/dev/null || true
      "$PATCHELF" --add-needed libshim.so "$lp"
      pad_one "$lp"
    done
    echo "  patched $1 / $abi"
  done
}

patch_module library libpython.zip.so "libpython.so"               no
patch_module ffmpeg  libffmpeg.zip.so "libffmpeg.so libffprobe.so" yes
patch_module aria2c  libaria2c.zip.so "libaria2c.so"               no
echo "Done. Rebuild the AARs — all bundled native packages now load on API 23."
