# Local SonarQube

EBSL expects local SonarQube at `http://localhost:9000` by default.

## One-time token setup

Create a SonarQube token from the local web UI, then run:

```powershell
.\scripts\setup-sonar-token.ps1
```

For non-interactive setup:

```powershell
.\scripts\setup-sonar-token.ps1 -Token "squ_..." -HostUrl "http://localhost:9000"
```

The script stores the token in your user Gradle file:

```text
%USERPROFILE%\.gradle\gradle.properties
```

This file is outside the repository, so the token is not committed.

## Run analysis

```powershell
.\gradlew.bat check
.\gradlew.bat sonar sonarQualityGate
```

`check` is token-free and enforces local quality gates. `sonar sonarQualityGate` uploads to SonarQube and verifies the server quality gate.
