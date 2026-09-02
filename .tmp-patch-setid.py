#!/usr/bin/env python3
"""Patch TRContent/ModRegistry/ModFluids to pass block names for MC 26 setId()."""
from pathlib import Path
import re

ROOT = Path("/mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1")

def patch_trcontent(text: str) -> str:
    # Solar panel
    text = text.replace(
        "block = new BlockSolarPanel(this);",
        'block = new BlockSolarPanel(this, name + "_solar_panel");',
    )
    # Tank unit
    text = text.replace(
        "block = new TankUnitBlock(this);",
        'block = new TankUnitBlock(this, name + "_tank_unit");',
    )
    # Ore
    text = text.replace(
        "TRBlockSettings.ore(name.startsWith(\"deepslate\"))",
        'TRBlockSettings.ore(name.startsWith("deepslate"), name + "_ore")',
    )
    # Storage block stairs/slab/wall
    text = text.replace(
        "stairsBlock = new TechRebornStairsBlock(block.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(block));",
        'stairsBlock = new TechRebornStairsBlock(block.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(block).setId(TRBlockSettings.key(name + "_storage_block_stairs")));',
    )
    text = text.replace(
        "slabBlock = new SlabBlock(BlockBehaviour.Properties.ofFullCopy(block));",
        'slabBlock = new SlabBlock(BlockBehaviour.Properties.ofFullCopy(block).setId(TRBlockSettings.key(name + "_storage_block_slab")));',
    )
    text = text.replace(
        "wallBlock = new WallBlock(BlockBehaviour.Properties.ofFullCopy(block));",
        'wallBlock = new WallBlock(BlockBehaviour.Properties.ofFullCopy(block).setId(TRBlockSettings.key(name + "_storage_block_wall")));',
    )
    # Storage block ctor
    text = text.replace(
        "block = new BlockStorage(isHot, hardness, resistance);",
        'block = new BlockStorage(isHot, hardness, resistance, name + "_storage_block");',
    )

    # Machine frames/casings
    text = text.replace(
        "frame = new BlockMachineFrame();",
        'frame = new BlockMachineFrame(name + "_machine_frame");',
    )
    text = text.replace(
        "casing = new BlockMachineCasing(casingHeatCapacity);",
        'casing = new BlockMachineCasing(casingHeatCapacity, name + "_machine_casing");',
    )
    # Cables
    text = re.sub(
        r"block = new CableBlock\(this\);",
        'block = new CableBlock(this, name + "_cable");',
        text,
    )

    # GenericMachineBlock(gui, ctor) -> add name from enum constant (line-based)
    def add_name_arg(line: str, class_name: str) -> str:
        m = re.match(
            rf'^(\t+)([A-Z0-9_]+)\(new {class_name}\((.*)\)\),?\s*$',
            line,
        )
        if not m:
            return line
        indent, enum_name, args = m.group(1), m.group(2), m.group(3)
        if re.search(r',\s*"[^"]+"\s*$', args):
            return line
        comma = "," if line.rstrip().endswith(",") else ""
        return f'{indent}{enum_name}(new {class_name}({args}, "{enum_name.lower()}")){comma}\n'

    out_lines = []
    for line in text.splitlines(keepends=True):
        if "new GenericMachineBlock(" in line and ',"' not in line and '", "' not in line:
            line = add_name_arg(line, "GenericMachineBlock")
        elif "new GenericGeneratorBlock(" in line and ',"' not in line and '", "' not in line:
            line = add_name_arg(line, "GenericGeneratorBlock")
        out_lines.append(line)
    text = "".join(out_lines)

    # Zero-arg named blocks in Machine enum
    replacements = {
        "new ResinBasinBlock(ResinBasinBlockEntity::new)": 'new ResinBasinBlock(ResinBasinBlockEntity::new, "resin_basin")',
        "new IronAlloyFurnaceBlock()": 'new IronAlloyFurnaceBlock("iron_alloy_furnace")',
        "new IronFurnaceBlock()": 'new IronFurnaceBlock("iron_furnace")',
        "new BlockFusionCoil()": 'new BlockFusionCoil("fusion_coil")',
        "new BlockFusionControlComputer()": 'new BlockFusionControlComputer("fusion_control_computer")',
        "new BlockAlarm()": 'new BlockAlarm("alarm")',
        "new PlayerDetectorBlock()": 'new PlayerDetectorBlock("player_detector")',
        "new LampBlock(": None,  # handle separately
    }
    for old, new in replacements.items():
        if new:
            text = text.replace(old, new)

    # Lamps
    text = text.replace(
        "new LampBlock(4, 10, 8)",
        'new LampBlock(4, 10, 8, "lamp_incandescent")',
    )
    text = text.replace(
        "new LampBlock(1, 1, 12)",
        'new LampBlock(1, 1, 12, "lamp_led")',
    )
    # Remove broken FIXME path if present
    text = text.replace(', "FIXME_LAMP"', "")

    # Energy storage / transformers / reactor in Machine enum - from 6.0.2 naming
    energy_map = {
        "new LowVoltageSUBlock()": 'new LowVoltageSUBlock("low_voltage_su")',
        "new MediumVoltageSUBlock()": 'new MediumVoltageSUBlock("medium_voltage_su")',
        "new HighVoltageSUBlock()": 'new HighVoltageSUBlock("high_voltage_su")',
        "new AdjustableSUBlock()": 'new AdjustableSUBlock("adjustable_su")',
        "new InterdimensionalSUBlock()": 'new InterdimensionalSUBlock("interdimensional_su")',
        "new LapotronicSUBlock()": 'new LapotronicSUBlock("lapotronic_su")',
        "new LSUStorageBlock()": 'new LSUStorageBlock("lsu_storage")',
        "new BlockLVTransformer()": 'new BlockLVTransformer("lv_transformer")',
        "new BlockMVTransformer()": 'new BlockMVTransformer("mv_transformer")',
        "new BlockHVTransformer()": 'new BlockHVTransformer("hv_transformer")',
        "new BlockEVTransformer()": 'new BlockEVTransformer("ev_transformer")',
        "new NuclearReactorBlock()": 'new NuclearReactorBlock("nuke")' if False else 'new NuclearReactorBlock("nuclear_reactor")',
        "new ReactorChamberBlock()": 'new ReactorChamberBlock("reactor_chamber")',
    }
    for old, new in energy_map.items():
        text = text.replace(old, new)

    return text


def patch_modregistry(text: str) -> str:
    pairs = [
        ('new BlockComputerCube()', 'new BlockComputerCube("computer_cube")'),
        ('new BlockNuke()', 'new BlockNuke("nuke")'),
        ('new BlockRefinedIronFence()', 'new BlockRefinedIronFence("refined_iron_fence")'),
        ('new BlockReinforcedGlass()', 'new BlockReinforcedGlass("reinforced_glass")'),
        ('new BlockRubberLeaves()', 'new BlockRubberLeaves("rubber_leaves")'),
        ('new BlockRubberLog()', 'new BlockRubberLog("rubber_log")'),
        ('new BlockRubberPlank()', 'new BlockRubberPlank("rubber_planks")'),
        ('new BlockRubberSapling()', 'new BlockRubberSapling("rubber_sapling")'),
        ('new BlockRubberPlankStair()', 'new BlockRubberPlankStair("rubber_stair")'),
        ('new RubberTrapdoorBlock()', 'new RubberTrapdoorBlock("rubber_trapdoor")'),
        ('new RubberButtonBlock()', 'new RubberButtonBlock("rubber_button")'),
        ('new RubberPressurePlateBlock()', 'new RubberPressurePlateBlock("rubber_pressure_plate")'),
        ('new RubberDoorBlock()', 'new RubberDoorBlock("rubber_door")'),
        ('TRBlockSettings.rubberLogStripped()', 'TRBlockSettings.rubberLogStripped("rubber_log_stripped")'),
        ('TRBlockSettings.rubberWoodStripped()', 'TRBlockSettings.rubberWoodStripped("rubber_wood")'),  # careful - two uses
        ('TRBlockSettings.rubberSlab()', 'TRBlockSettings.rubberSlab("rubber_slab")'),
        ('TRBlockSettings.rubberFence()', 'TRBlockSettings.rubberFence("rubber_fence")'),
        ('TRBlockSettings.rubberFenceGate()', 'TRBlockSettings.rubberFenceGate("rubber_fence_gate")'),
        ('TRBlockSettings.pottedRubberSapling()', 'TRBlockSettings.pottedRubberSapling("potted_rubber_sapling")'),
        ('TRBlockSettings.copperWall()', 'TRBlockSettings.copperWall("copper_wall")'),
    ]
    for old, new in pairs:
        text = text.replace(old, new)
    # Fix stripped rubber wood which also used rubberWoodStripped("rubber_wood") incorrectly
    text = text.replace(
        'new RotatedPillarBlock(TRBlockSettings.rubberWoodStripped("rubber_wood")), "stripped_rubber_wood")',
        'new RotatedPillarBlock(TRBlockSettings.rubberWoodStripped("stripped_rubber_wood")), "stripped_rubber_wood")',
    )
    return text


def patch_modfluids(text: str) -> str:
    text = text.replace(
        "TRBlockSettings.fluid()",
        'TRBlockSettings.fluid(identifier.getPath())',
    )
    # Also bucket Item.Properties may need setId - leave for compile
    return text


def main():
    for rel, fn in [
        ("src/main/java/techreborn/init/TRContent.java", patch_trcontent),
        ("src/main/java/techreborn/events/ModRegistry.java", patch_modregistry),
        ("src/main/java/techreborn/init/ModFluids.java", patch_modfluids),
    ]:
        path = ROOT / rel
        orig = path.read_text(encoding="utf-8")
        new = fn(orig)
        if new == orig:
            print(f"NO CHANGE {rel}")
        else:
            path.write_text(new, encoding="utf-8", newline="\n")
            print(f"patched {rel} delta={len(new)-len(orig)}")

if __name__ == "__main__":
    main()
