# AGENTS.md

## Cursor Cloud specific instructions

This is a **Minecraft mod** (Tech Reborn for NeoForge, targeting Minecraft 1.21.1). It is a pure Gradle/Java project with no external services (no databases, Docker, or web servers).

### Build & Run Commands

| Action | Command |
|--------|---------|
| Full build | `./gradlew build --stacktrace` |
| Lint (Spotless) | `./gradlew spotlessCheck` |
| Auto-fix lint | `./gradlew spotlessApply` |
| Data generation | `./gradlew :RebornCore:runData --stacktrace` |
| Dedicated server | `./gradlew :RebornCore:runServer` |
| Stage mod jars | `./gradlew prepareNeoForgeSmokeMods` |

CI runs: `./gradlew :RebornCore:runData --stacktrace` then `./gradlew build --stacktrace` (see `.github/workflows/check.yml`).

### Key Gotchas

- **First build is slow (~3-5 min):** Gradle + NeoGradle must download and decompile Minecraft JARs, apply access transformers, and remap sources. Subsequent builds are fast (<10s).
- **EULA for server:** Before running `./gradlew :RebornCore:runServer`, ensure `RebornCore/run/server/eula.txt` contains `eula=true`.
- **Java 21 required:** The toolchain is pinned to Java 21 (`java.toolchain.languageVersion = JavaLanguageVersion.of(21)`).
- **No unit tests exist yet:** `./gradlew test` completes with `NO-SOURCE`. Validation is done through data generation and server boot.
- **Fabric API warnings at server boot are expected:** The mod is ported from Fabric; warnings about missing Fabric modules are cosmetic and do not affect functionality.
- **Client tasks require a display:** `./gradlew :RebornCore:runClient` needs X11/virtual framebuffer; not feasible in headless Cloud Agent VMs.
- **`gradlew` must be executable:** Run `chmod +x gradlew` if builds fail with permission denied.
