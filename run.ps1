$env:JAVA_HOME = (Get-Command java | Split-Path -Parent | Split-Path -Parent)
Set-Location "$PSScriptRoot\desktop\moodwatch-desktop"
./mvnw javafx:run
