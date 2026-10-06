$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

$SdkPath = "$env:LOCALAPPDATA\Android\Sdk"
$CmdLineToolsPath = "$SdkPath\cmdline-tools\latest"
$ZipUrl = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
$ZipFile = "$env:TEMP\cmdline-tools-v2.zip"

if (-not (Test-Path $CmdLineToolsPath)) {
    Write-Host "Creando directorio del SDK en $CmdLineToolsPath..."
    New-Item -ItemType Directory -Force -Path $CmdLineToolsPath | Out-Null
    
    Write-Host "Descargando Android Command Line Tools..."
    Invoke-WebRequest -Uri $ZipUrl -OutFile $ZipFile -UseBasicParsing
    
    Write-Host "Extrayendo..."
    Expand-Archive -Path $ZipFile -DestinationPath "$env:TEMP\cmdline-tools-extracted" -Force
    
    Write-Host "Moviendo archivos a su ubicación definitiva..."
    Move-Item -Path "$env:TEMP\cmdline-tools-extracted\cmdline-tools\*" -Destination $CmdLineToolsPath -Force
    
    Remove-Item -Path $ZipFile -Force
    Remove-Item -Path "$env:TEMP\cmdline-tools-extracted" -Recurse -Force
}

$SdkManager = "$CmdLineToolsPath\bin\sdkmanager.bat"

Write-Host "Aceptando licencias del SDK..."
1..20 | ForEach-Object { "y" } | & $SdkManager --licenses


Write-Host "Instalando plataforma y herramientas (API 35)..."
& $SdkManager "platforms;android-35" "build-tools;35.0.0" "platform-tools"

$LocalProperties = "C:\Users\juand\entorno-android\local.properties"
$SdkDirEscaped = $SdkPath -replace "\\", "\\"
Set-Content -Path $LocalProperties -Value "sdk.dir=$SdkDirEscaped"

Write-Host "SDK configurado correctamente. Procediendo a compilar..."
