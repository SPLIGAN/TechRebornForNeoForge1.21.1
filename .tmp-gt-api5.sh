#!/usr/bin/env bash
set -euo pipefail
CP=/home/spligan/.gradle/caches/ng_execute/be457848d217ea16a927b2a8f98201d5f8f6012cdd4314df567be19d9a59c551/classes
javap -c -classpath "$CP" net.minecraft.gametest.framework.FunctionGameTestInstance 2>&1 | head -80
echo ====
javap -public -classpath "$CP" net.minecraft.gametest.framework.BuiltinTestFunctions 2>&1 | head -40
echo ====
# Is there a way to register consumer tests directly?
javap -public -classpath "$CP" net.minecraft.gametest.framework.GameTestBatchFactory 2>&1 | head -40
# Check StructureUtils for default empty
javap -public -classpath "$CP" net.minecraft.gametest.framework.StructureUtils 2>&1 | head -30
# TestData constructors via strings
javap -classpath "$CP" net.minecraft.gametest.framework.TestData | head -40
# GameTestEnvironments
javap -public -classpath "$CP" net.minecraft.gametest.framework.GameTestEnvironments
