from pathlib import Path
src = Path("/mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1/.tmp-rebuild-setid.sh")
dst = Path("/tmp/rebuild-setid.sh")
dst.write_bytes(src.read_bytes().replace(b"\r\n", b"\n").replace(b"\r", b"\n"))
dst.chmod(0o755)
print("ok")
