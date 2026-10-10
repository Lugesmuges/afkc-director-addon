param([string[]]$Tasks = @('build'))
$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot
try {
    $bundledJdk=Join-Path $projectRoot '.local/java/jdk-25.0.4.1+1'
    if(Test-Path $bundledJdk){$env:JAVA_HOME=$bundledJdk}
    $env:GRADLE_USER_HOME=Join-Path $projectRoot '.local/gradle-home'
    $bundledGradle=Join-Path $projectRoot '.local/gradle-9.6.0/bin/gradle.bat'
    if(Test-Path $bundledGradle){& $bundledGradle --no-daemon @Tasks}
    else {& .\gradlew.bat --no-daemon @Tasks}
    exit $LASTEXITCODE
} finally {Pop-Location}
