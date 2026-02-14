# run-all-tests.ps1
Write-Host "===================================" -ForegroundColor Cyan
Write-Host "Ejecutando pruebas de SmartHR" -ForegroundColor Cyan
Write-Host "===================================" -ForegroundColor Cyan

# Configurar variables de entorno
$env:JWT_SECRET="local-test-secret-key-32-characters-minimum"

# Backend
Write-Host "`n[1/3] Ejecutando pruebas del Backend..." -ForegroundColor Yellow
cd backend
mvn clean test
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error en pruebas del Backend" -ForegroundColor Red
    exit 1
}
mvn jacoco:report
cd ..

# Assistant
Write-Host "`n[2/3] Ejecutando pruebas del Assistant..." -ForegroundColor Yellow
cd assistant
mvn clean test
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error en pruebas del Assistant" -ForegroundColor Red
    exit 1
}
mvn jacoco:report
cd ..

# Frontend
Write-Host "`n[3/3] Compilando Frontend..." -ForegroundColor Yellow
cd frontend
npm install
npm run build
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error compilando Frontend" -ForegroundColor Red
    exit 1
}
cd ..

Write-Host "`n===================================" -ForegroundColor Green
Write-Host "Todas las pruebas completadas!" -ForegroundColor Green
Write-Host "===================================" -ForegroundColor Green
Write-Host "`nReportes de cobertura:" -ForegroundColor Cyan
Write-Host "  Backend:   backend\target\site\jacoco\index.html" -ForegroundColor White
Write-Host "  Assistant: assistant\target\site\jacoco\index.html" -ForegroundColor White

# Abrir reportes
Write-Host "`nPresiona Enter para abrir los reportes de cobertura..." -ForegroundColor Yellow
Read-Host

start backend\target\site\jacoco\index.html
start assistant\target\site\jacoco\index.html