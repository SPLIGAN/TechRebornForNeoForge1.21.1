#!/bin/bash
set -euo pipefail
MC=/home/spligan/.gradle/caches/ng_execute/fb2c561c7bee37230c168bd0676ea68e111807885c776c074309c76f226aaa4c/outputs.jar
javap -classpath "$MC" -public net.minecraft.gametest.framework.GameTestHelper | grep -iE 'succeed|fail|runAfter|runAt|getBlockEntity|assertTrue|assertValue'
echo ====
javap -classpath "$MC" -public net.minecraft.gametest.framework.TestEnvironmentDefinition\$AllOf 2>/dev/null | head -20
javap -classpath "$MC" -public 'net.minecraft.gametest.framework.TestEnvironmentDefinition$AllOf' 2>/dev/null | head -30
echo ====
# empty structure size?
python3 - <<'PY'
import struct,gzip,sys
from pathlib import Path
p=Path('/home/spligan/.gradle/caches/ng_execute/1e75bb06b156e30858ebb6e86d61c54216f166856f2b6cbef573e7344602483a/transformed/data/minecraft/structure/empty.nbt')
# snbt? it's gzip nbt
import gzip
data=gzip.open(p,'rb').read()
print('len',len(data), data[:40])
PY
