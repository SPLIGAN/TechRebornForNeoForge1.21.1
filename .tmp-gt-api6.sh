#!/usr/bin/env bash
set -euo pipefail
CP=/home/spligan/.gradle/caches/ng_execute/be457848d217ea16a927b2a8f98201d5f8f6012cdd4314df567be19d9a59c551/classes
javap -classpath "$CP" net.minecraft.gametest.framework.GameTestInstance | head -60
echo ====
# RecipeCrafter location
find /home/spligan/tr-neoforge-build -name 'RecipeCrafter.java' 2>/dev/null | head -5
# empty structure in vanilla
find /home/spligan/.gradle/caches -path '*data/minecraft/structure*' -name '*.nbt' 2>/dev/null | head -20
find /home/spligan/.gradle/caches -name '*empty*' -path '*structure*' 2>/dev/null | head -20
