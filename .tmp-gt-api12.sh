#!/bin/bash
set -euo pipefail
SRC=/home/spligan/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/26.1.2.73/5596011a67b8716d928cf7897d140680b0626606/neoforge-26.1.2.73-sources.jar
cd /tmp
jar xf "$SRC" net/neoforged/neoforge/gametest/GameTestHooks.java
cat net/neoforged/neoforge/gametest/GameTestHooks.java
