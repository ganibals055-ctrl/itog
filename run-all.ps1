Write-Host "====== STARTING ALL INFRASTRUCTURE ======" -ForegroundColor Cyan

# 1. Stop old containers
Write-Host "`n[1/5] Cleaning old environment..." -ForegroundColor Yellow
docker-compose -f infrastructure/docker-compose.yml down 2>$null

# 2. Start EVERYTHING
Write-Host "`n[2/5] Starting Jenkins, Nexus, PostgreSQL..." -ForegroundColor Yellow
docker-compose -f infrastructure/docker-compose.yml up -d

Write-Host "Waiting 60 seconds for startup..." -ForegroundColor Gray
Start-Sleep -Seconds 60

# 3. Nexus setup instruction
Write-Host "`n[3/5] Configuring Nexus..." -ForegroundColor Yellow
Write-Host "Open in browser: http://localhost:19091" -ForegroundColor White
Write-Host "Login: admin" -ForegroundColor White
Write-Host "Password: admin123" -ForegroundColor White
Write-Host ""
Write-Host "Create Docker repository:" -ForegroundColor White
Write-Host "1. Left menu: Repository -> Repositories" -ForegroundColor Gray
Write-Host "2. Create repository -> docker (hosted)" -ForegroundColor Gray
Write-Host "3. Name: docker-hosted" -ForegroundColor Gray
Write-Host "4. HTTP port: 19092" -ForegroundColor Gray
Write-Host "5. Create repository" -ForegroundColor Gray

# 4. Docker setup
Write-Host "`n[4/5] Configuring Docker..." -ForegroundColor Yellow
Write-Host "In Docker Desktop:" -ForegroundColor White
Write-Host "   Settings -> Docker Engine -> add this line:" -ForegroundColor Gray
Write-Host '   "insecure-registries": ["localhost:19092"]' -ForegroundColor Gray
Write-Host "   Save and restart Docker" -ForegroundColor White

Write-Host "`nOr run command:" -ForegroundColor White
Write-Host "   docker login localhost:19092 -u admin -p admin123" -ForegroundColor Gray

# 5. Final information
Write-Host "`n[5/5] EVERYTHING READY! Show to teacher:" -ForegroundColor Green

Write-Host "`n=== WHAT TO SHOW ===" -ForegroundColor Magenta

Write-Host "`n1. WORKING APPLICATION:" -ForegroundColor Cyan
Write-Host "   Java service (images): http://localhost:8080" -ForegroundColor White
Write-Host "   .NET service (ratings): http://localhost:8081" -ForegroundColor White
Write-Host "   Database: localhost:5432" -ForegroundColor White

Write-Host "`n2. JENKINS PIPELINES:" -ForegroundColor Cyan
Write-Host "   Jenkins: http://localhost:19090" -ForegroundColor White
Write-Host "   Login: admin / Password: admin" -ForegroundColor White
Write-Host "   Create 2 pipelines:" -ForegroundColor White
Write-Host "     - java-pipeline -> apps/java-dog-random/Jenkinsfile.java" -ForegroundColor Gray
Write-Host "     - dotnet-pipeline -> apps/dotnet-dog-rating/Jenkinsfile.dotnet" -ForegroundColor Gray

Write-Host "`n3. NEXUS WITH IMAGES:" -ForegroundColor Cyan
Write-Host "   Nexus: http://localhost:19091" -ForegroundColor White
Write-Host "   Login: admin / Password: admin123" -ForegroundColor White
Write-Host "   Show repository 'docker-hosted'" -ForegroundColor White

Write-Host "`n=== CHECK COMMANDS ===" -ForegroundColor Magenta
Write-Host "docker ps" -ForegroundColor Gray
Write-Host "curl http://localhost:8080" -ForegroundColor Gray

Write-Host "`n✅ ALL STARTED! Call your teacher." -ForegroundColor Green