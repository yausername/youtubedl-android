#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

: "${PYTHON_BUNDLE_SRC:?Set PYTHON_BUNDLE_SRC to the prepared Python runtime bundle directory}"
SRC="$PYTHON_BUNDLE_SRC"
OUT="$ROOT/library/src/main/jniLibs/armeabi-v7a/libpython.zip.so"

test -f "$SRC/usr/lib/libpython3.14.so"
test -f "$SRC/usr/lib/libz.so.1"
test -f "$SRC/usr/lib/libssl.so.3"
test -f "$SRC/usr/lib/libcrypto.so.3"
test -f "$SRC/usr/lib/libandroid-support.so"
test -f "$SRC/usr/lib/libc++_shared.so"
test -f "$SRC/usr/lib/python3.14/site-packages/certifi/cacert.pem"

rm -f "$SRC/usr/lib/liblzma.so"
rm -f "$OUT"

cd "$SRC"
zip -yr "$OUT" .

echo
echo "=== certifi ==="
unzip -l "$OUT" | grep 'certifi/cacert.pem'

echo
echo "=== required runtime libs ==="
unzip -l "$OUT" | grep -E \
'usr/lib/(libpython3\.14\.so|libz\.so\.1|libssl\.so\.3|libcrypto\.so\.3|libandroid-support\.so|libc\+\+_shared\.so)$'

echo
echo "=== bare liblzma check ==="
if unzip -l "$OUT" | grep -q 'usr/lib/liblzma.so$'; then
    echo "ERROR: bare liblzma.so is still present"
    exit 1
else
    echo "OK: bare liblzma.so absent"
fi

echo
sha256sum "$OUT"
