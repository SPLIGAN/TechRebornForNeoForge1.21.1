#!/usr/bin/env bash
set -euo pipefail
CP=/home/spligan/.gradle/caches/ng_execute/be457848d217ea16a927b2a8f98201d5f8f6012cdd4314df567be19d9a59c551/classes
javap -public -classpath "$CP" net.minecraft.gametest.framework.GameTestHelper | head -100
echo ====
find "$CP" -name 'GameTestInstance.class' | head -5
find "$CP" -name 'GameTest.class' | head -10
find "$CP" -name 'TestData.class' | head -5
javap -public -classpath "$CP" net.minecraft.gametest.framework.GameTestInstance 2>&1 | head -40
echo ====
# extract GameTestHooks
SRC=/home/spligan/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/26.1.2.73/5596011a67b8716d928cf7897d140680b0626606/neoforge-26.1.2.73-sources.jar
cd /tmp && jar xf "$SRC" net/neoforged/neoforge/gametest/GameTestHooks.java
cat net/neoforged/neoforge/gametest/GameTestHooks.java
echo ====
# look for ConsumerGameTestInstance or FunctionGameTest
find "$CP/net/minecraft/gametest" -name '*.class' | sed 's|.*/||' | sort | head -60
