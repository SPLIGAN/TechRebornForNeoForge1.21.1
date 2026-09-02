#!/usr/bin/env python3
"""Write a minimal 7x7x7 air structure NBT for TechReborn GameTests."""
import gzip
import struct
from pathlib import Path

OUT = Path("/mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1/src/gametest/resources/data/techreborn/structure/empty.nbt")


def write_string(buf: bytearray, s: str) -> None:
	b = s.encode("utf-8")
	buf += struct.pack(">H", len(b))
	buf += b


def write_named(buf: bytearray, tag_type: int, name: str) -> None:
	buf.append(tag_type)
	write_string(buf, name)


def main() -> None:
	# Root TAG_Compound ""
	root = bytearray()
	root.append(0x0A)  # TAG_Compound
	write_string(root, "")

	# size: TAG_List of 3 TAG_Int -> 7,7,7
	write_named(root, 0x09, "size")  # TAG_List
	root.append(0x03)  # element type TAG_Int
	root += struct.pack(">i", 3)
	root += struct.pack(">i", 7)
	root += struct.pack(">i", 7)
	root += struct.pack(">i", 7)

	# entities: empty TAG_List of TAG_Compound
	write_named(root, 0x09, "entities")
	root.append(0x0A)
	root += struct.pack(">i", 0)

	# blocks: empty TAG_List of TAG_Compound
	write_named(root, 0x09, "blocks")
	root.append(0x0A)
	root += struct.pack(">i", 0)

	# palette: empty TAG_List of TAG_Compound
	write_named(root, 0x09, "palette")
	root.append(0x0A)
	root += struct.pack(">i", 0)

	# DataVersion
	write_named(root, 0x03, "DataVersion")
	root += struct.pack(">i", 4552)  # near 26.1

	root.append(0x00)  # TAG_End for root

	OUT.parent.mkdir(parents=True, exist_ok=True)
	with gzip.open(OUT, "wb") as f:
		f.write(root)
	print(f"wrote {OUT} ({OUT.stat().st_size} bytes)")


if __name__ == "__main__":
	main()
