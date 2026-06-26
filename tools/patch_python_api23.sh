#!/usr/bin/env bash
# Make the bundled CPython load on Android 6.0 (API 23) — fixes #304.
#
# Two fixes are applied to each library/src/main/jniLibs/<abi>/libpython.zip.so (and the
# libpython.so loader stub):
#   1. Compile api23_shim.c — it backfills the libc functions Bionic only added at API 24+
#      (lockf/preadv/pwritev on 64-bit; lockf64/preadv64/pwritev64 on 32-bit) — and add it
#      as a DT_NEEDED of libpython, so "cannot locate symbol …" no longer aborts loading.
#   2. Page-pad every .so to a 4096-byte boundary — the strict API-23 linker refuses a final
#      PT_LOAD segment whose file content ends exactly at EOF ("invalid ELF file … load
#      segment past end of file"); newer Bionic zero-fills the trailing page, old does not.
#
# Re-run after updating the bundled python. Idempotent-ish (re-adds NEEDED / re-pads).
#
# Prereqs: Android NDK (ANDROID_NDK_HOME, or ANDROID_HOME/ndk/<latest>), patchelf, zip, unzip.
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/.." && pwd)"
JNI="$ROOT/library/src/main/jniLibs"
NDK="${ANDROID_NDK_HOME:-$(ls -d "${ANDROID_HOME:-$HOME/Android/Sdk}"/ndk/* 2>/dev/null | sort -V | tail -1)}"
TC="$NDK/toolchains/llvm/prebuilt/linux-x86_64/bin"
PATCHELF="${PATCHELF:-patchelf}"
API=23

declare -A CC=(
  [arm64-v8a]=aarch64-linux-android${API}-clang
  [armeabi-v7a]=armv7a-linux-androideabi${API}-clang
  [x86]=i686-linux-android${API}-clang
  [x86_64]=x86_64-linux-android${API}-clang
)

command -v "$PATCHELF" >/dev/null || { echo "ERROR: patchelf not found (set PATCHELF=...)"; exit 1; }
[ -x "$TC/${CC[x86_64]}" ] || { echo "ERROR: NDK clang not found under $TC (set ANDROID_NDK_HOME)"; exit 1; }

tmp="$(mktemp -d)"; trap 'rm -rf "$tmp"' EXIT
for abi in arm64-v8a armeabi-v7a x86 x86_64; do
  zipso="$JNI/$abi/libpython.zip.so"
  [ -f "$zipso" ] || { echo "skip $abi (no libpython.zip.so)"; continue; }
  d="$tmp/$abi"; mkdir -p "$d"; unzip -qo "$zipso" -d "$d"
  lp="$(find "$d/usr/lib" -maxdepth 1 -name 'libpython3*.so.*' | head -1)"
  [ -n "$lp" ] || { echo "ERROR: no libpython3*.so.* in $abi package"; exit 1; }
  "$TC/${CC[$abi]}" -shared -fPIC -O2 -o "$d/usr/lib/libshim.so" "$HERE/api23_shim.c"
  "$PATCHELF" --add-needed libshim.so "$lp"
  while IFS= read -r so; do
    sz=$(stat -c%s "$so"); pad=$(( (4096 - sz % 4096) % 4096 ))
    [ "$pad" -gt 0 ] && head -c "$pad" /dev/zero >> "$so" || true
  done < <(find "$d" -type f -name '*.so*')   # -type f: don't follow/double-pad symlinks
  ( cd "$d" && rm -f "$zipso" && zip --symlinks -qr -X "$zipso" usr )   # --symlinks: keep links as links
  # the small loader stub (libpython.so) also benefits from page-padding
  "$PATCHELF" --remove-rpath "$JNI/$abi/libpython.so" 2>/dev/null || true
  sz=$(stat -c%s "$JNI/$abi/libpython.so"); pad=$(( (4096 - sz % 4096) % 4096 ))
  [ "$pad" -gt 0 ] && head -c "$pad" /dev/zero >> "$JNI/$abi/libpython.so" || true
  echo "patched $abi ($(basename "$lp"))"
done
echo "Done. Rebuild the AAR — the bundled python now loads on API 23."
