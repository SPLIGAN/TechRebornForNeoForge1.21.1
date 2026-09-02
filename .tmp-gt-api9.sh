#!/bin/bash
set -euo pipefail
SRC=/home/spligan/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/26.1.2.73/5596011a67b8716d928cf7897d140680b0626606/neoforge-26.1.2.73-sources.jar
JAR=/home/spligan/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/26.1.2.73/d58040da3199b79094602e379f5ee79d75d38312/neoforge-26.1.2.73-universal.jar
MC=/home/spligan/.gradle/caches/ng_execute/fb2c561c7bee37230c168bd0676ea68e111807885c776c074309c76f226aaa4c/outputs.jar
cd /tmp
jar xf "$SRC" net/neoforged/neoforge/event/RegisterGameTestsEvent.java
cat net/neoforged/neoforge/event/RegisterGameTestsEvent.java
echo ====
javap -classpath "$MC:$JAR" -public net.minecraft.gametest.framework.GameTestHelper | head -120
echo ====
# Find default environment and BuiltinRegistries TEST_FUNCTION
javap -classpath "$MC" -c net.minecraft.core.registries.BuiltInRegistries 2>/dev/null | grep -i TEST | head -20
javap -classpath "$MC" -public net.minecraft.core.registries.Registries 2>/dev/null | grep -i TEST
echo ====
# How FunctionGameTestInstance resolves function
javap -classpath "$MC" -c -p net.minecraft.gametest.framework.FunctionGameTestInstance 2>/dev/null | head -80
