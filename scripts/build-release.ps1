$ErrorActionPreference = 'Stop'

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$appOutput = Join-Path $projectRoot 'app'
$releaseOutput = Join-Path $projectRoot 'github-release'
$stage = Join-Path $projectRoot 'target\jpackage-input'
$appImage = Join-Path $appOutput 'ShopDesk'
$setupExe = Join-Path $releaseOutput 'ShopManagementSystem-1.0.0-setup.exe'
$generatedSetup = Join-Path $releaseOutput 'ShopManagementSystem-1.0.0.exe'
$iconPath = Join-Path $projectRoot 'target\shopdesk.ico'
$upgradeCode = 'f09a7e64-6c0e-4d71-a809-d7d4e4b47f1a'

$localWixCandidates = @((Join-Path $projectRoot '.build-tools\wix3'), (Join-Path $projectRoot 'tools\wix3'))
foreach ($localWix in $localWixCandidates) {
    if ((Test-Path (Join-Path $localWix 'candle.exe')) -and (Test-Path (Join-Path $localWix 'light.exe'))) {
        $env:Path = "$localWix;$env:Path"
        break
    }
}

$mavenCommand = Get-Command mvn.cmd -ErrorAction SilentlyContinue
if (-not $mavenCommand) { $mavenCommand = Get-Command mvn -ErrorAction SilentlyContinue }
if (-not $mavenCommand) {
    $mavenCache = Join-Path $env:USERPROFILE '.m2\wrapper\dists\apache-maven-3.9.14-bin'
    if (Test-Path $mavenCache) {
        $cachedMaven = Get-ChildItem -Path $mavenCache -Filter mvn.cmd -Recurse -File | Select-Object -First 1
        if ($cachedMaven) { $mavenCommand = @{ Source = $cachedMaven.FullName } }
    }
}
if (-not $mavenCommand) { throw 'Maven 3.9+ is required. Install Maven and put mvn on PATH.' }

$jpackageCommand = Get-Command jpackage.exe -ErrorAction SilentlyContinue
if (-not $jpackageCommand) { $jpackageCommand = Get-Command jpackage -ErrorAction SilentlyContinue }
if (-not $jpackageCommand -and $env:JAVA_HOME) {
    $jdkJpackage = Join-Path $env:JAVA_HOME 'bin\jpackage.exe'
    if (Test-Path $jdkJpackage) { $jpackageCommand = @{ Source = $jdkJpackage } }
}
if (-not $jpackageCommand) {
    $installedJdk = Get-ChildItem (Join-Path $env:ProgramFiles 'Java') -Directory -Filter 'jdk-*' -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending | ForEach-Object { Join-Path $_.FullName 'bin\jpackage.exe' } |
        Where-Object { Test-Path $_ } | Select-Object -First 1
    if ($installedJdk) { $jpackageCommand = @{ Source = $installedJdk } }
}
if (-not $jpackageCommand) { throw 'A JDK with jpackage is required.' }
$javaCommand = Join-Path (Split-Path -Parent $jpackageCommand.Source) 'java.exe'
if (-not (Test-Path $javaCommand)) { throw 'The JDK java.exe next to jpackage.exe is required to generate the branded icon.' }

$wixCandle = Get-Command candle.exe -ErrorAction SilentlyContinue
$wixLight = Get-Command light.exe -ErrorAction SilentlyContinue
if (-not $wixCandle -or -not $wixLight) {
    throw 'WiX Toolset 3.14.1+ is required to create the setup executable. Add candle.exe and light.exe to PATH.'
}
$wixVersionLine = (& $wixCandle.Source -? 2>&1 | Select-String -Pattern 'version\s+([0-9]+\.[0-9]+\.[0-9]+)' | Select-Object -First 1).Line
if ($wixVersionLine -notmatch 'version\s+([0-9]+\.[0-9]+\.[0-9]+)') { throw 'Could not determine the WiX version.' }
if ([version]$Matches[1] -lt [version]'3.14.1') { throw 'WiX 3.14.1 or later is required for a supported installer build.' }

& $mavenCommand.Source -f (Join-Path $projectRoot 'pom.xml') clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw 'Maven build failed.' }

& $javaCommand -cp (Join-Path $projectRoot 'target\ShopManagementSystem.jar') com.shop.ui.ShopLogo $iconPath
if ($LASTEXITCODE -ne 0 -or -not (Test-Path $iconPath)) { throw 'Could not generate the Shop Desk application icon.' }

if (Test-Path $stage) { Remove-Item -LiteralPath $stage -Recurse -Force }
New-Item -ItemType Directory -Path $stage -Force | Out-Null
Copy-Item -LiteralPath (Join-Path $projectRoot 'target\ShopManagementSystem.jar') -Destination $stage
Copy-Item -LiteralPath (Join-Path $projectRoot 'THIRD_PARTY_NOTICES.txt') -Destination $stage
New-Item -ItemType Directory -Path $appOutput -Force | Out-Null
New-Item -ItemType Directory -Path $releaseOutput -Force | Out-Null
if (Test-Path $appImage) { Remove-Item -LiteralPath $appImage -Recurse -Force }
if (Test-Path $setupExe) { Remove-Item -LiteralPath $setupExe -Force }
if (Test-Path $generatedSetup) { Remove-Item -LiteralPath $generatedSetup -Force }

& $jpackageCommand.Source --type app-image --name ShopDesk --app-version 1.0.0 `
    --vendor 'Shop Management System' --input $stage --main-jar ShopManagementSystem.jar `
    --main-class com.shop.App --icon $iconPath --dest $appOutput
if ($LASTEXITCODE -ne 0) { throw 'Could not create the portable app image.' }

& $jpackageCommand.Source --type exe --name ShopManagementSystem --app-version 1.0.0 `
    --vendor 'Shop Management System' --input $stage --main-jar ShopManagementSystem.jar `
    --main-class com.shop.App --icon $iconPath --dest $releaseOutput --win-menu --win-shortcut `
    --win-per-user-install --win-upgrade-uuid $upgradeCode
if ($LASTEXITCODE -ne 0) { throw 'Could not create the Windows installer. Verify WiX Toolset 3.14.1+ is installed and on PATH.' }
Move-Item -LiteralPath $generatedSetup -Destination $setupExe
if (-not (Test-Path $setupExe) -or (Get-Item -LiteralPath $setupExe).Length -lt 1000000) {
    throw 'The generated setup executable is missing or unexpectedly small.'
}

$hash = (Get-FileHash -LiteralPath $setupExe -Algorithm SHA256).Hash.ToLowerInvariant()
Set-Content -LiteralPath (Join-Path $releaseOutput 'SHA256SUMS.txt') -Value "$hash  ShopManagementSystem-1.0.0-setup.exe" -Encoding ascii
Copy-Item -LiteralPath (Join-Path $projectRoot 'THIRD_PARTY_NOTICES.txt') -Destination $releaseOutput
Set-Content -LiteralPath (Join-Path $releaseOutput 'README.txt') -Value @'
Shop Management System 1.0.0 for Windows x64

Run ShopManagementSystem-1.0.0-setup.exe to install for the current Windows user. No Java installation is required.
The installer includes the application and its Java runtime, creates a Start menu entry and desktop shortcut, and supports upgrades using the same app identity.
On first launch, create an administrator account with a username and a password of at least 12 characters.
If Windows shows a SmartScreen prompt, verify the SHA-256 value in SHA256SUMS.txt before choosing Run anyway.
See THIRD_PARTY_NOTICES.txt for bundled license references.
'@ -Encoding utf8

Write-Host "Portable app: $appImage"
Write-Host "GitHub release files: $releaseOutput"
