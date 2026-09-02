#!/bin/bash
set -euo pipefail
MC=/home/spligan/.gradle/caches/ng_execute/fb2c561c7bee37230c168bd0676ea68e111807885c776c074309c76f226aaa4c/outputs.jar
javap -classpath "$MC" -public net.minecraft.gametest.framework.GameTestHelper > /tmp/gth.txt
grep -iE 'succeed|fail|runAfter|runAt|getBlockEntity|assertTrue' /tmp/gth.txt || true
echo ====
javap -classpath "$MC" -public 'net.minecraft.gametest.framework.TestEnvironmentDefinition$AllOf' | head -40
