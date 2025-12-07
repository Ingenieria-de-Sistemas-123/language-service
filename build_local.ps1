# Check if .secrets directory exists
if (!(Test-Path .secrets)) {
    Write-Error "The .secrets directory does not exist. Please ensure you have .secrets/github_user and .secrets/github_token files."
    exit 1
}

# Check if secret files exist
if (!(Test-Path .secrets/github_user) -or !(Test-Path .secrets/github_token)) {
    Write-Error "Missing secret files. Please ensure .secrets/github_user and .secrets/github_token exist."
    exit 1
}

Write-Host "Building Docker image 'language-service' with local secrets..."

docker build -t language-service `
  --secret id=github_token,src=.secrets/github_token `
  --secret id=github_user,src=.secrets/github_user `
  .

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build successful!" -ForegroundColor Green
} else {
    Write-Error "Build failed."
}
