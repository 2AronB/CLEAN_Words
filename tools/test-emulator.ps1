param(
    [ValidatePattern('^emulator-[0-9]+$')][string]$Serial = 'emulator-5554',
    [string]$Adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
)
$ErrorActionPreference = 'Stop'
# Explicit serial prevents test runners from touching a connected personal phone.
$taskEmulatorFlag = & $Adb -s $Serial shell getprop ro.kernel.qemu
if ($LASTEXITCODE -ne 0 -or ($taskEmulatorFlag.Trim() -ne '1')) {
    throw 'An explicitly selected running emulator is required. No installation performed.'
}
$taskRoot = Split-Path -Parent $PSScriptRoot
Push-Location $taskRoot
try {
    & .\gradlew.bat --offline :app:assembleDebug :app:assembleDebugAndroidTest
    if ($LASTEXITCODE -ne 0) { throw 'Build failed; no installation performed.' }
    & $Adb -s $Serial install -r app\build\outputs\apk\debug\app-debug.apk
    if ($LASTEXITCODE -ne 0) { throw 'App install failed.' }
    & $Adb -s $Serial install -r app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk
    if ($LASTEXITCODE -ne 0) { throw 'Test install failed.' }
    New-Item -ItemType Directory -Path artifacts -Force | Out-Null
    $taskTestOutput = & $Adb -s $Serial shell am instrument -w dev.clean.words.test/androidx.test.runner.AndroidJUnitRunner 2>&1
    $taskExitCode = $LASTEXITCODE
    $taskTestOutput | Tee-Object -FilePath artifacts\F1-device-test.txt
    if ($taskExitCode -ne 0 -or (($taskTestOutput -join "`n") -notmatch 'OK \(\d+ tests?\)')) {
        throw 'Android tests did not report success; inspect artifacts/F1-device-test.txt.'
    }
} finally { Pop-Location }
