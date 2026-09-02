# AGENTS.md

## Cursor Cloud specific instructions

This is a **Minecraft mod** (Tech Reborn for NeoForge, targeting **Minecraft 26.1.2**). It is a pure Gradle/Java project with no external services (no databases, Docker, or web servers).

**Porting baseline:** Official Fabric TechReborn **`6.0.2`** → NeoForge. See [`docs/FABRIC_TO_NEOFORGE_26.1.2.md`](docs/FABRIC_TO_NEOFORGE_26.1.2.md). Related upgrade notes: [`docs/NEOFORGE_26.1.2_MIGRATION_SPEC.md`](docs/NEOFORGE_26.1.2_MIGRATION_SPEC.md).

### Build & Run Commands

| Action | Command |
|--------|---------|
| Full build | `./gradlew build --stacktrace` |
| Lint (Spotless) | `./gradlew spotlessCheck` |
| Auto-fix lint | `./gradlew spotlessApply` |
| Data generation | `./gradlew :RebornCore:runServerData --stacktrace` |
| Dedicated server | `./gradlew :RebornCore:runServer` |
| GameTest server | `./gradlew :RebornCore:runGameTestServer --stacktrace` |
| Stage mod jars | `./gradlew prepareNeoForgeSmokeMods` |

CI runs: `./gradlew :RebornCore:runServerData --stacktrace` then `./gradlew build --stacktrace` (see `.github/workflows/check.yml`).

Recipe JSON under `src/main/generated/data/techreborn/recipe/**` is produced by Groovy datagen (`src/datagen/groovy`, NeoForge `RecipeProvider.Runner`). Block/POI tags, block loot tables, and advancements are also generated into `src/main/generated` via `:RebornCore:runServerData`. Client model/blockstate JSON goes to `src/main/generated_assets` via `:RebornCore:runClientData` (separate output so HashCache stale cleanup does not wipe server data). Do not re-copy machine recipes into `src/main/resources`.

### Key versions

| Item | Value |
|------|-------|
| Minecraft | `26.1.2` |
| NeoForge | `26.1.2.73` |
| Java toolchain | **25** |
| Gradle | **9.2.1** (wrapper) |
| Mod version | `6.0.5` (fork line; clean re-port baseline is upstream **`6.0.2`**) |
| Arclight smoke target | `arclight-neoforge-26.1.2-1.0.2-SNAPSHOT` |

### Key Gotchas

- **First build is slow (~3-5 min):** Gradle + NeoGradle must download Minecraft JARs and set up NeoForm. Subsequent builds are fast.
- **EULA for server:** Before running `./gradlew :RebornCore:runServer`, ensure `RebornCore/run/server/eula.txt` contains `eula=true`.
- **Java 25 required:** `java.toolchain.languageVersion = JavaLanguageVersion.of(25)`. Do not use Java 21 guidance from older docs.
- **NeoForge-only runtime:** Production sources must not import `net.fabricmc.*`. Loader differences go through `*Bridge` classes (see the Fabric→NeoForge manual).
- **`migration-tool/` is excluded** from the NeoForge build (`settings.gradle`); do not re-enable it for 26.1.2 work.
- **No unit tests exist yet:** `./gradlew test` completes with `NO-SOURCE`. Machine GameTests live in `src/gametest` and run via `:RebornCore:runGameTestServer` (namespace `techreborn`). Broader validation is through data generation, NeoForge run configs, and Arclight smoke.
- **Client tasks require a display:** `./gradlew :RebornCore:runClient` needs a GUI / framebuffer; not feasible in headless Cloud Agent VMs.
- **`gradlew` must be executable on Unix:** Run `chmod +x gradlew` if builds fail with permission denied.
