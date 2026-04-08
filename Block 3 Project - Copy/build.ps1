# Build and run JDM JavaFX desktop app (JDK 17+).
# Set $env:JAVAFX_SDK to your javafx-sdk-XX\lib folder if not next to PROJECT.

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot

$JFX = if ($env:JAVAFX_SDK) { $env:JAVAFX_SDK } else {
    Join-Path (Split-Path -Parent $ProjectRoot) "javafx-sdk-26\lib"
}
if (-not (Test-Path $JFX)) {
    Write-Error "JavaFX lib folder not found: $JFX (set JAVAFX_SDK)."
}

$Out = Join-Path $ProjectRoot "out"
New-Item -ItemType Directory -Force -Path $Out | Out-Null

$sources = Get-ChildItem -Path (Join-Path $ProjectRoot "src") -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
Write-Host "Compiling $($sources.Count) source file(s)..."
& javac --release 17 -encoding UTF-8 -d $Out `
    --module-path $JFX --add-modules javafx.controls,javafx.fxml `
    @sources
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Copy-Item (Join-Path $ProjectRoot "src\*.fxml") $Out -Force
Write-Host "Build OK. Run: java --module-path `"$JFX`" --add-modules javafx.controls,javafx.fxml -cp `"$Out`" jdm.fx.MainApp data"

if ($args -contains "-run") {
    $data = Join-Path $ProjectRoot "data"
    & java --module-path $JFX --add-modules javafx.controls,javafx.fxml -cp $Out jdm.fx.MainApp $data
}
