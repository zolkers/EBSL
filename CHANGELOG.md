# Changelog

All notable changes to EBSL will be documented in this file.

This project follows [Conventional Commits](https://www.conventionalcommits.org/) for commit history and release notes.

## Unreleased

### Added

- GPL-3.0-or-later open-source licensing.
- Local quality gates for tests, coverage, source hygiene, and SonarQube integration.
- Contract-first API surface documentation.
- GitHub issue templates, pull request template, and CI build workflow.
- Cross-platform setup notes for Windows, macOS, and Linux contributors.

### Changed

- Public pathfinder, event, and Minecraft adapter surfaces now prefer contracts and factories over concrete implementation classes.
- Aggregate coverage gate is enforced at `>= 80%`.

### Security

- Added security reporting policy.
