# Mode development: compile otomatis + spring-boot:run (DevTools restart).
# Usage: .\dev.ps1

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
Set-Location $root

Write-Host "Compile awal..." -ForegroundColor Cyan
& .\mvnw.cmd -q compile
if ($LASTEXITCODE -ne 0) {
    throw "Compile gagal."
}

$state = [hashtable]::Synchronized(@{
        pending    = $false
        compiling  = $false
    })

$watcher = New-Object System.IO.FileSystemWatcher
$watcher.Path = Join-Path $root "src"
$watcher.IncludeSubdirectories = $true
$watcher.Filter = "*.*"
$watcher.NotifyFilter = [IO.NotifyFilters]::LastWrite -bor [IO.NotifyFilters]::FileName -bor [IO.NotifyFilters]::DirectoryName
$watcher.EnableRaisingEvents = $true

$handler = {
    $ext = [IO.Path]::GetExtension($Event.SourceEventArgs.FullPath).ToLowerInvariant()
    if ($ext -notin @(".java", ".yaml", ".yml", ".properties", ".css", ".js", ".ts", ".tsx", ".html")) {
        return
    }
    $Event.MessageData.pending = $true
}

$subscriptions = @(
    (Register-ObjectEvent $watcher Created -Action $handler -MessageData $state),
    (Register-ObjectEvent $watcher Changed -Action $handler -MessageData $state),
    (Register-ObjectEvent $watcher Renamed -Action $handler -MessageData $state),
    (Register-ObjectEvent $watcher Deleted -Action $handler -MessageData $state)
)

Write-Host "Menjalankan spring-boot:run (Ctrl+C untuk berhenti)..." -ForegroundColor Cyan
$app = Start-Process -FilePath ".\mvnw.cmd" -ArgumentList @("spring-boot:run", "-DskipTests") -WorkingDirectory $root -NoNewWindow -PassThru

try {
    while (-not $app.HasExited) {
        if ($state.pending -and -not $state.compiling) {
            $state.pending = $false
            $state.compiling = $true
            Start-Sleep -Milliseconds 600
            $state.pending = $false

            Write-Host "`nPerubahan terdeteksi — compile ulang..." -ForegroundColor Yellow
            & .\mvnw.cmd -q compile
            if ($LASTEXITCODE -eq 0) {
                Write-Host "Compile sukses. DevTools akan restart jika perlu." -ForegroundColor Green
            } else {
                Write-Host "Compile gagal. Perbaiki error lalu simpan lagi." -ForegroundColor Red
            }
            $state.compiling = $false
        }
        Start-Sleep -Milliseconds 400
    }
}
finally {
    $watcher.EnableRaisingEvents = $false
    $watcher.Dispose()
    foreach ($sub in $subscriptions) {
        Unregister-Event -SourceIdentifier $sub.Name -Force -ErrorAction SilentlyContinue
        Remove-Job -Id $sub.Id -Force -ErrorAction SilentlyContinue
    }
    if (-not $app.HasExited) {
        Stop-Process -Id $app.Id -Force -ErrorAction SilentlyContinue
    }
}