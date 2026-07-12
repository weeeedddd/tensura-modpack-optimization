@echo off
REM Setup script für Tensura Modpack Optimization Repo (Windows)

echo.
echo ====================================================================
echo Tensura Modpack Optimization - GitHub Push Setup
echo ====================================================================
echo.

REM Git initialisieren
echo Initialisiere Git Repository...
git init

REM Git konfigurieren
echo Konfiguriere Git...
git config user.email noreply@anthropic.com
git config user.name Claude

REM Alle Dateien hinzufügen
echo Fuege Dateien hinzu...
git add .

REM Commit erstellen
echo Erstelle Initial Commit...
git commit -m "Initial commit: Tensura Abyss modpack optimization guide"

REM Remote hinzufügen
echo Fuege GitHub Remote hinzu...
git remote add origin https://github.com/weeeedddd/tensura-modpack-optimization.git

REM Branch zu main umbenennen
echo Benenne Branch um...
git branch -M main

REM Pushen
echo.
echo Pushe zu GitHub...
git push -u origin main

echo.
echo ====================================================================
echo OK! Repo ist unter https://github.com/weeeedddd/tensura-modpack-optimization
echo ====================================================================
pause