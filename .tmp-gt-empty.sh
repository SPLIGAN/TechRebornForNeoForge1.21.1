#!/bin/bash
set -euo pipefail
EMPTY=$(find ~/.gradle/caches/ng_execute -path '*/structure/empty.nbt' 2>/dev/null | head -1)
echo "EMPTY=$EMPTY"
python3 - <<PY
import gzip
from pathlib import Path
p = Path("$EMPTY")
data = gzip.open(p, "rb").read()
print("bytes", len(data))
# find TAG_List 'size' with 3 ints
idx = data.find(b"size")
print("idx", idx)
print(data[idx:idx+60])
PY
# Also check fabric empty if any in TR sources
find /home/spligan/tr-neoforge-build -name '*.nbt' 2>/dev/null | head
ls /mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1/src/gametest/resources 2>/dev/null || true
