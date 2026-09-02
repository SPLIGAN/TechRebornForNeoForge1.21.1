#!/usr/bin/env bash
set -euo pipefail
cd /mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
java -version
python3 - <<'PY'
from pathlib import Path
data = Path("gradlew").read_bytes().replace(b"\r\n", b"\n").replace(b"\r", b"")
Path("/tmp/tr-gradlew").write_bytes(data)
PY
chmod +x /tmp/tr-gradlew
bash /tmp/tr-gradlew :RebornCore:compileJava compileJava --stacktrace
