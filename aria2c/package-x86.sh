#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

ABI="x86"

BASE="$ROOT/aria2c/src/main/jniLibs/$ABI"
ZIP="$BASE/libaria2c.zip.so"
BIN="$BASE/libaria2c.so"
LIBRARY_OUT="$ROOT/library/src/main/jniLibs/$ABI/libaria2.zip.so"

test -f "$ZIP"
test -f "$BIN"

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

unzip -q "$ZIP" -d "$TMP"

mkdir -p "$TMP/usr/bin"

cp "$BIN" "$TMP/usr/bin/aria2c"
chmod 755 "$TMP/usr/bin/aria2c"

rm -f "$TMP/usr/lib/liblzma.so"

rm -f "$ZIP"

(
    cd "$TMP"
    zip -qry "$ZIP" .
)

cp "$ZIP" "$LIBRARY_OUT"

echo "=== aria2 executable ==="
unzip -l "$ZIP" | grep 'usr/bin/aria2c$'

echo
echo "=== bare liblzma check ==="
if unzip -l "$ZIP" | grep -q 'usr/lib/liblzma.so$'; then
    echo "ERROR: bare liblzma.so is present"
    exit 1
else
    echo "OK: bare liblzma.so absent"
fi

echo
echo "=== library copy ==="
sha256sum "$ZIP" "$LIBRARY_OUT"
