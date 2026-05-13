# Local SonarQube

EBSL expects local SonarQube at `http://localhost:9000` by default.

## One-time token setup

Create a SonarQube token from the local web UI, then run:

On Windows:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\setup-sonar-token.ps1
```

On macOS/Linux:

```bash
sh ./scripts/setup-sonar-token.sh
```

For non-interactive setup:

On Windows:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\setup-sonar-token.ps1 -Token "squ_..." -HostUrl "http://localhost:9000"
```

On macOS/Linux:

```bash
sh ./scripts/setup-sonar-token.sh --token "squ_..." --host-url "http://localhost:9000"
```

The script stores the token in your user Gradle file:

```text
~/.gradle/gradle.properties
```

This file is outside the repository, so the token is not committed.

## Run analysis

On Windows:

```powershell
.\gradlew.bat check
.\gradlew.bat sonar sonarQualityGate
```

On macOS/Linux:

```bash
./gradlew check
./gradlew sonar sonarQualityGate
```

`check` is token-free and enforces local quality gates. `sonar sonarQualityGate` uploads to SonarQube and verifies the server quality gate.
