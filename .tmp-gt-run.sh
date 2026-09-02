#!/bin/bash
set -euo pipefail
python3 - <<'PY'
from pathlib import Path
import shutil
src = Path('/mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1')
dst = Path('/home/spligan/tr-neoforge-build')
for rel in [
  'src/gametest',
  'src/main/java/techreborn/TechRebornNeoForge.java',
  'build.gradle',
  'RebornCore/build.gradle',
]:
  s, d = src/rel, dst/rel
  if s.is_dir():
    if d.exists():
      shutil.rmtree(d)
    shutil.copytree(s, d)
  else:
    d.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(s, d)
  print('synced', rel)
PY

cd /home/spligan/tr-neoforge-build
# Ensure eula for game test server if needed
mkdir -p RebornCore/run/gameTestServer
if [ ! -f RebornCore/run/gameTestServer/eula.txt ]; then
  echo 'eula=true' > RebornCore/run/gameTestServer/eula.txt
fi

./gradlew :RebornCore:runGameTestServer --stacktrace 2>&1 | tee /tmp/gametest-run.log
echo EXIT:$?
# Summarize results
grep -iE 'tests? (failed|passed|succeeded)|GameTest|FAILED|PASSED|All tests|techreborn:' /tmp/gametest-run.log | tail -80
