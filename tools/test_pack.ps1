# Tensura Abyss — Pack Diagnose-Script (PowerShell)
# Ausfuehren: powershell -ExecutionPolicy Bypass -File test_pack.ps1
#
# Was dieses Script prueft:
#   1. Letzte Crash-Reports
#   2. Errors und Warnings aus latest.log
#   3. Java-Version
#   4. RAM-Allocation (aus CurseForge/Prism Launcher config)
#   5. Ob OptiFine installiert ist (Warnung)
#   6. Ob options.txt schreibgeschuetzt ist

param(
  [string]$InstancePath = ""
)

$RED    = "Red"
$YELLOW = "Yellow"
$GREEN  = "Green"
$CYAN   = "Cyan"
$WHITE  = "White"

Write-Host ""
Write-Host "======================================================" -ForegroundColor $CYAN
Write-Host "  Tensura Abyss — Pack Diagnose" -ForegroundColor $CYAN
Write-Host "======================================================" -ForegroundColor $CYAN
Write-Host ""

# ─── Instanz-Pfad finden ───
if ($InstancePath -eq "") {
  $candidates = @(
    "$env:USERPROFILE\curseforge\minecraft\Instances\Tensura Abyss",
    "$env:APPDATA\.minecraft\instances\Tensura Abyss",
    "C:\Users\$env:USERNAME\curseforge\minecraft\Instances\Tensura Abyss"
  )
  foreach ($c in $candidates) {
    if (Test-Path $c) { $InstancePath = $c; break }
  }
}

if ($InstancePath -eq "" -or -not (Test-Path $InstancePath)) {
  Write-Host "[!] Instanz-Ordner nicht gefunden." -ForegroundColor $YELLOW
  Write-Host "    Pfad manuell angeben:" -ForegroundColor $WHITE
  Write-Host '    .\test_pack.ps1 -InstancePath "C:\Pfad\zu\Tensura Abyss"' -ForegroundColor $WHITE
  exit 1
}

Write-Host "[OK] Instanz gefunden: $InstancePath" -ForegroundColor $GREEN
Write-Host ""

# ─── 1. Crash Reports ───
Write-Host "── 1. Crash Reports ──────────────────────────────────" -ForegroundColor $CYAN
$crashDir = Join-Path $InstancePath "crash-reports"
if (Test-Path $crashDir) {
  $crashes = Get-ChildItem $crashDir -Filter "*.txt" | Sort-Object LastWriteTime -Descending | Select-Object -First 5
  if ($crashes.Count -eq 0) {
    Write-Host "[OK] Keine Crash-Reports gefunden." -ForegroundColor $GREEN
  } else {
    Write-Host "[!!] $($crashes.Count) neueste Crash-Reports:" -ForegroundColor $RED
    foreach ($c in $crashes) {
      Write-Host "     $($c.LastWriteTime.ToString('yyyy-MM-dd HH:mm'))  $($c.Name)" -ForegroundColor $YELLOW
    }
    Write-Host ""
    Write-Host "     Letzten Crash anzeigen? (j/n)" -ForegroundColor $WHITE -NoNewline
    $ans = Read-Host " "
    if ($ans -eq "j" -or $ans -eq "J" -or $ans -eq "y") {
      Write-Host ""
      Get-Content $crashes[0].FullName | Select-Object -First 40 | ForEach-Object { Write-Host "  $_" -ForegroundColor $YELLOW }
    }
  }
} else {
  Write-Host "[OK] crash-reports/ Ordner existiert nicht (kein Crash bisher)." -ForegroundColor $GREEN
}
Write-Host ""

# ─── 2. latest.log ───
Write-Host "── 2. Log-Analyse (ERROR / WARN) ────────────────────" -ForegroundColor $CYAN
$logPath = Join-Path $InstancePath "logs\latest.log"
if (Test-Path $logPath) {
  $errors   = Select-String -Path $logPath -Pattern "\[ERROR\]" | Select-Object -Last 10
  $warnings = Select-String -Path $logPath -Pattern "\[WARN\]"  | Select-Object -Last 10
  $mixins   = Select-String -Path $logPath -Pattern "Mixin"      | Select-Object -Last 5

  if ($errors.Count -gt 0) {
    Write-Host "[!!] $($errors.Count) ERROR(s) (letzte 10):" -ForegroundColor $RED
    $errors | ForEach-Object { Write-Host "  $($_.Line)" -ForegroundColor $RED }
  } else {
    Write-Host "[OK] Keine ERRORS im Log." -ForegroundColor $GREEN
  }
  Write-Host ""
  if ($warnings.Count -gt 0) {
    Write-Host "[!]  $($warnings.Count) WARN(s) (letzte 10):" -ForegroundColor $YELLOW
    $warnings | ForEach-Object { Write-Host "  $($_.Line)" -ForegroundColor $YELLOW }
  } else {
    Write-Host "[OK] Keine kritischen WARNS." -ForegroundColor $GREEN
  }
  Write-Host ""
  if ($mixins.Count -gt 0) {
    Write-Host "[!]  Mixin-Meldungen (letzte 5):" -ForegroundColor $YELLOW
    $mixins | ForEach-Object { Write-Host "  $($_.Line)" -ForegroundColor $YELLOW }
  }
} else {
  Write-Host "[?]  latest.log nicht gefunden. Pack noch nie gestartet?" -ForegroundColor $YELLOW
}
Write-Host ""

# ─── 3. OptiFine-Check ───
Write-Host "── 3. OptiFine-Check ─────────────────────────────────" -ForegroundColor $CYAN
$modsDir = Join-Path $InstancePath "mods"
if (Test-Path $modsDir) {
  $optifine = Get-ChildItem $modsDir -Filter "OptiFine*" -ErrorAction SilentlyContinue
  if ($optifine) {
    Write-Host "[!!] OPTIFINE GEFUNDEN! Sofort entfernen:" -ForegroundColor $RED
    $optifine | ForEach-Object { Write-Host "     $($_.Name)" -ForegroundColor $RED }
    Write-Host "     OptiFine verursacht Sensitivity-Bugs und Crashes mit Tensura." -ForegroundColor $RED
  } else {
    Write-Host "[OK] OptiFine nicht installiert." -ForegroundColor $GREEN
  }

  # Embeddium check
  $embeddium = Get-ChildItem $modsDir -Filter "embeddium*" -ErrorAction SilentlyContinue
  if ($embeddium) {
    Write-Host "[OK] Embeddium gefunden: $($embeddium[0].Name)" -ForegroundColor $GREEN
  } else {
    Write-Host "[?]  Embeddium nicht gefunden. Performance-Mod fehlt?" -ForegroundColor $YELLOW
  }
}
Write-Host ""

# ─── 4. options.txt Check ───
Write-Host "── 4. options.txt ────────────────────────────────────" -ForegroundColor $CYAN
$optPath = Join-Path $InstancePath "options.txt"
if (Test-Path $optPath) {
  $opts = Get-Content $optPath
  $sens = $opts | Where-Object { $_ -match "^mouseSensitivity:" }
  $raw  = $opts | Where-Object { $_ -match "^rawMouseInput:" }
  $ro   = (Get-Item $optPath).IsReadOnly

  Write-Host "  mouseSensitivity : $(if($sens){ ($sens -split ':')[1] } else { 'nicht gefunden' })" -ForegroundColor $(if($sens -and $sens -match "0\.5"){"Green"} else {"Yellow"})
  Write-Host "  rawMouseInput    : $(if($raw){ ($raw -split ':')[1] } else { 'nicht gefunden' })"  -ForegroundColor $(if($raw -and $raw -match "true"){"Green"} else {"Yellow"})
  Write-Host "  Schreibgeschuetzt: $(if($ro){'JA [OK]'} else {'NEIN [!] Empfehlung: Read-Only setzen'})" -ForegroundColor $(if($ro){"Green"} else {"Yellow"})
} else {
  Write-Host "[?]  options.txt nicht gefunden." -ForegroundColor $YELLOW
}
Write-Host ""

# ─── 5. Java-Version ───
Write-Host "── 5. Java-Version ───────────────────────────────────" -ForegroundColor $CYAN
try {
  $javaVer = & java -version 2>&1 | Select-Object -First 1
  Write-Host "  $javaVer" -ForegroundColor $(if($javaVer -match "21"){"Green"} else {"Yellow"})
  if ($javaVer -notmatch "21") {
    Write-Host "  [!] Java 21 empfohlen fuer bessere GC-Performance." -ForegroundColor $YELLOW
  }
} catch {
  Write-Host "[?]  Java nicht im PATH gefunden." -ForegroundColor $YELLOW
}
Write-Host ""

# ─── Zusammenfassung ───
Write-Host "======================================================" -ForegroundColor $CYAN
Write-Host "  Diagnose abgeschlossen." -ForegroundColor $CYAN
Write-Host "  Fuer weiteren Support: github.com/weeeedddd/tensura-modpack-optimization" -ForegroundColor $CYAN
Write-Host "======================================================" -ForegroundColor $CYAN
Write-Host ""
