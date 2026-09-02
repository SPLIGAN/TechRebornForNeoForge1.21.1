# TechReborn — NeoForge 26.1.2 向け修正仕様書

本書は **Arclight**（`arclight-neoforge-26.1.2-1.0.2-SNAPSHOT`）上で **NeoForge 対応 TechReborn** を動作させるための **1.21.1 → 26.1.2** 移植修正仕様である。  
**実装は本書の ID 順に進め、完了時は各 ID の状態列を更新する。**

| 文書の位置 | `docs/NEOFORGE_26.1.2_MIGRATION_SPEC.md` |
| 前版（1.21.1） | `docs/NEOFORGE_1.21.1_MIGRATION_SPEC.md` |
| 作業状況（英語） | リポジトリ直下 `NEOFORGE_PORTING.md`（26.1.2 着手後に更新予定） |
| 作業ブランチ | **`26.1.2`** |
| 対象ツリー | `RebornCore/` + ルート `TechReborn` + `src/*` のみ（新規トップレベルモジュール追加なし） |

---

## 0. 要約（エグゼクティブ）

| 項目 | 内容 |
|------|------|
| 目的 | 既存 **NeoForge 1.21.1** 移植済みコードを **NeoForge 26.1.2**（Minecraft 26.1.2）へ昇格し、**Arclight NeoForge ハイブリッド鯖**で安定稼働させる |
| ベースライン | 1.21.1 仕様書 §9.1 の完了項目（Data Components、RegisterEvent、FluidType、Payload 等）を **維持・再検証** |
| 26.1.2 の本質的変更 | **難読化廃止**（公式パラメータ名）、**Java 25**、**`ResourceLocation` → `Identifier`**、**`ItemStackTemplate` / `FluidStackTemplate`**、**GUI `extract*` リネーム**、**村人取引データパック化**、NeoForge **Transfer API（`ResourceHandler`）** |
| Data Components | 1.21.1 で導入済みの方針は **継続有効**。26.1.2 では `DataComponentGetter` / `ItemStackTemplate` 連携が強化 |
| 現状（2026-06-17 実装） | **Phase 1 完了** + **VIL-01/02 完了** + **UP-03 完了** + **UP-11 完了** + **UP-12 完了** + **UP-13 完了** + **UP-06 完了** + **UP-10 完了** + **UP-04 部分完了**（JEI コンパイル有効化・26.1 API 追随）。`./gradlew build` 成功。次: Arclight 実機（A-01〜A-05）、JEI 実機確認・subtype/transfer 拡張 |
| 配布 | **単一 JAR**（RebornCore Jar-in-Jar 同梱）。Arclight `mods/` には TR JAR のみ |
| 参考 | Arclight 26.1.2 仕様書、NeoForge 26.1 Primer / Release Notes、**[公式 TechReborn `26.1`](https://github.com/TechReborn/TechReborn/tree/26.1)**（`upstream/26.1`）、Thermal 系 1.21.1 NeoForge ポート |
| upstream 取り込み | 公式は **Fabric 26.1.2**（mod **6.0.5**）。本フォークは **API パターンとコンテンツ差分を cherry-pick** し、NeoForge ブリッジ層は維持 |

---

## 1. 目的

- **Arclight** `arclight-neoforge-26.1.2-1.0.2-SNAPSHOT` 環境で NeoForge 対応 TechReborn が安定して動作すること。
- **NeoForge 1.21.1** から **NeoForge 26.1.2** への API 変更（中間版 21.2〜21.11 を含む）に追随すること。
- 修正は **既存リポジトリのツリー構造内のみ**（`RebornCore` + ルート `TechReborn` + `src/*`）。新規トップレベルモジュールは追加しない。
- **公式 upstream** [TechReborn `26.1`](https://github.com/TechReborn/TechReborn/tree/26.1) のゲームロジック・レシピ・バグ修正を **NeoForge 層を壊さず cherry-pick** すること（§5.5, §8.6）。
- **Cursor Free 枠のみ**で実装（大規模自動生成・有償 API 依存を避ける）。

---

## 2. 前提（ターゲット環境）

| 項目 | 現行（ソース・2026-06-13） | ターゲット |
|------|---------------------------|------------|
| Minecraft | **1.21.1**（`gradle.properties`） | **26.1.2** |
| NeoForge | **21.1.219** | **26.1.2**（例: `26.1.0.1-beta` 以降、Arclight 同梱版に合わせる） |
| Arclight | `1.0.2-SNAPSHOT-668f9f3`（1.21.1 メモ） | **`arclight-neoforge-26.1.2-1.0.2-SNAPSHOT`** |
| Java | **21** | **25** |
| Gradle | **8.14.3** | **9.1.0+** |
| NeoGradle | **7.1.25**（RebornCore userdev） | **7.1.21+**（[26.1 Release Notes](https://neoforged.net/news/26.1release/)） |
| Parchment | `2024.11.17`（1.21.1） | **削除可**（公式パラメータ名が利用可能） |
| 本リポ mod 版 | **5.11.19** | upstream **`6.0.5`**（[Releases](https://github.com/TechReborn/TechReborn/releases)）— 取り込み完了時に合わせる |
| FML ローダ範囲 | **`[4,)`** | **`[4,)`**（維持想定） |
| upstream TechReborn | [`upstream/26.1`](https://github.com/TechReborn/TechReborn/tree/26.1) — **Fabric Loom 1.17** / FAPI **0.145.4+26.1.2** / Energy **5.0.0** | 本リポは **NeoForge 専用フォーク** — **ビルド系はマージしない**。ゲームロジックは `git diff upstream/26.1` で cherry-pick |

---

## 3. 参照資料

| 資料 | URL / パス |
|------|------------|
| NeoForge 公式 | https://docs.neoforged.net/ |
| **26.1 Primer**（1.21.11→26.1 バニラ変更の網羅） | https://docs.neoforged.net/primer/docs/26.1/ |
| **26.1 Release Notes**（ビルド・ItemStackTemplate 等） | https://neoforged.net/news/26.1release/ |
| Versioning（MC / NeoForge 版体系） | https://docs.neoforged.net/docs/gettingstarted/versioning |
| Data Components（概念は 1.21.1 文書も有効） | https://docs.neoforged.net/docs/1.21.1/items/datacomponents |
| 中間版リリースノート | 26.1 Release Notes 内リンク（**21.2, 21.4, 21.5, 21.6, 21.9, 21.11**） |
| Forge / NeoForge 比較表 | https://docs.google.com/spreadsheets/d/1_DQELiPvCF0FmFfyU4opGDbWi7zv-bSl8ImZuh6645E/edit?gid=248444698 |
| **Arclight 26.1.2 仕様書** | `C:\Users\SPLIGAN\Documents\GitHub\Arclight\docs\NEOFORGE_26.1.1_MIGRATION_SPEC.md`（ファイル名は 26.1.1 だが内容は **26.1.2** 実装進捗） |
| 本リポ 1.21.1 仕様書（完了ベースライン） | `docs/NEOFORGE_1.21.1_MIGRATION_SPEC.md` |
| Thermal 系 1.21.1 参考（API パターン） | `ThermalCoreForNeoForge/docs/NEOFORGE_1.21.1_MIGRATION_SPEC.md` 他 |
| **公式 TechReborn 26.1（upstream）** | https://github.com/TechReborn/TechReborn/tree/26.1 — ローカル `upstream/26.1`（2026-06-13 時点 HEAD: `d8d36c37b`） |
| 本リポジトリ | `TechRebornForNeoForge1.21.1`（ブランチ `26.1.2`） |

---

## 4. スコープ

### 4.1 修正対象ツリー（1.21.1 仕様書と同一）

```
TechRebornForNeoForge1.21.1/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── RebornCore/
├── src/main/java/techreborn/
├── src/client/java/techreborn/
├── src/main/resources/
├── src/client/resources/
├── src/main/generated/
├── src/main/generated_assets/   # client ModelProvider output
├── src/datagen/groovy/          # recipe / loot / advancement / model datagen
└── src/gametest/                # NeoForge GameTest (groovy + java; gameTestServer modSource)
```

**対象外:** `migration-tool/`、CurseForge 公開パイプライン細部。

### 4.2 配布形態（Arclight）

| 配置 | 内容 |
|------|------|
| `mods/techreborn-*.jar` | TechReborn 単体 JAR（RebornCore Jar-in-Jar 同梱） |
| 別途 `reborncore.jar` | **不要** |

---

## 5. 移植の層構造

### 5.1 1.21.1 NeoForge 移植で完了済み（再検証のみ）

以下は **1.21.1 仕様書 §9.1 で完了** と記録済み。26.1.2 では **破壊的変更がない限り維持**し、ビルド成功後に Arclight で再検証する。

| 領域 | 1.21.1 完了 ID | 26.1.2 での扱い |
|------|----------------|-----------------|
| ItemStack ルート NBT 廃止 → Data Components | TR-R03, TR-R10 | **維持**（`TRDataComponentTypes`, `BaseBlockEntityProvider`） |
| RegisterEvent フェーズ登録 | TR-R01〜R02, R16 | **維持**（API シグネチャ変更があれば追随） |
| Payload + StreamCodec | TR-R08 | **要確認**（§5.4 NF-01） |
| Capabilities 登録 | TR-R09 | **要移行**（§5.4 CAP-01 — `IItemHandler` 系） |
| FluidType | TR-F01 | **維持**（クライアント `Material` API は §5.3 CLI-02） |
| Mixin JAVA_21 | TR-M01 | **JAVA_25 へ更新**（§8 B-11） |
| Forgified Fabric API 除去 | TR-R13 | **維持** |
| `RecipeIngredientCompat` | TR-R05 | **維持** + `Ingredient` パッケージ移動対応（§5.2 REC-02） |

### 5.2 NeoForge 1.21.1 → 26.1.2：バニラ＋ローダー変更の本質

Minecraft は **26.1** から calver 体系（`year.release.patch`）。NeoForge 版は **`26.1.0.N-beta`** 形式（先頭 3 成分 = MC 版）。

| レイヤ | 1.21.1（現行ソース） | 26.1.2（ターゲット） |
|--------|----------------------|----------------------|
| 難読化 | Parchment + MojMap | **公式パラメータ名**（Parchment 任意） |
| Java | 21 | **25** |
| 識別子型 | `net.minecraft.resources.ResourceLocation` | **`net.minecraft.resources.Identifier`**（全置換） |
| レシピ結果（不変） | `ItemStack` フィールド / `getResultItem()` | **`ItemStackTemplate`** + `result()` |
| 流体結果（不変） | `FluidStack` | **`FluidStackTemplate`**（datagen / codec） |
| アイテム `use` 戻り値 | `InteractionResultHolder<ItemStack>` | **`InteractionResult`**（interface 化） |
| GUI 描画 | `renderBg` / `renderLabels` / `GuiGraphics` | **`extractBackground` / `extractLabels` / `GuiGraphicsExtractor`** |
| 村人取引 | `VillagerTrades.ItemListing` + `TRADES` map | **`VillagerTrade` + `TradeSet` データパック** |
| Ingredient | `net.minecraft.world.item.Ingredient` | **`net.minecraft.world.item.crafting.Ingredient`** |
| ChunkPos 生成 | `new ChunkPos(pos)` / `asLong` | **`ChunkPos.containing` / `pack` / `unpack`** |
| エンティティスポーン理由 | （本 mod 未使用） | `MobSpawnType` → **`EntitySpawnReason`** |
| NeoForge アイテム搬送 | `IItemHandler` / `Capabilities.ItemHandler` | **`ResourceHandler<ItemResource>`** / `neoforge.transfer.*`（Arclight 実測） |
| FML 起動 | modlauncher `--launchTarget` | **`net.neoforged.fml.startup.Server#main`**（Arclight 側。mod 本体は通常どおり `@Mod`） |

### 5.5 公式 upstream TechReborn `26.1` との関係（取り込み方針）

[TechReborn/TechReborn `26.1`](https://github.com/TechReborn/TechReborn/tree/26.1) は **Fabric 26.1.2** 向け公式ポート（mod **6.0.5**、Java **25**、Loom **1.17.0-alpha.14**）。本 NeoForge フォークとの差分は **754 ファイル**規模（`git diff --stat upstream/26.1 HEAD`、2026-06-13）。

**マージ禁止・cherry-pick 推奨**

| 領域 | upstream `26.1` | 本フォーク（NeoForge 1.21.1） | 26.1.2 での方針 |
|------|-----------------|------------------------------|-----------------|
| ビルド | Fabric Loom 全プロジェクト | NeoGradle userdev + Jar-in-Jar | **upstream の `build.gradle` は採用しない** |
| エントリポイント | `fabric.mod.json` + Fabric 初期化 | `@Mod` + `*NeoForge.java` | **NeoForge エントリを維持** |
| ワールド生成 | `BiomeModifications`（Fabric API） | `NeoForgeBiomeModifierPack` | upstream の鉱石/木ロジックのみ移植し、登録は NeoForge パック経由 |
| 村人 | **`TRVillager` 全体がコメントアウト**（`/* TODO: Villager trading */`） | `RegisterEvent` + `NeoForgeVillagerBridge` で **有効** | **本フォークの村人実装を維持**し、26.1 のデータパック取引 API（§5.2）へ段階移行 |
| レシピ互換 | `FluidContainerIngredient`（Fabric `CustomIngredient`） | `RecipeIngredientCompat`（`fabric:components` フォールバック） | Fabric 専用クラスは **NeoForge 向けに再実装**（UP-03） |
| JEI | **リポジトリ内蔵**（`src/client/.../jei/JEIPlugin.java` 他、旧 TechRebornJEI から移行） | `TechRebornJeiPlugin.java`（compileOnly・旧 API） | upstream JEI 構造を **NeoForge JEI API** で移植（UP-04） |
| `PaddedShapedRecipe` | **upstream 26.1 から削除** | NeoForge `RegisterEvent` で登録済み | 既存レシピ JSON が参照する限り **NeoForge 側で維持**（UP-05） |

**upstream が既に完了している 26.1 API パターン（本フォークへの適用指針）**

| upstream の実装 | 参考ファイル | 本フォークでの対応 ID |
|-----------------|-------------|---------------------|
| `Identifier` 全面採用 | 全ソース | ID-01 |
| `RebornRecipe.outputs` → `List<ItemStackTemplate>` | `RebornCore/.../RebornRecipe.java` | REC-01 |
| `RecipeCrafter` — `ItemStackTemplate` 出力、`ValueInput`/`ValueOutput` 永続化、`poweredMachine` リネーム | `RecipeCrafter.java` | UP-01, REC-01 |
| `RebornRecipeDisplay` + `RecipeDisplay` / `SlotDisplay` | `RebornRecipeDisplay.java` | UP-02 |
| `SizedIngredient` — `world.item.crafting.Ingredient` | `SizedIngredient.java` | REC-02 |
| `HologramRenderer` — `BlockModelResolver` / `ItemStackRenderState` / `HologramRenderState` | `HologramRenderer.java`, `HologramRenderState.java` | UP-06, CLI-03 |
| `ChunkPosMultiMap` + `ChunkEventListeners` | `reborncore.common.misc.world.*` | UP-07, CHK-01 |
| JSpecify `@NullMarked` + `package-info.java` | 全パッケージ | UP-08（低優先） |

**Data Components（1.21.1 からの継続 — 最重要）**

| 用途 | 方針（変更なし） |
|------|------------------|
| アイテム独自データ | `DataComponentType` — `stack.get` / `set` / `update` |
| BE ドロップ | `DataComponents.BLOCK_ENTITY_DATA` + `CustomData` |
| BE ワールド永続化 | `CompoundTag` in `saveAdditional`（対象外） |
| エネルギー Item | `team_reborn_energy:energy` |
| 動的セル | `techreborn:fluid` |

26.1.2 追加: レシピ・advancement 等の **不変スタック表現**は `ItemStackTemplate` を使用。`ItemStack` の新規生成はレジストリロード後のみ。

### 5.3 クライアント・レンダリング（26.1.2 固有）

| 区分 | 1.21.1 | 26.1.2 | 本リポ影響 |
|------|--------|--------|------------|
| GUI 基底 | `GuiBase#renderBg` 等 | `extractBackground` / `extractLabels` | **~60 GUI ファイル**（`GuiBase` + 各 `Gui*`） |
| GuiGraphics | `GuiGraphics` | `GuiGraphicsExtractor` | `GuiSprites`, `GuiBuilder`, REI 連携 |
| テクスチャ参照 | `Material(ResourceLocation, ...)` | `Material(Identifier, ...)` | `GuiSprites`, `MachineCasingModel`, 動的セル/バケツ |
| ブロックアトラス | `TextureAtlas.LOCATION_BLOCKS` | 要 26.1 ソースで再確認 | `RenderUtil`, `SpriteSlot`, 動的モデル |
| 流体モデル | `IClientFluidTypeExtensions` | 維持想定 + `FluidModel.Unbaked` / `Material` 変更の可能性 | `NeoForgeFluidRenderAppearanceAdapter` |

### 5.4 NeoForge ローダー API（26.1.2）

| 区分 | 1.21.1 | 26.1.2 | 本リポ影響 |
|------|--------|--------|------------|
| Payload 登録 | `PayloadRegistrar#playToClient` 等 | **ハンドラ分離**（Arclight: 7 引数 → 6 引数 + `SERVERBOUND_HANDLERS`） | `Packets.java`, `ServerBoundPackets.java`, TR payloads |
| アイテム Cap | `IItemHandler`, `SidedInvWrapper` | **`ResourceHandler<ItemResource>`**（移行期は NeoForge がブリッジ提供する可能性 — 要 compile 確認） | `TransferApiBridge`, `RcStorageItemHandler` |
| イベント | `BlockEvent.BreakEvent` | `event.level.block.BreakBlockEvent` | `EventBridge`（使用箇所 grep 要） |
| `FMLLoader` | `getLoadingModList()` | `getCurrent().getLoadingModList()` | `LoaderBridge`（あれば） |

---

## 6. API 対応表（26.1.2 重点）

### 6.1 ビルド・メタデータ

| 区分 | 1.21.1 | 26.1.2 | 状態 |
|------|--------|--------|------|
| `minecraft_version` | `1.21.1` | `26.1.2` | **完了** |
| `neo_version` / `neoforge_version` | `21.1.219` | `26.1.2.73`（Arclight 同梱） | **完了** |
| `minecraft_version_range` | `[1.21.1,1.21.2)` | `[26.1.2,26.2)` | **完了** |
| Java toolchain | 21 | 25 | **完了** |
| Gradle wrapper | 8.14.3 | 9.2.1 | **完了** |
| Parchment | 有効 | 削除 | **完了** |
| `neoforge.mods.toml` | `[21.1.216,)`, `[1.21.1,1.21.2)` | `[26.1.0,)`, `[26.1.2,26.2)`, `loaderVersion=[11,)` | **完了** |
| `energy_version` | （未記載） | `5.0.0`（メモ） | **完了** |
| `migration-tool` | Loom 1.10 | **settings.gradle から除外**（Java 25 非互換） | **完了** |
| JEI / REI / PAL | 1.21.1 版 | 26.1 対応版（公開後） | **要調査** |

### 6.2 識別子・レジストリ

| 区分 | 置換 | grep 概算 | 状態 |
|------|------|-----------|------|
| `ResourceLocation` import / 型 | `Identifier` | RebornCore + src（main/client）**完了** | **完了** |
| `ResourceLocation.fromNamespaceAndPath` | `Identifier.fromNamespaceAndPath` | 同上 | **未実施** |
| `ResourceLocation.parse` | `Identifier.parse` | 同上 | **未実施** |
| `ModelResourceLocation` | 26.1 ソースで確認 | 動的セル/バケツ | **要調査** |
| `RegisterEvent` | 維持 | — | **再検証** |

### 6.3 レシピ・データ

| 区分 | 1.21.1 | 26.1.2 | 主要ファイル | 状態 |
|------|--------|--------|--------------|------|
| 成形レシピ結果 | `ItemStack result` | `ItemStackTemplate result` | `PaddedShapedRecipe` | **未実施** |
| 機械レシピ出力 | `List<ItemStack> outputs` in codec | `List<ItemStackTemplate>` | `RebornRecipe`, `RebornRecipeDisplay` | **RebornCore 完了**；TR レシピ・`RecipeCrafter` **未実施** |
| `RecipeSerializer` | 独自 record implements | バニラ `RecipeSerializer` record | `RecipeManager` | **完了**（`RebornRecipeSerializer` 削除） |
| Advancement criterion | `advancements.critereon` | `advancements.criterion` | `RecipeUtils`, `PaddedShapedRecipeJsonBuilder` | **完了** |
| ShapedRecipe 結果 | `getResultItem()` | `result().create()` 等 | `RollingMachineRecipe`, 各 Recipe | **未実施** |
| Ingredient パッケージ | `world.item` | `world.item.crafting` | `RecipeIngredientCompat`, 全レシピ | **未実施** |
| レシピビルダー | `ItemStack` 引数 | `ItemStackTemplate` 引数 | `PaddedShapedRecipeJsonBuilder` | **未実施** |
| `fabric:components` 互換 | `RecipeIngredientCompat` | **維持** | 同上 | **再検証** |

### 6.4 アイテム・相互作用

| 区分 | 1.21.1 | 26.1.2 | 主要ファイル | 状態 |
|------|--------|--------|--------------|------|
| `Item#use` 戻り値 | `InteractionResultHolder<ItemStack>` | `InteractionResult` | **13 ファイル**（`DynamicCellItem`, ツール, 防具等） | **未実施** |
| Data Components | 導入済み | 維持 + `DataComponentGetter` | `TRDataComponentTypes`, `EnergyImpl` | **再検証** |

### 6.5 搬送・Capabilities

| 区分 | 1.21.1 | 26.1.2 | 主要ファイル | 状態 |
|------|--------|--------|--------------|------|
| ブロック ItemHandler | `Capabilities.ItemHandler` + `IItemHandler` | `ResourceHandler` 系（要 NeoForge 26.1 Javadoc） | `TransferApiBridge`, `TechRebornCapabilities` | **未実施** |
| `SidedInvWrapper` | NeoForge items | 26.1 でパッケージ/API 確認 | `TransferApiBridge` | **未実施** |
| 流体 Handler | `IFluidHandler` | 維持想定（`FluidStackTemplate` は datagen 側） | `TransferApiBridge`, `TankFluidHandler` | **要調査** |
| TeamReborn Energy | `TeamRebornEnergyCapabilities` | `energy_version=5.0.0` 互換確認 | `EnergyImpl`, Cap 登録 | **要調査** |

### 6.6 村人・ワールド

| 区分 | 1.21.1 | 26.1.2 | 主要ファイル | 状態 |
|------|--------|--------|--------------|------|
| 職業取引 | `VillagerTrades.ItemListing` + `TRADES` map | **`VillagerTrade` + `TradeSet` データパック** | `TradeUtils`, `TRVillager` | **完了**（VIL-01） |
| 放浪商人 | `WandererTradesEvent` | イベント存続 + 内部 `VillagerTrade` 化の可能性 | `NeoForgeVillagerBridge` | **要調査** |
| 村人の家 TR-V01 | 未実施（1.21.1） | データパック merge 方針は維持 | `TRVillager` | **未実施** |
| バイオーム修飾 | `NeoForgeBiomeModifierPack` | 維持 + `Identifier` 置換 | `NeoForgeBiomeModifierPack` | **未実施** |
| ChunkPos | 旧 API | `containing` / `pack` / `unpack` | `ChunkLoaderManager` 等 **5 ファイル** | **未実施** |

### 6.7 ネットワーク

| 区分 | 1.21.1 | 26.1.2 | 状態 |
|------|--------|--------|------|
| `CustomPacketPayload.Type` | `ResourceLocation` ベース ID | `Identifier` ベース | **未実施** |
| `PayloadRegistrar` | `playToClient` / `playToServer` | シグネチャ変更の可能性 | **未実施** |
| `ItemStack.STREAM_CODEC` | 維持 | 維持（実行時スタック） | **再検証** |
| Payload 内 `CompoundTag` | 維持（BE 設定同期） | 維持 | **再検証** |

---

## 7. 修正仕様 — ビルド・メタデータ

| ID | 内容 | 受入条件 | 状態 |
|----|------|----------|------|
| B-01 | `gradle.properties` — MC **26.1.2** / NeoForge **26.1.2.73** / Java **25** / mod **6.0.5** | 依存解決 | **完了** |
| B-02 | Gradle wrapper → **9.2.1** | `./gradlew --version` | **完了** |
| B-03 | RebornCore NeoGradle userdev **7.1.36** | NeoForm `26.1.2` 解決 | **完了** |
| B-04 | Parchment 設定削除 | ビルド成功 | **完了** |
| B-05 | `META-INF/neoforge.mods.toml`（TR / RC） | expand 成功 | **完了** |
| B-06 | `energy_version=5.0.0` 追加 | `gradle.properties` | **完了** |
| B-07 | JEI / REI / PAL を 26.1 対応版に更新（compileOnly） | 依存解決 or 一時除外 | **部分完了**（`jei_version=29.5.0.28` 解決済み・JEI ソースコンパイル有効。REI 16.0.754 は compile 除外維持） |
| B-08 | `prepareNeoForgeSmokeMods` 維持 | smoke ステージング | **再検証** |
| B-09 | `./gradlew compileJava` エラーゼロ | 全モジュール | **完了** |
| B-10 | `./gradlew build` | 成果物生成 | **完了** |
| B-11 | Mixin `compatibilityLevel` → **JAVA_25** | mixin json 2 ファイル | **完了** |
| B-12 | `mod_version` → **6.0.5** | `gradle.properties` | **完了** |
| B-13 | `runs { data }` → **`clientData`**（NeoGradle 26.1） | RebornCore/build.gradle | **完了** |
| B-14 | `migration-tool` を settings から除外 | Java 25 + Loom 非互換回避 | **完了** |

---

## 8. 修正仕様 — ソース（実装順・バックログ）

**上から順に**実施。1.21.1 完了項目は **再検証（RV-xx）** として末尾に記載。

### 8.1 Phase 1 — コンパイル通過（最優先）

| ID | 内容 | 仕様 | 優先度 | 状態 |
|----|------|------|--------|------|
| ID-01 | **`ResourceLocation` → `Identifier` 一括置換** | RebornCore + src 全 Java ソース完了 | 最高 | **完了** |
| REC-01 | **レシピ `ItemStackTemplate` 化** | `RebornRecipe` / TR recipe サブクラス / `PaddedShapedRecipe` / `RecipeCrafter` 完了 | 最高 | **完了** |
| CHK-01 | **`ChunkPos` / `SavedDataType` / `TicketType`** | `TicketType` は `RegisterEvent` 登録（`FMLCommonSetup` では凍結エラー）。dev server 起動確認済み | 中 | **完了** |
| REC-02 | **`Ingredient` パッケージ移動** | `net.minecraft.world.item.crafting.Ingredient` へ移行済み | 高 | **完了** |
| ITM-01 | **`InteractionResultHolder` → `InteractionResult`** | 全 Item クラス `use` / `useOn` 移行済み | 高 | **完了** |
| NF-01 | **Payload 登録 API 追随** | RebornCore + TR payloads 登録済み | 高 | **完了** |
| CAP-01 | **Transfer API / ItemHandler 移行** | deprecated `IItemHandler`/`IFluidHandler` ブリッジでコンパイル通過。本格 `ResourceHandler` 移行は Phase 3 | 高 | **部分完了**（コンパイル） |
| VIL-01 | **村人職業** | POI/職業登録 + `trade_set`/`villager_trade` データパック（冶金・電気 各5レベル） | 中 | **完了** |

### 8.2 Phase 2 — クライアント

| ID | 内容 | 仕様 | 優先度 | 状態 |
|----|------|------|--------|------|
| CLI-01 | **GUI `extract*` 移行** | `GuiBase` + 全 TR `Gui*` / config GUI 完了 | 高 | **完了** |
| CLI-02 | **`GuiGraphics` → `GuiGraphicsExtractor`** | RebornCore + TR GUI 完了（REI は compile 除外） | 高 | **完了** |
| CLI-03 | **`Material` / モデル API** | `SpriteId` / `FluidModel` / `MachineFaceElementRenderer`；`FluidTintSource` / `AtlasManager` | 中 | **RebornCore 完了** |
| CLI-04 | **エンティティレンダラ** | `NukeRenderer`, `TurbineRenderer` — 26.1 API 追随済み | 中 | **完了** |
| CLI-05 | **`RegisterMenuScreensEvent` / クライアント NeoForge エントリ** | `TechRebornNeoForgeClient` 登録済み | 中 | **完了** |

### 8.3 Phase 3 — ゲームロジック・データ

| ID | 内容 | 仕様 | 優先度 | 状態 |
|----|------|------|--------|------|
| VIL-01 | **村人職業取引のデータパック化** | `data/techreborn/villager_trade/` + `trade_set/` + `tags/villager_trade/`。`TRVillager` は `tradeSetsByLevel` を `RegisterEvent` で職業に紐付け | 中 | **完了** |
| VIL-02 | **放浪商人** | `data/minecraft/tags/villager_trade/wandering_trader/common.json` にゴムの苗木取引を追加 | 中 | **完了** |
| VIL-03 | **村人の家（TR-V01 継承）** | 構造 pool データパック merge | 低 | **未実施** |
| WLD-01 | **ワールド生成・鉱石** | `NeoForgeBiomeModifierPack`, `WorldGenerator` — `Identifier` + 26.1 feature API | 中 | **未実施** |
| NBT-01 | **`BuiltInRegistries` / `TagsUpdatedEvent`** | 1.21.1 TR-N04 継続。Arclight 26.1.2 で再監査 | 中 | **完了**（`ItemStack` 禁止NBT API 呼び出しなし確認） |

### 8.4 Phase 4 — 1.21.1 バックログの継承

| ID | 内容 | 1.21.1 状態 | 26.1.2 | 状態 |
|----|------|-------------|--------|------|
| TR-D01 | Datagen ポート | 未実施 | `GatherDataEvent` + Java 化 | **レシピ + block/POI タグ + block loot + advancements + ModelProvider 配線完了** |
| TR-G01 | GameTest ポート | 未実施 | NeoForge GameTest | **完了**（`RegisterGameTestsEvent` + `GameTestHelper`、3 tests green。Fabric の raw_iron_block 精錬ケースはバニラ/TR にレシピ無しのため未登録） |
| TR-P01 | PAL 量子スーツ | compile 除外 | 26.1 PAL 版があれば再有効化 | **未実施** |
| TR-N01 | `RcFabricEnergyItem` リネーム | 未実施 | 低優先 | **未実施** |
| TR-N05 | `mods.toml` modId 統一 | 要確認 | 維持 | **要確認** |

### 8.5 再検証（1.21.1 完了項目）

| ID | 内容 | 状態 |
|----|------|------|
| RV-01 | Data Components — 禁止 NBT API がソースにないこと | **完了**（`getTag/setTag/hasTag/getOrCreateTag` 呼び出しなし確認） |
| RV-02 | `net.minecraftforge` / `net.fabricmc`（本番 Java）ゼロ | **完了**（Java 本番ソースに import なし。`src/datagen` / `src/gametest` Groovy は対象外） |
| RV-03 | FluidType 登録・流体描画 | **再検証待ち** |
| RV-04 | `RecipeIngredientCompat` + 既存 JSON 資産 | **完了**（`fabric:components` + `reborncore:fluid_container` + 流体クラフト JSON 7 件） |
| RV-05 | Jar-in-Jar 単一 JAR 配布 | **再検証待ち** |

### 8.6 Phase 5 — 公式 upstream `26.1` からの取り込み（cherry-pick）

**`git diff HEAD upstream/26.1 -- <path>`** で差分確認 → NeoForge ブリッジを壊さない範囲で適用。Fabric 専用 import は移植時に除去。

| ID | 内容 | upstream 参照 | 優先度 | 状態 |
|----|------|---------------|--------|------|
| UP-01 | **`RecipeCrafter` 26.1 リファクタ** | `ItemStackTemplate` 出力、`ValueInput`/`ValueOutput`、`energy` フィールド、サウンド整理 | `RecipeCrafter.java` | 高 | **完了**（upstream ロジック移植済み。`poweredMachine` → `energy`） |
| UP-02 | **`RebornRecipeDisplay`** | レシピトースト `RecipeDisplay` / `SlotDisplay.ItemStackSlotDisplay` | 新規ファイル | 中 | **完了** |
| UP-03 | **流体コンテナ Ingredient** | `FluidContainerIngredient`（Fabric `CustomIngredient`）→ NeoForge 向け独自 Ingredient または `RecipeIngredientCompat` 拡張 | `FluidContainerIngredient.java` | 高 | **完了**（`FluidContainerIngredient` + `RecipeIngredientCompat` が `fabric:components` / `reborncore:fluid_container` をデコード。`MixinIngredientCodec` + `MixinCraftingResultSlot` でクラフト台レシピ・空容器返却対応） |
| UP-04 | **JEI 内蔵化** | `JEIPlugin` + category/transfer/subtype 一式。Fabric `ClientRecipeSynchronizedEvent` は NeoForge レシピ同期に置換 | `src/client/.../jei/**` | 中 | **部分完了**（`TechRebornJeiPlugin` + 3 category、`IRecipeHolderType` / `RecipeMap` / `RecipesReceivedEvent` / `OnDatapackSyncEvent.sendRecipes`。`build.gradle` コンパイル除外解除。subtype・GUI transfer・upstream 専用 category は未移植） |
| UP-05 | **`PaddedShapedRecipe` 維持判断** | upstream では削除。本フォークの `src/main/generated` / JSON に残存参照があれば **NeoForge 版を維持**し `ItemStackTemplate` 化 | `PaddedShapedRecipe.java` | 中 | **要確認** |
| UP-06 | **ホログラム描画 26.1 化** | `HologramRenderer` record 化、`BlockModelResolver` / `HologramRenderState` | `HologramRenderer.java` 等 | 中 | **完了** |
| UP-07 | **`ChunkPosMultiMap` / `ChunkEventListeners`** | チャンクイベント集約（チャンクローダー等） | `reborncore.common.misc.world.*` | 中 | **RebornCore 完了**（upstream 取込） |
| UP-08 | **JSpecify `@NullMarked`** | 全 `package-info.java` — コンパイル優先度は低 | upstream 各 package | 低 | **未実施** |
| UP-09 | **機械筐体アセット改名** | `tier1/2/3_casing` → `basic/advanced/industrial_machine_casing`（+ `_start/_middle/_end`） | textures + JSON + `MachineCasingModel` | 中 | **完了** |
| UP-10 | **核反応炉コンテンツ一式** | ブロック・BE・アイテム・レシピ・流体・GUI・JEI カテゴリ | `feature/nuclear-reactor-recipes` マージ群 | 中 | **完了**（Java/GUI/登録/レシピ/テクスチャ/lang。JEI カテゴリは UP-04 待ち） |
| UP-11 | **Recycler 無限ループ修正** | blacklist / upgrade 尊重（#3507） | `RecyclerRecipeCrafter.java` | 高 | **完了** |
| UP-12 | **バグ修正バックポート** | 温室スイカ収穫 #3472、ソーラー発電量 #3493、Matter Fabricator 常時 OFF #3470、`RebornInventory#setHasChanged` typo #3490、セル/バケツ透明度 #813b4c6fa 等 | upstream 直近コミット | 中 | **完了**（#3470 `updateState`、#3493 `EnvironmentAttributes.SUN_ANGLE`、#3490 typo 修正、#813b4c6fa cutout 強制） |
| UP-13 | **レシピ・タグ・datagen 資産** | 石炭/木炭ダスト互換 #3494、核関連 UU レシピ、反応炉部品クラフトレシピ、流体レシピ JSON 更新 | `src/main/resources/data/**` | 中 | **完了**（#3494 + uranium ore UU + 反応炉部品 28 件 + upstream `536ef5b36` 流体クラフト 7 件: paper / carbon_fiber / coolant cells / thick_neutron_reflector） |
| UP-14 | **村人取引** | upstream は **無効化済み**。本フォークは **NeoForge 実装を維持** → VIL-01 と統合（upstream のコメントアウトをマージしない） | `TRVillager.java` | — | **意図的スキップ** |
| UP-15 | **Client GameTest** | upstream CI: `runClientGameTest` | `.github/workflows/check.yml` 参考 | 低 | **未実施** |

**upstream 取り込み手順（推奨）**

1. `git diff HEAD upstream/26.1 -- <対象パス>` で差分を確認する。
2. **NeoForge 専用ファイル**（`*NeoForge*`, `*Bridge*`, `neoforge.mods.toml`）は upstream 変更を **適用しない**。
3. **ゲームロジック**（BE、レシピ、items、resources）は upstream を優先し、コンパイルエラーを §5.2 / §8.1 の ID で解消する。
4. Fabric import（`net.fabricmc.*`）は **必ず NeoForge / バニラ / 既存ブリッジ**に置換してからコミットする。

---

## 9. Data Components マッピング（継承）

1.21.1 仕様書 §11 を **そのまま適用**。26.1.2 で追加されるルール:

- レシピ JSON・codec の **不変出力**は `ItemStackTemplate` で表現し、実行時に `create()` で `ItemStack` 化する。
- `DataComponentPatch#get` は `DataComponentGetter` を受け取る（Arclight 26.1.2 実測で存続）。
- 新規アイテム状態は引き続き **Raw NBT on ItemStack 禁止**。

| Component ID | 型 | 用途 |
|--------------|-----|------|
| `techreborn:is_active` | `Boolean` | マシンアクティブ |
| `techreborn:aoe5` | `Boolean` | ツール AOE |
| `techreborn:frequency_transmitter` | `GlobalPos` | 周波数送信機 |
| `techreborn:painting_cover` | `BlockState` | 塗装カバー |
| `techreborn:fluid` | `Holder<Fluid>` | 動的セル |
| `techreborn:stored_heat` | `Integer` | 反応炉部品耐久・熱 |
| `techreborn:fuel_remaining` | `Integer` | 燃料棒残り |
| `team_reborn_energy:energy` | `Long` | アイテム EU |
| `minecraft:block_entity_data` | `CustomData` | BE ポータブル |

---

## 10. Arclight 向け検証

| ID | 検証項目 | 手順 | 状態 |
|----|----------|------|------|
| A-01 | mod ロード | RebornCore dev server `Done (0.325s)` 確認（2026-06-15）。Arclight 実機は未 | **部分完了** |
| A-02 | ブロック設置・破壊 | 機械・蓄電・タンク — BE → Item `BLOCK_ENTITY_DATA` | **未実施** |
| A-03 | 流体・レシピ | 動的セル、産業レシピ、JEI/REI | **未実施** |
| A-04 | エネルギー・ケーブル | 充放電・ケーブル接続 | **未実施** |
| A-05 | 村人 | 冶金・電気職業・放浪商人（VIL-xx 完了後） | **未実施** |
| A-06 | ワールド生成 | オーラ鉱石・ゴム | **未実施** |
| A-07 | チャンクローダー | `ChunkLoaderManager` + 新 `ChunkPos` API | **未実施** |

### 10.1 実機手順

1. `./gradlew jar` → `build/libs/techreborn-*.jar`
2. Arclight `build/libs/arclight-neoforge-26.1.2-1.0.2-SNAPSHOT.jar` でサーバ起動
3. `mods/` に TR JAR のみ（RebornCore 別 JAR 不要）
4. `eula=true`、同梱 NeoForge ≥ `gradle.properties` の `neo_version`
5. 補助: `./gradlew prepareNeoForgeSmokeMods` → `build/smoke-neoforge/mods/`

---

## 11. 完了定義

1. **`compileJava` / `build` 成功**（Java **25** ツールチェーン）。
2. ソースに **ItemStack 廃止 NBT API ゼロ**（1.21.1 準拠を維持）。
3. **`ResourceLocation` ゼロ** → 全て **`Identifier`**（Mojang 公式名）。
4. レシピ型が **`ItemStackTemplate`** に追随。
5. Arclight **26.1.2** 上で **A-01〜A-04** を最低限実施。
6. 1.21.1 単一 JAR 配布形態を維持。

---

## 12. 既知のリスク・注意

| リスク | 説明 | 緩和 |
|--------|------|------|
| 中間版の積み重ね | 1.21.1→26.1.2 は 21.2〜21.11 の変更を含む | [26.1 Release Notes](https://neoforged.net/news/26.1release/) の中間リンクを順に確認 |
| `IItemHandler` 廃止 | Transfer API 全面移行の工数 | `TransferApiBridge` に集約して段階移行 |
| 村人取引 rewrite | `TradeUtils` 全面破綻の可能性 | データパック化（VIL-01）を早期にプロトタイプ |
| JEI/REI/PAL 未リリース | compileOnly 依存が解決不能 | 一時除外してサーバ JAR を優先 |
| upstream Fabric 26.1 | 本リポとはビルド系が非互換 | **マージしない**。API パターンのみ参考 |
| Arclight + 新 Transfer API | Bukkit ブリッジとの相互作用 | Arclight 仕様書 §0.1 の NF 変更を照合 |
| upstream Fabric マージ | `build.gradle` / `fabric.mod.json` の誤マージ | **ビルド系は手動更新のみ**（§5.5） |
| 村人 upstream との乖離 | upstream は取引無効、本フォークは有効 | **UP-14: upstream のコメントアウトを採用しない** |
| GUI 大量変更 | ~60 画面 | `GuiBase` 修正後に機械的置換 |
| TeamReborn Energy 5.0.0 | 26.1 Data Component 互換 | B-06 で早期検証 |

---

## 13. 実装フェーズ（推奨順）

| フェーズ | ID | 内容 |
|--------|-----|------|
| 0 | — | 本仕様書のレビュー・合意（**現在地**） |
| 1 | B-01〜B-12 | ビルド系を 26.1.2 に上げ、最初の `compileJava` 失敗ログを取得 |
| 2 | ID-01 | `Identifier` 一括置換 |
| 3 | REC-01, REC-02, **UP-01** | レシピ・`RecipeCrafter`（upstream パターン） |
| 4 | ITM-01, NF-01, CAP-01, CHK-01, **UP-03, UP-11** | ゲームロジック API + 重要バグ修正 |
| 5 | CLI-01〜CLI-05, **UP-06** | クライアント |
| 6 | B-09, B-10 | フルビルド |
| 7 | A-01〜A-04 | Arclight スモーク |
| 8 | **UP-09, UP-10, UP-12, UP-13** | upstream コンテンツ・資産同期 |
| 9 | VIL-01〜03, WLD-01 | データパック・村人（upstream は無効 — 本フォーク独自） |
| 10 | UP-04, TR-D01, TR-G01, TR-P01, UP-15 | JEI / Datagen / GameTest / 長期 |

### 13.1 推奨デバッグワークフロー

**NeoForge 公式（バニラ API）**

1. 26.1.2 でコンパイルエラーになった呼び出しを特定する。
2. 同一呼び出しを 1.21.1 ワークスペースで検索し、バニラの使用例へジャンプする。
3. 26.1 バニラソースで同箇所を比較し、同様の修正を mod に適用する。

**公式 upstream TechReborn（ゲームロジック）**

1. `git diff HEAD upstream/26.1 -- path/to/File.java` で upstream の修正意図を確認する。
2. Fabric 依存部分を除き、ロジック差分を本フォークに手動適用する。
3. NeoForge ブリッジ（`TransferApiBridge` 等）経由の呼び出しは upstream の Fabric Transfer API を **そのまま持ち込まない**。

---

## 14. 改訂履歴

| 日付 | 内容 |
|------|------|
| 2026-06-17 | **第12版** — **UP-04 部分完了**: JEI コンパイル除外解除。`IRecipeHolderType` / `add()` / `addCraftingStation` / `ItemStackTemplate.create()` へ 26.1 JEI API 追随。クライアントレシピ同期: `OnDatapackSyncEvent.sendRecipes` + `RecipesReceivedEvent` + `IRecipeManager.addRecipes`。`./gradlew build` 成功。次: Arclight 実機、JEI subtype/transfer |
| 2026-06-17 | **第11版** — **UP-13 完了**: upstream `536ef5b36` の流体クラフトレシピ 7 件を取り込み。**UP-03 強化**: NeoForge `FluidContainerIngredient`（`ICustomIngredient`）登録、`RecipeIngredientCompat` が `reborncore:fluid_container` をデコード、`MixinIngredientCodec` / `MixinCraftingResultSlot`（26.1.2 API + 空容器返却）。`./gradlew build` 成功。次: Arclight 実機、UP-04 JEI |
| 2026-06-17 | **第10版** — **UP-10 完了**: upstream から核反応炉一式を NeoForge 層へ移植（`NuclearReactorBlockEntity`/`ReactorChamberBlockEntity`、部品アイテム、`NuclearReactorComponents` enum、`GuiNuclearReactor`、`stored_heat`/`fuel_remaining` Data Components、部品レシピ 28 件）。**UP-06** 完了扱いに更新。`./gradlew build` 再確認。次: Arclight 実機、UP-04 JEI、UP-13 流体レシピ残り |
| 2026-06-16 | **第9版** — UP-12 実装確認で完了扱いへ更新。UP-13: uranium_ore / deepslate_uranium_ore UU レシピ追加。次: UP-10（核反応炉コンテンツ）方針確定 |
| 2026-06-15 | **第8版** — **UP-12/13 部分**: Matter Fabricator `updateState`（#3470）、`data/c/tags/item/dusts/coal.json`（#3494）。UP-01/02/REC-01 を完了に更新。dev server `Done (0.325s)` 再確認。次: Arclight 実機、UP-09/10/12/13 |
| 2026-06-15 | **第7版** — **VIL-01/02 完了**: 冶金・電気村人の `villager_trade`/`trade_set` データパック（1.21.1 取引内容を移植）、`TRVillager.tradeSetsByLevel` 紐付け。放浪商人ゴム苗木は `minecraft` タグへ追加。**UP-11**: `RecyclerRecipeCrafter` — `canRecycle` + 無限ループ修正。次: Arclight 実機、UP-01/12/13 |
| 2026-06-14 | **第6版** — **Phase 1 完了**: `./gradlew build` 成功。`TicketType` を `RegisterEvent` 登録に修正（`Registry is already frozen` 解消）。RebornCore dev server `Done` 確認。次: Arclight 実機（A-01〜A-04）、VIL-01、UP-* |
| 2026-06-13 | **第5版** — **`:RebornCore:compileJava` 成功**。残14エラー修正: `TagValueInput`+`loadWithComponents`、`ClientPacketDistributor`、`FluidTintSource`/`AtlasManager`、`addClientSystemMessage`、`FuelValues`/`getBurnTime`、`MixinGameRenderer` 削除（FOV は TODO）、`jei_version=29.5.0.28`。次: TR `src/` Phase 1 |
| 2026-06-13 | **第4版** — RebornCore クライアント GUI 26.1 化（CLI-01/02）、NeoForge 26.1.2 イベント API（NF-01 部分）、`RebornExplosion`/`PaddedShapedRecipe`/`Equippable`、upstream `MachineBaseBlockEntity`/multiblock/`ChunkEventListeners` 取込。残 ~100 エラー（Transfer/BE/RecipeCrafter） |
| 2026-06-13 | **第3版** — Phase 1 実装（B-01〜B-14、ID/REC/CHK/VIL 部分）。`:RebornCore:compileJava` まで到達、残 ~100 エラー（GUI/レンダリング） |
| 2026-06-13 | **第2版** — upstream `26.1` 差分を §5.5・§8.6 に追加 |
| 2026-06-13 | **初版** — 1.21.1 仕様書を継承し 26.1.2 向けに再構成 |

---

## 付録 A — 現行ソースの grep サマリ（2026-06-13）

| パターン | 概算 | 26.1.2 対応 ID |
|----------|------|----------------|
| `ResourceLocation` | ~100 ファイル | ID-01 |
| `InteractionResultHolder` | 13 ファイル | ITM-01 |
| `IItemHandler` / `SidedInvWrapper` | 3 ファイル（集中: `TransferApiBridge`） | CAP-01 |
| `VillagerTrades` / `TradeUtils` | 6 ファイル | VIL-01, VIL-02 |
| `GuiGraphics` / `renderBg` / `renderLabels` | ~60 ファイル | CLI-01, CLI-02 |
| `ChunkPos` 旧コンストラクタ | 5 ファイル | CHK-01 |
| `getTag()` / `setTag()` on ItemStack | **0** | RV-01 |
| `MobSpawnType` | **0** | — |

---

## 付録 B — 1.21.1 仕様書との対応

| 1.21.1 文書 | 26.1.2 文書 |
|-------------|-------------|
| §5 Data Components 方針 | §5.2 継承 |
| §7 API 対応表 | §6 拡張 |
| §8 B-xx ビルド | §7 B-xx（バージョン更新） |
| §9 TR-xx ソース | §8 + RV-xx |
| §12 Arclight 検証 | §10（Arclight 26.1.2） |
| 付録 D BuiltInRegistries | §8.3 NBT-01 |
| 付録 E upstream 差分 | §5.5, §8.6 |

---

## 付録 D — 公式 upstream `26.1` 差分サマリ（2026-06-13）

**比較コマンド:** `git fetch upstream 26.1` → `git diff --stat upstream/26.1 HEAD`

| 区分 | upstream のみ（本フォークに無い） | 本フォークのみ（upstream に無い） |
|------|----------------------------------|----------------------------------|
| ビルド | Fabric Loom、`fabric.mod.json`、`runDatagen` / `runClientGameTest` | NeoGradle、`neoforge.mods.toml`、Jar-in-Jar、`prepareNeoForgeSmokeMods` |
| ローダー層 | Fabric API（BiomeModifications、CustomIngredient、Transfer API） | `*NeoForge*`、`*Bridge*`、`RegisterEvent`、`TransferApiBridge`（Capabilities） |
| クライアント | 内蔵 JEI 一式（`JEIPlugin` + categories）、`HologramRenderState` | `TechRebornJeiPlugin`（旧）、`NeoForgeMachineCasingModelBridge`、REI 互換 |
| レシピ | `FluidContainerIngredient`、`RebornRecipeDisplay`；**`PaddedShapedRecipe` 削除** | `RecipeIngredientCompat`、`PaddedShapedRecipe`（NeoForge 登録） |
| コンテンツ | **核反応炉一式**、機械筐体テクスチャ改名、大量 datagen/JSON 更新 | — |
| 村人 | **`TRVillager` コメントアウト（TODO）** | `NeoForgeVillagerBridge` + 有効な職業/取引 |
| ワールド | `WorldGenerator` + Fabric `BiomeModifications` | `NeoForgeBiomeModifierPack` + ランタイム datapack |
| 品質 | JSpecify `package-info`、`ChunkPosMultiMap`、Client GameTest | Mixin `JAVA_21`、Arclight 向け `TagsUpdatedEvent` 監査メモ |

**upstream 直近の機能・修正コミット（取り込み候補）**

| コミット / Issue | 内容 | UP-ID |
|------------------|------|-------|
| `982296723` / #3507 | Recycler blacklist・upgrade・無限ループ修正 | UP-11 |
| `07e338567` / #3494 | 石炭/木炭ダスト互換 | UP-13 |
| `e0a454c72` / #3472 | 温室スイカ収穫 | UP-12 |
| `355f543c9` / #3493 | ソーラー発電量 | UP-12 |
| `17cd81d58` / #3492 | JEI 統合修正 | UP-04 |
| `5d7fef9cc` | カスタム流体 Ingredient | UP-03 |
| `8ce08733a` / `ba6ad223c` | ホログラム描画・キャッシュ | UP-06 |
| `79d0b16b1`〜`133058b64` | 核反応炉レシピ・コンテンツ | UP-10 |
| `fc813a3af` | 機械筐体モデル・テクスチャ 26.1.1 修正 | UP-09 |

---

## 付録 C — 実装時の禁止・推奨（1.21.1 から継承 + 26.1.2 追加）

**禁止**

- [x] `ItemStack` の `getTag` / `setTag` / `hasTag` / `getOrCreateTag`
- [ ] `net.minecraftforge.*` の新規 import
- [ ] `net.fabricmc.*` の新規 import（本番 Java）
- [ ] `@Mod.EventBusSubscriber` の新規使用
- [ ] `RegisterEvent` 外での `Registry.register`
- [ ] Arclight 専用パッケージへの依存
- [ ] **`ResourceLocation` の新規使用**（`Identifier` を使う）

**推奨**

- [ ] `Identifier.fromNamespaceAndPath("techreborn", path)`
- [ ] 不変レシピ結果は **`ItemStackTemplate`**
- [ ] 実行時スタックは `ItemStack` + Data Components
- [ ] ネットワーク: `CustomPacketPayload` + `StreamCodec`
- [ ] タグリロード: `TagsUpdatedEvent#getRegistryAccess()`
- [ ] Arclight 検証は単一 TR JAR
- [ ] バニラ 26.1 ソースと並行ワークスペースで差分確認
- [ ] upstream 取り込み前に `git diff HEAD upstream/26.1 -- <path>` で NeoForge 層との衝突を確認
- [ ] Fabric import を cherry-pick に混入させない

---

*本ドキュメントは修正作業の指示書であり、実装進捗は §7〜§8 の ID 状態列で追跡する。実装着手時は本書を適宜更新すること。*
