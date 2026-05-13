$ErrorActionPreference = "Stop"

$repoRoot = git rev-parse --show-toplevel
if ([string]::IsNullOrWhiteSpace($repoRoot)) {
    throw "This script must be run inside a Git repository."
}

git -C $repoRoot config core.hooksPath .githooks
git -C $repoRoot config commit.template .gitmessage

Write-Host "Git hooks installed for $repoRoot"
Write-Host "Commit template configured: .gitmessage"
