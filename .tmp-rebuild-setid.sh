#!/bin/bash
set -euo pipefail
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
TR_WIN=/mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1
TR=$HOME/tr-neoforge-build
# sync critical sources
rsync -a --delete \
  "$TR_WIN/src/main/java/techreborn/blocks/" "$TR/src/main/java/techreborn/blocks/"
rsync -a \
  "$TR_WIN/src/main/java/techreborn/init/TRBlockSettings.java" \
  "$TR_WIN/src/main/java/techreborn/init/TRContent.java" \
  "$TR_WIN/src/main/java/techreborn/init/ModFluids.java" \
  "$TR/src/main/java/techreborn/init/"
rsync -a \
  "$TR_WIN/src/main/java/techreborn/events/ModRegistry.java" \
  "$TR/src/main/java/techreborn/events/"
# remove REI service if present
rm -f "$TR/src/main/resources/META-INF/services/me.shedaniel.rei.api.client.plugins.REIClientPlugin"
# restore gradlew
cp "$TR_WIN/gradlew" "$TR/gradlew"
python3 - <<'PY'
from pathlib import Path
p = Path.home()/'tr-neoforge-build'/'gradlew'
data = p.read_bytes().replace(b'\r\n', b'\n').replace(b'\r', b'\n')
assert b'dirname' in data
p.write_bytes(data); p.chmod(0o755)
PY
cd "$TR"
bash ./gradlew compileJava --stacktrace --no-daemon 2>&1 | tee /tmp/tr-compile.log | tail -n 80
