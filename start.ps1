# Start backend with local profile for OAuth2 credentials
Write-Host "Starting backend with local profile..." -ForegroundColor Green
./mvnw spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=local"
