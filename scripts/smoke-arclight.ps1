#Requires -Version 5.1
<#
.SYNOPSIS
  Build TechReborn (RebornCore Jar-in-Jar) and stage a single mod JAR for Arclight / NeoForge smoke tests.

.DESCRIPTION
  1) Runs Gradle jar (unless -SkipBuild)
  2) Copies techreborn-*.jar into build/smoke-neoforge/mods (same as prepareNeoForgeSmokeMods)
  3) Optionally copies an Arclight server jar beside the mods folder
  4) Prints isolated-run steps (do not require a separate reborncore.jar)

  NeoForge-only smoke (no Arclight), from repo root:
    .\gradlew.bat :RebornCore:runServer
  Expect: Done (...)! For help, type "help" (techreborn + reborncore both load via modSource)

.PARAMETER ArclightJar
  Optional path to Arclight server jar (e.g. arclight-neoforge-26.1.2-1.0.2-SNAPSHOT.jar).

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
	& "$root\gradlew.bat" jar --no-daemon
}

$modsDir = Join-Path $root "build\smoke-neoforge\mods"
New-Item -ItemType Directory -Force -Path $modsDir | Out-Null

$trJar = Get-ChildItem -Path (Join-Path $root "build\libs") -Filter "techreborn-*.jar" -ErrorAction SilentlyContinue |
	Sort-Object LastWriteTime -Descending | Select-Object -First 1

if (-not $trJar) {
	Write-Error "Could not find built TechReborn jar. Run: .\gradlew.bat jar"
}

# Single-JAR distribution: RebornCore is Jar-in-Jar. Do not stage a separate reborncore.jar.
Get-ChildItem -Path $modsDir -Filter "reborncore-*.jar" -ErrorAction SilentlyContinue | Remove-Item -Force
Copy-Item -Force $trJar.FullName (Join-Path $modsDir $trJar.Name)

$smokeRoot = Join-Path $root "build\smoke-neoforge"
if ($ArclightJar -ne "") {
	if (-not (Test-Path $ArclightJar)) { Write-Error "Arclight jar not found: $ArclightJar" }
	Copy-Item -Force $ArclightJar (Join-Path $smokeRoot (Split-Path -Leaf $ArclightJar))
}

Write-Host ""
Write-Host "Staged mods under: $modsDir"
Write-Host "- $($trJar.Name) (RebornCore embedded via Jar-in-Jar)"
Write-Host ""
Write-Host "NeoForge versions: Minecraft 26.1.2 / NeoForge 26.1.2.103 (see gradle.properties)"
Write-Host ""
Write-Host "NeoForge dev smoke:"
Write-Host "  .\gradlew.bat :RebornCore:runServer"
Write-Host "  .\gradlew.bat :RebornCore:runClient"
Write-Host ""
Write-Host "Arclight isolated smoke example:"
Write-Host "  1) Copy $($trJar.Name) into <arclight-run>/mods/"
Write-Host "  2) Set eula=true; ensure NeoForge >= 26.1.2.103"
Write-Host "  3) java -Xmx4G -Darclight.alwaysExtract=true -jar arclight-neoforge-26.1.2-*.jar nogui"
Write-Host "  Docs: https://github.com/IzzelAliz/Arclight"
Write-Host ""
