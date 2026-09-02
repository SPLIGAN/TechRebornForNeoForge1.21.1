#!/bin/bash
set -euo pipefail
SRC=/mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1
DST=/home/spligan/tr-neoforge-build

# Prefer python copy to avoid chmod issues on /mnt/c
python3 - <<PY
import shutil
from pathlib import Path
src = Path("$SRC")
dst = Path("$DST")
paths = [
  "build.gradle",
  "RebornCore/build.gradle",
  "src/main/java/techreborn/TechRebornNeoForge.java",
  "src/gametest",
  "AGENTS.md",
  "docs/NEOFORGE_26.1.2_MIGRATION_SPEC.md",
]
for rel in paths:
  s = src / rel
  d = dst / rel
  if s.is_dir():
    if d.exists():
      shutil.rmtree(d)
    shutil.copytree(s, d)
  else:
    d.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(s, d)
  print("synced", rel)
PY

cd "$DST"
chmod +x gradlew
./gradlew :compileGametestJava :compileGametestGroovy --stacktrace
