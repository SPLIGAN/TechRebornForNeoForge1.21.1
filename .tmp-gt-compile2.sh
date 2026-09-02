#!/bin/bash
set -euo pipefail
python3 - <<'PY'
from pathlib import Path
import shutil
s = Path('/mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1/src/gametest/groovy/techreborn/test/TRGameTestRegistration.groovy')
d = Path('/home/spligan/tr-neoforge-build/src/gametest/groovy/techreborn/test/TRGameTestRegistration.groovy')
shutil.copy2(s, d)
print('synced registration')
PY
cd /home/spligan/tr-neoforge-build
./gradlew :compileGametestJava :compileGametestGroovy --stacktrace
