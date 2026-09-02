#!/bin/bash
set -euo pipefail
MC=/home/spligan/.gradle/caches/ng_execute/fb2c561c7bee37230c168bd0676ea68e111807885c776c074309c76f226aaa4c/outputs.jar
javap -classpath "$MC" -public net.minecraft.gametest.framework.GameTestHelper | grep -iE 'absolute|relative|Pos'
