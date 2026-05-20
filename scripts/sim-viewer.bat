@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "REPO_DIR=%SCRIPT_DIR%.."

powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%sim-viewer.ps1" %*
exit /b %ERRORLEVEL%
