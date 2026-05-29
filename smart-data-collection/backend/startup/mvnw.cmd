@echo off
setlocal enabledelayedexpansion

set "MAVEN_PROJECTBASEDIR=%cd%"
set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6"
set "M2_HOME=%MAVEN_HOME%"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo [Maven Wrapper] Downloading Apache Maven 3.9.6...
    if not exist "%USERPROFILE%\.m2\wrapper\dists" mkdir "%USERPROFILE%\.m2\wrapper\dists"
    set "MAVEN_ZIP=%TEMP%\apache-maven-3.9.6-bin.zip"
    if not exist "!MAVEN_ZIP!" (
        powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip' -OutFile '!MAVEN_ZIP!'"
    )
    if not exist "%MAVEN_HOME%" (
        echo [Maven Wrapper] Extracting Maven...
        powershell -Command "Expand-Archive -Path '!MAVEN_ZIP!' -DestinationPath '%USERPROFILE%\.m2\wrapper\dists' -Force"
    )
    echo [Maven Wrapper] Maven ready.
)

set "PATH=%MAVEN_HOME%\bin;%PATH%"
call "%MAVEN_HOME%\bin\mvn.cmd" %*
