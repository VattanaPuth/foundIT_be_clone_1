# Script to clean OAuth secrets from git history

Write-Host "Starting git history cleanup..." -ForegroundColor Green

# Set environment variable to suppress warnings
$env:FILTER_BRANCH_SQUELCH_WARNING = '1'

# Run git filter-branch to rewrite history
git filter-branch --force --tree-filter @'
if (Test-Path "src/main/resources/application.properties") {
    $content = Get-Content "src/main/resources/application.properties" -Raw
    $content = $content -replace "spring\.security\.oauth2\.client\.registration\.google\.clientId\s*=\s*[^\r\n]*", "spring.security.oauth2.client.registration.google.clientId = `${GOOGLE_CLIENT_ID}"
    $content = $content -replace "spring\.security\.oauth2\.client\.registration\.google\.clientSecret\s*=\s*[^\r\n]*", "spring.security.oauth2.client.registration.google.clientSecret = `${GOOGLE_CLIENT_SECRET}"
    Set-Content "src/main/resources/application.properties" -Value $content -NoNewline
}
'@ --prune-empty --tag-name-filter cat -- --all

Write-Host "`nHistory rewrite complete!" -ForegroundColor Green
Write-Host "`nNext steps:" -ForegroundColor Yellow
Write-Host "1. Run: git push --force --all" -ForegroundColor Cyan
Write-Host "2. Run: git push --force --tags" -ForegroundColor Cyan
