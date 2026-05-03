#Requires -Version 5.1
<#
.SYNOPSIS
  Build TechReborn + RebornCore and stage jars for Arclight / NeoForge dedicated-server smoke tests.

.DESCRIPTION
  1) Runs Gradle jar for root and :RebornCore
  2) Copies mod jars into build/smoke-neoforge/mods (same layout as prepareNeoForgeSmokeMods)
  3) Prints next steps: drop Arclight server jar, accept EULA, run java -jar

  For NeoForge-only smoke (no Arclight), from repo root:
    .\gradlew.bat :RebornCore:runServer
  Wait for log line: "Done (...)! For help, type \"help\"" then stop the process.

.PARAMETER ArclightJar
  Optional path to Arclight server jar (e.g. arclight-neoforge-1.21.1-*.jar). If set, copies it to the smoke folder.

.PARAMETER SkipBuild
  Skip Gradle jar tasks (reuse existing build/libs outputs).
#>
param(
	[string] $ArclightJar = "",
	[switch] $SkipBuild
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

if (-not $SkipBuild) {
	& "$root\gradlew.bat" jar ":RebornCore:jar" --no-daemon
}

$modsDir = Join-Path $root "build\smoke-neoforge\mods"
New-Item -ItemType Directory -Force -Path $modsDir | Out-Null

$trJar = Get-ChildItem -Path (Join-Path $root "build\libs") -Filter "techreborn-*.jar" -ErrorAction SilentlyContinue |
	Sort-Object LastWriteTime -Descending | Select-Object -First 1
$rcJar = Get-ChildItem -Path (Join-Path $root "RebornCore\build\mod-jar") -Filter "reborncore-*.jar" -ErrorAction SilentlyContinue |
	Sort-Object LastWriteTime -Descending | Select-Object -First 1

if (-not $trJar -or -not $rcJar) {
	Write-Error "Could not find built jars. Run: .\gradlew.bat jar :RebornCore:jar"
}

Copy-Item -Force $trJar.FullName (Join-Path $modsDir $trJar.Name)
Copy-Item -Force $rcJar.FullName (Join-Path $modsDir $rcJar.Name)

if ($ArclightJar -ne "") {
	if (-not (Test-Path $ArclightJar)) { Write-Error "Arclight jar not found: $ArclightJar" }
	Copy-Item -Force $ArclightJar (Join-Path (Join-Path $root "build\smoke-neoforge") (Split-Path -Leaf $ArclightJar))
}

Write-Host ""
Write-Host "Staged mods under: $modsDir"
Write-Host "- $($trJar.Name)"
Write-Host "- $($rcJar.Name)"
Write-Host ""
Write-Host "NeoForge dev smoke (RebornCore only, no Arclight):"
Write-Host "  cd RebornCore ; ..\gradlew.bat :RebornCore:runServer"
Write-Host "  (watch for: Done (...)! For help, type `"help`")"
Write-Host ""
Write-Host "Arclight: copy staged jars into the server's mods folder, ensure NeoForge >= 21.1.219 per gradle.properties,"
Write-Host "set eula=true, then start your Arclight jar per upstream docs: https://github.com/IzzelAliz/Arclight"
Write-Host ""
