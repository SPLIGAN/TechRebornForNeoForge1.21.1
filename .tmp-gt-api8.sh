#!/bin/bash
set -euo pipefail
JAR=/home/spligan/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/26.1.2.73/d58040da3199b79094602e379f5ee79d75d38312/neoforge-26.1.2.73-universal.jar
MC=/home/spligan/.gradle/caches/ng_execute/fb2c561c7bee37230c168bd0676ea68e111807885c776c074309c76f226aaa4c/outputs.jar
echo "=== GameTest related classes in NeoForge ==="
jar tf "$JAR" | grep -i gametest | head -80
echo "=== RegisterGameTests ==="
jar tf "$JAR" | grep -i RegisterGame
echo "=== GameTestHelpers / GameTestHooks ==="
jar tf "$JAR" | grep -iE 'GameTestHook|GameTestHelp|GameTestReg'
echo "===="
# Also check patched jars
find ~/.gradle/caches -name '*neoforge*26.1.2*' -name '*.jar' 2>/dev/null | head -20
