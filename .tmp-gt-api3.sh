#!/usr/bin/env bash
set -euo pipefail
CP=/home/spligan/.gradle/caches/ng_execute/be457848d217ea16a927b2a8f98201d5f8f6012cdd4314df567be19d9a59c551/classes
javap -public -classpath "$CP" net.minecraft.gametest.framework.FunctionGameTestInstance
echo ====
javap -public -classpath "$CP" net.minecraft.gametest.framework.TestData
echo ====
javap -classpath "$CP" net.minecraft.gametest.framework.GameTestHelper | grep -iE 'wait|runAt|succeed|fail|finalTask|runAfter' | head -40
echo ====
javap -public -classpath "$CP" net.neoforged.neoforge.common.extensions.GameTestHelperExtension 2>&1 | head -40
echo ====
# GameTestEnvironments
javap -public -classpath "$CP" net.minecraft.gametest.framework.GameTestEnvironments 2>&1 | head -40
# look for empty structure
find /home/spligan/tr-neoforge-build -path '*gametest*' -name '*.nbt' 2>/dev/null | head -10
find "$CP/../.." -name 'empty.nbt' 2>/dev/null | head -5
