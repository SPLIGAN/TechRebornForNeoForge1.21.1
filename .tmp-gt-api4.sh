#!/usr/bin/env bash
set -euo pipefail
SRC=/home/spligan/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/26.1.2.73/5596011a67b8716d928cf7897d140680b0626606/neoforge-26.1.2.73-sources.jar
rm -rf /tmp/nf-gt
mkdir -p /tmp/nf-gt
cd /tmp/nf-gt
jar xf "$SRC"
grep -RIn --include='*.java' 'RegisterGameTestsEvent\|FunctionGameTestInstance\|registerTest(' . 2>/dev/null | head -40
echo ====
CP=/home/spligan/.gradle/caches/ng_execute/be457848d217ea16a927b2a8f98201d5f8f6012cdd4314df567be19d9a59c551/classes
javap -public -classpath "$CP" net.minecraft.gametest.framework.BlockBasedTestInstance
echo ====
javap -public -classpath "$CP" net.minecraft.gametest.framework.GeneratedTest
echo ====
# getBlockEntity overloads
javap -classpath "$CP" net.minecraft.gametest.framework.GameTestHelper | grep getBlockEntity
# wait methods aliases
javap -classpath "$CP" net.minecraft.gametest.framework.GameTestHelper | grep -E 'runAfter|runAt|succeedOn'
