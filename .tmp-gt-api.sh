#!/usr/bin/env bash
set -euo pipefail
SRC=$(find /home/spligan/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/26.1.2.73 -name '*-sources.jar' | head -1)
echo "SRC=$SRC"
jar tf "$SRC" | grep -iE 'GameTest|RegisterGameTests|TestContext|GameTestHelper' | head -50
CP=/home/spligan/.gradle/caches/ng_execute/be457848d217ea16a927b2a8f98201d5f8f6012cdd4314df567be19d9a59c551/classes
find "$CP" -name '*GameTest*.class' 2>/dev/null | head -20
find "$CP" -name 'TestContext.class' 2>/dev/null | head -10
find "$CP" -name 'GameTestHelper.class' 2>/dev/null | head -10
# also annotation package
find "$CP" -path '*gametest*' -name '*.class' 2>/dev/null | head -40
find "$CP" -path '*/test/*' -name 'GameTest.class' 2>/dev/null | head -10
