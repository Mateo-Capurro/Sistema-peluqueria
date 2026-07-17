# start-dev.ps1 - arranca backend (8080) + frontend (4200) limpio.
# Mata cualquier proceso huerfano en esos puertos antes (ej: ng serve de otro proyecto).
# Uso:  desde ScaffoldingBE-FE/ ->  ./start-dev.ps1

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

function Free-Port($port) {
    $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    foreach ($c in $conns) {
        $procId = $c.OwningProcess
        $p = Get-Process -Id $procId -ErrorAction SilentlyContinue
        if ($p) {
            Write-Host "Puerto $port ocupado por $($p.ProcessName) (PID $procId) -> matando" -ForegroundColor Yellow
            Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
        }
    }
}

Write-Host "Liberando puertos 8080 y 4200..." -ForegroundColor Cyan
Free-Port 8080
Free-Port 4200
Start-Sleep -Milliseconds 800

# Backend en ventana propia
Write-Host "Arrancando backend (Spring Boot, 8080)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit","-Command","cd '$root\BE\parcial'; ./mvnw.cmd spring-boot:run"

# Esperar a que 8080 escuche antes de levantar el front
Write-Host "Esperando backend en 8080..." -ForegroundColor Cyan
$up = $false
for ($i = 0; $i -lt 40; $i++) {
    if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) { $up = $true; break }
    Start-Sleep -Seconds 2
}
if ($up) { Write-Host "Backend UP." -ForegroundColor Green }
else { Write-Host "Backend no respondio a tiempo; sigo con el front igual." -ForegroundColor Yellow }

# Frontend en ventana propia
Write-Host "Arrancando frontend (ng serve, 4200)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit","-Command","cd '$root\FE'; npm start"

Write-Host "Listo. Backend: http://localhost:8080  Frontend: http://localhost:4200" -ForegroundColor Green
