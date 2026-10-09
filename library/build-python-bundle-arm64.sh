#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

ABI="arm64-v8a"

: "${PYTHON_BUNDLE_SRC:?Set PYTHON_BUNDLE_SRC to the prepared Python runtime bundle directory}"
SRC="$PYTHON_BUNDLE_SRC"
OUT="$ROOT/library/src/main/jniLibs/$ABI/libpython.zip.so"

test -f "$SRC/usr/lib/libpython3.14.so"
test -f "$SRC/usr/lib/libandroid-support.so"
test -f "$SRC/usr/lib/libc++_shared.so"
test -e "$SRC/usr/lib/libz.so.1"
test -f "$SRC/usr/lib/libssl.so.3"
test -f "$SRC/usr/lib/libcrypto.so.3"
test -f "$SRC/usr/lib/python3.14/site-packages/certifi/cacert.pem"

rm -f "$SRC/usr/lib/liblzma.so"
rm -f "$OUT"

(
    cd "$SRC"
    zip -qry "$OUT" .
)

echo "=== required files ==="

unzip -l "$OUT" | grep 'usr/lib/libpython3.14.so$'
unzip -l "$OUT" | grep 'usr/lib/libandroid-support.so$'
unzip -l "$OUT" | grep 'usr/lib/libc++_shared.so$'
unzip -l "$OUT" | grep 'usr/lib/libz.so.1$'
unzip -l "$OUT" | grep 'usr/lib/libssl.so.3$'
unzip -l "$OUT" | grep 'usr/lib/libcrypto.so.3$'
unzip -l "$OUT" | grep 'usr/lib/python3.14/site-packages/certifi/cacert.pem$'

echo
echo "=== bare liblzma check ==="

if unzip -l "$OUT" | grep -q 'usr/lib/liblzma.so$'; then
    echo "ERROR: bare liblzma.so is present"
    exit 1
else
    echo "OK: bare liblzma.so absent"
fi

echo
echo "=== SHA256 ==="
sha256sum "$OUT"
