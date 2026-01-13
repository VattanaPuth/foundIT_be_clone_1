# Usage:
#   .\start.ps1
#   .\start.ps1 -Port 8086
param(
	[int]$Port = 8085
)

# Start backend with local profile for OAuth2 credentials
Write-Host "Starting backend with local profile..." -ForegroundColor Green
./mvnw spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=local --server.port=$Port"
