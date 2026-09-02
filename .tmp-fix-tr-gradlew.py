from pathlib import Path

src = Path("/mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1/gradlew")
data = src.read_bytes()
print("win size", len(data), "dirname", b"dirname" in data, "warn", b"warn" in data)

dst = Path.home() / "tr-neoforge-build" / "gradlew"
fixed = data.replace(b"\r\n", b"\n").replace(b"\r", b"\n")
assert b"dirname" in fixed and b"warn" in fixed
dst.write_bytes(fixed)
dst.chmod(0o755)
print("restored", dst, len(fixed))
