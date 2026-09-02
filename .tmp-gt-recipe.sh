#!/bin/bash
set -euo pipefail
# Find smelting recipes for raw_iron_block
find ~/.gradle/caches/ng_execute -path '*data/minecraft/recipe*' -name '*raw_iron*' 2>/dev/null | head -40
echo ====
# also in client jar resources
for j in $(find ~/.gradle/caches -name 'minecraft-*-client.jar' 2>/dev/null | head -5); do
  jar tf "$j" 2>/dev/null | grep -i 'recipe.*raw_iron' | head -20 && echo "in $j"
done
echo ====
# from outputs / transformed
find ~/.gradle/caches -path '*minecraft*recipe*raw_iron*' 2>/dev/null | head -30
