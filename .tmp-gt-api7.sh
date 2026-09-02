#!/bin/bash
set -euo pipefail
JAR=$(find ~/.gradle/caches -name 'neoforge-*-universal.jar' 2>/dev/null | head -1)
MC=$(find ~/.gradle/caches/ng_execute -path '*/minecraft-*-client.jar' 2>/dev/null | head -1)
# Prefer joined/server mapped jar
for c in $(find ~/.gradle/caches/neoform* ~/.gradle/caches/ng_execute -name '*.jar' 2>/dev/null | head -200); do
  if jar tf "$c" 2>/dev/null | grep -q 'net/minecraft/gametest/framework/FunctionGameTestInstance.class'; then
    MC="$c"; break
  fi
done
echo "MC=$MC"
echo "NF=$JAR"
javap -classpath "$MC:$JAR" -public net.minecraft.gametest.framework.FunctionGameTestInstance 2>/dev/null || true
echo ====
javap -classpath "$MC:$JAR" -public net.minecraft.gametest.framework.TestData 2>/dev/null | head -80
echo ====
javap -classpath "$MC:$JAR" -public net.neoforged.neoforge.gametest.RegisterGameTestsEvent 2>/dev/null || \
javap -classpath "$JAR" -public net.neoforged.neoforge.gametest.RegisterGameTestsEvent 2>/dev/null || \
find ~/.gradle/caches -name '*neoforge*.jar' 2>/dev/null | while read j; do
  if jar tf "$j" 2>/dev/null | grep -q RegisterGameTestsEvent; then
    echo "found in $j"; javap -classpath "$j" -public net.neoforged.neoforge.gametest.RegisterGameTestsEvent; break
  fi
done
echo ====
javap -classpath "$MC:$JAR" -public net.minecraft.gametest.framework.GameTestHelper 2>/dev/null | head -100
