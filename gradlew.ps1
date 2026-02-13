param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$GradleArgs
)

$ErrorActionPreference = "Stop"
$gradleVersion = "9.1.0"
$zipName = "gradle-$gradleVersion-bin.zip"
$baseDir = Join-Path $env:USERPROFILE ".gradle\wrapper\dists\gradle-$gradleVersion-bin\quickcart"
$gradleHome = Join-Path $baseDir "gradle-$gradleVersion"
$gradleBat = Join-Path $gradleHome "bin\gradle.bat"
$zipPath = Join-Path $baseDir $zipName
$downloadUrl = "https://services.gradle.org/distributions/$zipName"

if (-not (Test-Path $gradleBat)) {
    New-Item -ItemType Directory -Path $baseDir -Force | Out-Null

    if (-not (Test-Path $zipPath)) {
        Write-Host "Downloading Gradle $gradleVersion..."
        Invoke-WebRequest -Uri $downloadUrl -OutFile $zipPath
    }

    Write-Host "Extracting Gradle $gradleVersion..."
    Expand-Archive -Path $zipPath -DestinationPath $baseDir -Force
}

& $gradleBat @GradleArgs
exit $LASTEXITCODE
