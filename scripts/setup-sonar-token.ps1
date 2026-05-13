param(
    [string] $Token,
    [string] $HostUrl = "http://localhost:9000"
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($Token)) {
    $secret = Read-Host "Sonar token" -AsSecureString
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secret)
    try {
        $Token = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
    }
}

if ([string]::IsNullOrWhiteSpace($Token)) {
    throw "A Sonar token is required."
}

$gradleDir = Join-Path $HOME ".gradle"
$propertiesPath = Join-Path $gradleDir "gradle.properties"
New-Item -ItemType Directory -Path $gradleDir -Force | Out-Null

$lines = [System.Collections.Generic.List[string]]::new()
if (Test-Path $propertiesPath) {
    Get-Content -Path $propertiesPath | ForEach-Object {
        $lines.Add($_)
    }
}

function Set-GradleProperty {
    param(
        [System.Collections.Generic.List[string]] $Lines,
        [string] $Name,
        [string] $Value
    )

    $updated = $false
    for ($i = 0; $i -lt $Lines.Count; $i++) {
        if ($Lines[$i] -match "^\s*$([Regex]::Escape($Name))\s*=") {
            $Lines[$i] = "$Name=$Value"
            $updated = $true
            break
        }
    }

    if (-not $updated) {
        if ($Lines.Count -gt 0 -and -not [string]::IsNullOrWhiteSpace($Lines[$Lines.Count - 1])) {
            $Lines.Add("")
        }
        $Lines.Add("$Name=$Value")
    }
}

Set-GradleProperty -Lines $lines -Name "sonar.token" -Value $Token
Set-GradleProperty -Lines $lines -Name "sonar.host.url" -Value $HostUrl
Set-Content -Path $propertiesPath -Value $lines -Encoding UTF8

Write-Host "Sonar token configured in $propertiesPath"
Write-Host "Run: ./gradlew.bat sonar sonarQualityGate"
