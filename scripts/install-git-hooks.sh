#!/usr/bin/env sh
set -eu

repo_root="$(git rev-parse --show-toplevel)"
if [ -z "$repo_root" ]; then
    echo "This script must be run inside a Git repository." >&2
    exit 1
fi

git -C "$repo_root" config core.hooksPath .githooks
git -C "$repo_root" config commit.template .gitmessage

echo "Git hooks installed for $repo_root"
echo "Commit template configured: .gitmessage"
