# printscript-service

## Building the Docker image

The application pulls several private dependencies from GitHub Packages, so Docker
needs your GitHub credentials at build time. BuildKit secrets keep those tokens
out of the final image layers.

1. Generate a **fine-grained access token** (or classic token) in GitHub with the
   `read:packages` scope and copy your GitHub username.
2. Export the credentials to your shell session.

   **PowerShell**
   ```powershell
   $env:GITHUB_USERNAME = "your-username"
   $env:GITHUB_TOKEN    = "ghp_yourGeneratedToken"
   ```

   **bash/zsh**
   ```bash
   export GITHUB_USERNAME="your-username"
   export GITHUB_TOKEN="ghp_yourGeneratedToken"
   ```

3. Run Docker build (or Compose) passing those env vars as BuildKit secrets:
   ```bash
   docker build -t language-service \
     --secret id=github_token,env=GITHUB_TOKEN \
     --secret id=github_user,env=GITHUB_USERNAME \
     .
   ```

If you prefer `docker compose build`, supply the same `--secret` flags:
```bash
docker compose build language-service \
  --secret id=github_token,env=GITHUB_TOKEN \
  --secret id=github_user,env=GITHUB_USERNAME
```
