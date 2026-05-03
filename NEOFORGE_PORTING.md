# NeoForge / Arclight Porting Status

## NeoForge 専用化の進捗（概要）

| 領域 | 状態 |
|------|------|
| ビルド・エントリポイント | **完了**: RebornCore / TechReborn は `neoforge.mods.toml` + `@Mod`（`RebornCoreNeoForge` / `TechRebornNeoForge`）。ルート／RebornCore の **`fabric.mod.json` と Fabric `*Entrypoint` ソースは削除済み**（`net.fabricmc.*` の Java 参照なし）。`migration-tool` サブプロジェクトのみ従来どおり Fabric Loom。 |
| 共通ロジックのローダー非依存化 | **ほぼ完了**: `LoaderBridge`（FML/ModList）、各種 `*Bridge`（ネットワーク・イベント・レジストリ等）。 |
| **Forgified Fabric API** | **完了（本ブランチ）**: Gradle / `mods.toml` から **`fabric_api` / forgified-fabric-api** を外し、NeoForge 単体で解決。流体見た目は **`FluidRenderRegistryBridge` → `NeoForgeFluidRenderAppearanceAdapter`（`IClientFluidTypeExtensions`）** のみ。セル・バケツ等の動的アイテムモデルは **`IDynamicBakedModel` + `getRenderPasses(ItemStack)`**（`techreborn.client.render.DynamicFluidItemModelBase`）。エネルギーアイテムのホットバー挙動は **`RcEnergyItemSwingHooks` + `MixinItemRcEnergy*`**。アイテム／流体の搬送は **`TransferApiBridge`** が **`Capabilities.ItemHandler` / `IFluidHandler`** のみを使用（Fabric Transfer API の型・依存なし）。 |
| **キーバインド** | **NeoForge 化済**: `RegisterKeyMappingsEvent` + `ClientInputBridge`（Fabric `KeyBindingHelper` は削除）。 |
| **機械筐体ブロックモデル** | **NeoForge 化済**: `ModelEvent.ModifyBakingResult`（`NeoForgeMachineCasingModelBridge`）。Fabric `ModelLoadingPlugin` / `MachineCasingModelLoadingBridge` は削除。 |
| クリエイティブタブ改変 | **更新済（本ブランチ）**: `ItemGroupApiBridge` は NeoForge の `BuildCreativeModeTabContentsEvent` のみ（Fabric `ItemGroupEvents` は撤去）。 |
| ランタイム検証 | **手動**: 統合クライアント・専用鯖・Arclight のスモークは CI 未自動化。 |

「NeoForge のみで動く」とは、このリポジトリでは **ローダーは NeoForge 専用**で、**Forgified Fabric API はランタイム依存に含めない**構成です（上表「Forgified Fabric API」行参照）。

Target runtime (this branch assumes):

- Minecraft **1.21.1**
- NeoForge **compile**: **`21.1.219`** (`gradle.properties`: `neo_version` / `neoforge_version`). **`mods.toml`** requires **`[21.1.219,)`** for **`neoforge`**. Arclight を使う場合はバンドル NeoForge がこの範囲を満たすビルドを選ぶ（`gradle.properties` の `arclight_version` は手動検証メモ用）。

### ① Arclight / dedicated server smoke

- **NeoForge dev (RebornCore only)**: `.\gradlew.bat :RebornCore:runServer` — expect **`Done (...)! For help, type "help"`** in `RebornCore/run/server/logs/latest.log` when the dedicated server is healthy.
- **GameTest run target**: `runGameTestServer` fails with *No test functions were given!* until GameTests exist; use **`runServer`** for boot-only smoke.
- **Stage jars for a real Arclight tree**: `.\gradlew.bat prepareNeoForgeSmokeMods` (outputs under `build/smoke-neoforge/mods/`) or `.\scripts\smoke-arclight.ps1` (optional `-ArclightJar` path). Then follow [Arclight](https://github.com/IzzelAliz/Arclight) install docs: copy staged mods、`eula=true`、ハイブリッド鯖 jar を起動（**FFAPI は不要**）。
- **Gradle**: `RebornCore` / ルートの **`build.gradle` から Su5ed（`org.sinytra`）リポジトリは削除済み**（forgified-fabric-api 非依存のため）。`migration-tool` サブプロジェクトの Loom は従来どおり Fabric Maven を `settings.gradle` の `pluginManagement` で参照。
- Arclight **`1.0.2-SNAPSHOT-668f9f3`** (`gradle.properties`: `arclight_version`; hybrid server smoke tests). Prefer an Arclight build whose bundled NeoForge is **≥ `neo_version`** to avoid missing-class crashes from newer NeoForge APIs.

## Completed in this branch

- Added version targets in `gradle.properties`.
- Added NeoForge mod metadata:
  - `src/main/resources/META-INF/neoforge.mods.toml`
  - `RebornCore/src/main/resources/META-INF/neoforge.mods.toml`
- Added repository endpoints for NeoForged and IzzelAliz maven in `build.gradle`.
- Added helper task `printMigrationTargets` to verify active migration targets.
- Ignored generated `RebornCore/bin` output in `.gitignore`.
- NeoForge 専用化に伴い、Fabric 用 `fabric.mod.json` と `*Fabric*Entrypoint` ラッパーは削除（初期化は `@Mod` のみ）。
- Added `reborncore.common.util.LoaderBridge` and moved several direct `FabricLoader` calls behind it to make NeoForge swap-in localized.
- Added networking abstraction bridges and switched core packet registration/handlers to them:
  - `reborncore.common.network.NetworkingBridge`
  - `reborncore.client.network.ClientNetworkingBridge`
- Added `reborncore.common.event.EventBridge` and moved command registration to it (`RebornCoreCommands`, `TechRebornTemplates`).
- Added `reborncore.common.event.ServerLifecycleBridge` and moved `RebornCore` world tick/load, equipment change, and block entity unload registrations to it.
- Added `reborncore.client.event.ClientLifecycleBridge` and moved `RebornCoreClient` lifecycle/render/resource-pack registrations to it.
- Added `LoaderBridge.getEnvironmentType()` and switched `RebornCore.getSide()` to use a loader-agnostic `LoaderBridge.Side` enum (removing direct `EnvType` coupling from `RebornCore`).
- Added `ClientNetworkingBridge.sendToServer(...)` and moved TechReborn client-originated packet sends (GUI/keybind/jump/manual actions) to it.
- Moved RebornCore client-originated packet sends (slot/fluid/redstone config + chunk loader request) to `ClientNetworkingBridge.sendToServer(...)`.
- Added `EventBridge.registerUseBlock(...)` and switched `BlockWrenchEventHandler` + `UseBlockHandler` to use it.
- Added `EventBridge.registerLootModify(...)` and switched `ModLoot` registration to it.
- Added `techreborn.world.WorldgenBridge` and routed worldgen selector/modifier registration in `WorldGenerator` and `TargetDimension` through it.
- Added `reborncore.common.compat.TransferApiBridge` and moved transfer API registration entry points in `RebornCore`/`ModRegistry` to it.
- Replaced Fabric `EventFactory` usage in `ApplyArmorToDamageCallback` and `ItemCraftCallback` with internal loader-agnostic events (`RcEvent`, `RcEventFactory`).
- Added `reborncore.client.input.ClientInputBridge` and switched keybinding registration in `techreborn.client.keybindings.KeyBindings` to it.
- `reborncore.common.compat.RegistryBridge` / `NeoForgeFuelRegistryBridge`: custom furnace fuels from `FuelRecipes` via `FurnaceFuelBurnTimeEvent` on `NeoForge.EVENT_BUS` (no Fabric `FuelRegistry`).
- Added `techreborn.init.VillagerBridge` and routed villager POI/profession/trade/template-pool registration in `TRVillager` through it.
- Replaced `FabricBlockSettings` usage in `TRBlockSettings` with vanilla `AbstractBlock.Settings`.
- Replaced `FabricBlockEntityTypeBuilder` usage in `TRBlockEntities` with vanilla `BlockEntityType.Builder`.
- Added `techreborn.init.EntityTypeBridge` and routed `TRContent` entity-type creation through it (uses vanilla `EntityType.Builder`).
- Added `techreborn.init.WoodTypeBridge` and routed `TRContent` wood/block-set type creation through it.
- Routed TechReborn creative tabs through `reborncore.common.compat.ItemGroupApiBridge` (`TRItemGroup`).
- Replaced Fabric event API in `techreborn.api.events.CableElectrocutionEvent` with internal loader-agnostic events (`RcEvent`, `RcEventFactory`).
- Added `ServerLifecycleBridge.onStartServerTick(...)` and switched `techreborn.blockentity.cable.CableTickManager` from direct `ServerTickEvents` usage to the bridge.
- Moved template-pool dynamic registry callback wiring out of `techreborn.init.VillagerBridge` into `reborncore.common.event.EventBridge.onTemplatePoolAdded(...)`.
- Cable cover rendering reads attach data via `techreborn.blocks.cable.RenderDataBridge#getRenderAttachment`: resolve `CableBlockEntity` with `BlockAndTintGetter#getBlockEntity` and call `CableBlockEntity#getRenderAttachmentData()` (no Fabric `RenderAttachedBlockView` / attachment helpers).
- `FluidRenderRegistryBridge#getFluidRenderAppearanceHandler`: **`NeoForgeFluidRenderAppearanceAdapter` のみ**（`IClientFluidTypeExtensions` の色・静止/流動テクスチャ、`ClientHooks#getBlockMaterial`）。
- `reborncore.mixin.common.MixinItemRcEnergyContinueUsing`: `RcFabricEnergyItem` に対し NeoForge `Item#canContinueUsing` を `ItemUtils.isEqualIgnoreEnergy` で上書き（エネルギー変化のみではブロック破壊を中断しない）。
- `reborncore.mixin.common.MixinItemRcEnergyBlockBreakReset`: `RcFabricEnergyItem` に対し `Item#shouldCauseBlockBreakReset` を `!ItemUtils.isEqualIgnoreEnergy` で上書き（EU 同期で採掘リセットしない）。
- `reborncore.mixin.common.MixinItemRcEnergyReequipAnimation`: `RcFabricEnergyItem` に対し `Item#shouldCauseReequipAnimation` を `!ItemUtils.isEqualIgnoreEnergy` で上書き（エネルギー同期だけで手の再装備アニメを出さない）。`RcEnergyItemSwingHooks` が旧 FabricItem 相当のデフォルトを提供。
- `reborncore.common.screen.ScreenHandlerBridge` / `NeoForgeExtendedScreenHandlerBridge`: extended block menus use NeoForge `IMenuTypeExtension` + `ServerPlayer.openMenu(..., StreamCodec)`; `techreborn.blockentity.GuiType` no longer uses Fabric extended screen handlers.
- Refactored `techreborn.world` types to depend on loader-agnostic worldgen bridge types:
  - `TargetDimension` and `TROreFeatureConfig` now use `WorldgenBridge.BiomeSelector`
  - Ore / rubber / oil placement uses NeoForge biome modifiers: `techreborn.world.compat.neoforge.NeoForgeBiomeModifierPack` registers an auto-enabled datapack under `config/techreborn/generated_worldgen_pack/` (`AddPackFindersEvent`), emitting `neoforge:add_features` JSON from `TechRebornConfig` (Fabric `BiomeModifications` bridge removed). The pack builder uses fixed `ResourceLocation`s for rubber/oil placed features so `WorldGenerator` is not loaded during pack discovery (avoids early static init side effects).
  - `OreDistribution.isGenerating()` now reads `TechRebornConfig.enableOreGeneration` plus the per-ore toggles at call time instead of snapshotting booleans during enum class initialization. Datagen registers configured/placed features for **`TROreFeatureQueries.allWithDistribution()`** (all distributable ores) while biome modifiers only reference **`TROreFeatureQueries.spawnEnabledByConfig()`** so disabled ores keep valid registry entries without world placement.
- `techreborn.init.FlammableBlockBridge` → `FlammableBlockApiBridge` → `NeoForgeFlammableBlockBridge` (vanilla `FireBlock#setFlammable`); rubber wood blocks no longer touch Fabric `FlammableBlockRegistry`.
- `ItemGroupApiBridge` exposes loader-agnostic `Entries`; NeoForge fills them via `BuildCreativeModeTabContentsEvent` (`NeoForgeItemGroupBridge`). `TRItemGroup` only sees `ItemGroupApiBridge.Entries`.
- Replaced Fabric entity builder usage in `techreborn.init.EntityTypeBridge` with vanilla `EntityType.Builder` for `ENTITY_NUKE`.
- `reborncore.common.compat.VillagerApiBridge` delegates to `NeoForgeVillagerBridge` (vanilla POI + `VillagerProfession`, `WandererTradesEvent` on `NeoForge.EVENT_BUS`) from `techreborn.init.VillagerBridge`.
- `WoodTypeApiBridge` / `FlammableBlockApiBridge` delegate to `NeoForgeWoodTypeBridge` / `NeoForgeFlammableBlockBridge`; `WoodTypeBridge` / `FlammableBlockBridge` in TechReborn stay Fabric-free at the call site.
- Creative-tab modifications no longer use Fabric `ItemGroupEvents` (`FabricItemGroupModificationsBridge` removed); NeoForge mod bus listens for `BuildCreativeModeTabContentsEvent`. Documented in `NetworkingBridge` that play payload codecs must register consistently so dedicated server, client, and integrated single-player stay aligned.
- Replaced Fabric `Event` / `EventFactory` in `reborncore.client.ClientJumpEvent` with `RcEvent` / `RcEventFactory`.
- Removed Fabric `PacketByteBufs` from `reborncore.common.screen.BuiltScreenHandler` (use `PacketByteBuf` + `Unpooled.buffer()`).
- Added `reborncore.common.compat.EnergyLookupBridge` for adjacent-block `EnergyStorage` lookup via `TeamRebornEnergyCapabilities.findSided` (NeoForge capability graph).
- Extended `TransferApiBridge` with NeoForge-backed item/fluid helpers (`findItemStorage`, `findFluidStorage`, `FluidUtil.tryFluidTransfer`, `insertVariantCommitted`, `runOuterCommitted`, `simulateOuter`, `EnergyTransferInOuterTx`). Routed cable tick distribution (`CableTickManager`) and resin basin output (`ResinBasinBlockEntity`) through `RcTransaction` + `RcStorage` facades—no Fabric Transfer API types.
- Dynamic cell fluid exposure uses NeoForge stack capabilities (`FluidUtil`) plus thin `DynamicCellFluidProvider`; registration stubs remain on `TransferApiBridge` for call-site clarity.
- Routed storage-unit sided item exposure (`InventoryStorage` / `CombinedSlottedStorage` / anonymous `SingleStackStorage`) through `TransferApiBridge` (`inventoryStorageOf`, `combineSlottedItemStorages`, `createSingleStackStorage`, `itemVariantMaxStackSize`).
- **NeoGradle / MojMap (RebornCore)**: `RebornCore` uses `net.neoforged.gradle.userdev` + NeoForm joined Minecraft; the root TechReborn module compiles against the `RebornCore` `compileClasspath` (no second full userdev in the root project).
- **Java compile + `build -x test`**: `:compileJava`, `:RebornCore:compileJava`, Spotless, `jar`/`sourcesJar`, and resource processing succeed on this branch.
- **Client MojMap screens**: TechReborn GUIs updated for 1.21 screen API (`renderBg` / `renderLabels`, `font`, `leftPos` / `topPos`, `addRenderableWidget`, `Button.bounds`, `Slot` / `BlockState` accessors, etc.).
- **Menu screens**: `RegisterMenuScreensEvent` is registered from `TechRebornNeoForge` on the mod event bus (client-only) with an explicit lambda so it does not clash with the generic `ClientGuiType` record method reference.
- **Gradle resources**: `duplicatesStrategy = DuplicatesStrategy.EXCLUDE` on `processResources`, `jar`, and `sourcesJar` where `src/main/resources` and `src/client/resources` overlap (e.g. duplicate asset paths).
- **Root `compileDatagenJava` / `compileGametestJava`**: lazily append `:RebornCore` `compileClasspath` (same NeoForge stack as `compileJava`). **`datagen` / `gametest` の Groovy はソースセットから除外**（旧 Fabric/Yarn datagen、`compileDatagenGroovy` が MojMap と両立しないため）。移植時は NeoForge DataGen／Java 化または別モジュールで再有効化する。
- **Spotless**: root `HEADER` (TechReborn) vs `RebornCore/HEADER` (RebornCore / TeamReborn) selected per subproject in `build.gradle` `subprojects { spotless { ... } }`.
- **Block atlas references**: client code uses `TextureAtlas.LOCATION_BLOCKS` instead of `InventoryMenu.BLOCK_ATLAS` where appropriate (`RenderUtil`, dynamic cell/bucket models, `NukeRenderer`); common `SpriteSlot` still uses `InventoryMenu.BLOCK_ATLAS` to avoid pulling client-only types into universal bytecode paths.
- **Forgified Fabric API**: Gradle から **削除済み**（`RebornCore` は `net.neoforged:neoforge` のみを `implementation`）。ルートは `api project(':RebornCore')` で RebornCore を参照。
- **NeoForge `RegisterEvent` (boot fix)**: `EnergyImpl` (data component `team_reborn_energy:energy`), `ModSounds` (`reborncore:block_dismantle`), `PaddedShapedRecipe` (recipe serializer), and TechReborn `TRDataComponentTypes` register on the mod event bus instead of `FMLCommonSetupEvent` / static `Registry.register`, so dedicated server / `runServer` no longer hits *Registry is already frozen*.
- **Smoke staging**: Gradle task **`prepareNeoForgeSmokeMods`** and **`scripts/smoke-arclight.ps1`** stage jars under `build/smoke-neoforge/mods/` for Arclight or external server trees.
- **GitHub Actions `check.yml`**: JDK コンテナに **`git` を apt で入れてから** `checkout`、`safe.directory`、`chmod +x gradlew` のあと **`./gradlew :RebornCore:runData`** → **`build`**。ルートに **`runDatagen`** という Gradle タスク名は作らない（NeoGradle が実行種別として解釈して設定に失敗する）。
- **`neoforge.mods.toml`**: **`fabric_api` 依存は削除済み**（必須は NeoForge / Minecraft / TechReborn→reborncore のみ）。

## Remaining blockers

- **`net.fabricmc`（コンパイル対象のソース）**: TechReborn **main**／RebornCore の **`src/**` Java に `net.fabricmc` はない**。`migration-tool` は従来どおり Fabric Loom。**`src/datagen/groovy`** と **`src/gametest/groovy`** には Fabric DataGen／GameTest の import が残るが、**`build.gradle` で Groovy のソースディレクトリを空にしており Gradle はコンパイルしない**（Yarn パッケージ名のままの移植未完コンテンツ）。
- **REI / JEI**: Client recipe viewers compile against **`RoughlyEnoughItems-*-neoforge`** and **`jei-*-neoforge-api`** (`compileOnly` in root `build.gradle`). Runtime loading uses SPI (`META-INF/services/...REIClientPlugin`) and `@JeiPlugin`; optional `mods.toml` entries reference **`rei`** and **`jei`**.
- **Runtime validation**: Integrated client (`RebornCore:runClient`), dedicated server, and Arclight smoke tests from the checklist below are not automated here; run them manually before release.

### Windows dev note

If `:RebornCore:clean` fails with “Unable to delete … `RebornCore-*.jar`” or “別のプロセスが使用中”, close processes locking that JAR (IDE run configs, Gradle daemons holding the file), then retry `clean`.

If **`selectRawArtifactNg_dummy_ng.net.minecraft_client_*.client-extra`** fails with “別のプロセスが使用中” / `FileSystemException` (often when using `--rerun-tasks` or after `cacheVersionExtractedServer*` re-runs):

1. Run **`.\gradlew.bat --stop`**, close other terminals running Gradle, and pause IDE Gradle sync / Java tooling if it pins jars under `RebornCore\.gradle\`.
2. Add a Windows Defender **exclusion** for the repo’s **`RebornCore\.gradle`** folder (real-time scanning commonly locks `client-1.21.1-client-extra.jar` during NeoForm artifact selection).
3. Prefer a normal **`.\gradlew.bat build`** for day-to-day work; reserve **`--rerun-tasks`** for when you intentionally invalidate the NeoForm chain (and retry after releases above).

Repository root **`gradle.properties`** sets **`org.gradle.vfs.watch=false`** to reduce Gradle’s native watcher contention with those jars on Windows.

**VS Code / Cursor Java**: Root **`build.gradle`** merges `:RebornCore` compile/runtime classpaths into IDEA/Eclipse models (`evaluationDependsOn` + Reborn **`afterEvaluate`**) because NeoGradle leaves **`apiElements`** empty—reload the Gradle project (**Java: Clean Java Language Server Workspace** or command palette **Gradle: Refresh Gradle Project**) if root sources still show unresolved `net.minecraft` / NeoForge types.

## Migration order

1. Build system migration:
   - `RebornCore`: NeoGradle userdev + NeoForm (done for this branch).
   - Root TechReborn: Java library + shared `compileClasspath` from `RebornCore` (done); `fabric-loom` may still appear on unrelated subprojects such as `migration-tool`.
   - NeoForge `@Mod` entrypoints (`TechRebornNeoForge`, `RebornCoreNeoForge`) replace Fabric loader metadata for NeoForge loads.
2. Server-critical path:
   - Networking (`Packets`, `ServerboundPackets`, `NetworkManager`).
   - Worldgen (`WorldGenerator`, `TROreFeatureConfig`, `TargetDimension`).
   - Event hooks (`UseBlockHandler`, `OreDepthSyncHandler`, wrench and armor hooks).
3. Client path:
   - Rendering, keybinds, tooltip and model loader hooks.
4. Compat path:
   - REI/PAL and optional guards.
5. Arclight validation:
   - Smoke test boot with Arclight snapshot.
   - Verify startup log has no missing-mod / capability errors for staged NeoForge mods.

## Quick check

Use:

`./gradlew printMigrationTargets`

to verify this branch is configured for the intended NeoForge/Arclight versions.
