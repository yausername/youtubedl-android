#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
: "${FFMPEG_MAKER_ROOT:?Set FFMPEG_MAKER_ROOT to the ffmpeg-android-maker checkout}"

ABI="x86"

MAKER="$FFMPEG_MAKER_ROOT/build/ffmpeg/$ABI"
BASE="$ROOT/ffmpeg/src/main/jniLibs/$ABI"

ZIP="$BASE/libffmpeg.zip.so"
FFMPEG_OUT="$BASE/libffmpeg.so"
FFPROBE_OUT="$BASE/libffprobe.so"

test -f "$MAKER/bin/ffmpeg"
test -f "$MAKER/bin/ffprobe"

for lib in \
    libavdevice.so \
    libavfilter.so \
    libavformat.so \
    libavcodec.so \
    libswresample.so \
    libswscale.so \
    libavutil.so
do
    test -f "$MAKER/lib/$lib"
done

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

unzip -q "$ZIP" -d "$TMP"

mkdir -p "$TMP/usr/lib"

rm -f \
    "$TMP"/usr/lib/libavdevice.so* \
    "$TMP"/usr/lib/libavfilter.so* \
    "$TMP"/usr/lib/libavformat.so* \
    "$TMP"/usr/lib/libavcodec.so* \
    "$TMP"/usr/lib/libswresample.so* \
    "$TMP"/usr/lib/libswscale.so* \
    "$TMP"/usr/lib/libavutil.so* \
    "$TMP"/usr/lib/libpostproc.so*

cp \
    "$MAKER/lib/libavdevice.so" \
    "$MAKER/lib/libavfilter.so" \
    "$MAKER/lib/libavformat.so" \
    "$MAKER/lib/libavcodec.so" \
    "$MAKER/lib/libswresample.so" \
    "$MAKER/lib/libswscale.so" \
    "$MAKER/lib/libavutil.so" \
    "$TMP/usr/lib/"

rm -f "$TMP/usr/lib/liblzma.so"

cp "$MAKER/bin/ffmpeg" "$FFMPEG_OUT"
cp "$MAKER/bin/ffprobe" "$FFPROBE_OUT"

rm -f "$ZIP"

(
    cd "$TMP"
    zip -qry "$ZIP" .
)

echo "=== ffmpeg executable ==="
file "$FFMPEG_OUT"

echo
echo "=== ffmpeg NEEDED ==="
readelf -d "$FFMPEG_OUT" | grep NEEDED

echo
echo "=== runtime libs ==="
unzip -l "$ZIP" | grep -E \
'usr/lib/lib(avdevice|avfilter|avformat|avcodec|swresample|swscale|avutil)\.so$'

echo
echo "=== bare liblzma check ==="
if unzip -l "$ZIP" | grep -q 'usr/lib/liblzma.so$'; then
    echo "ERROR: bare liblzma.so is present"
    exit 1
else
    echo "OK: bare liblzma.so absent"
fi
