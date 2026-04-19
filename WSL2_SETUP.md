# WSL2 Setup Guide

Development environment setup for Gen-Crawler on Windows using WSL2.

## Prerequisites
- WSL2 with an Ubuntu/Debian distribution
- Windows Docker Desktop (if you plan to run `docker-compose`)

## 1. Install Java 21 and Maven

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk maven
java -version     # should show 21.x
mvn --version
```

## 2. Clone / enter the project

```bash
cd /mnt/c/Users/v/Documents/Projects/Gen-Crawler
```

## 3. Choose a profile

The default profile is `dev` (H2 in-memory) — no extra setup needed.

```bash
mvn spring-boot:run
```

For `prod` (PostgreSQL) locally without Docker, install Postgres and set env vars:

```bash
sudo apt install -y postgresql
sudo -u postgres psql -c "CREATE DATABASE crawlerdb;"
sudo -u postgres psql -c "CREATE USER crawler WITH PASSWORD 'change-me';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE crawlerdb TO crawler;"

export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:postgresql://localhost:5432/crawlerdb
export DATABASE_USERNAME=crawler
export DATABASE_PASSWORD=change-me
mvn spring-boot:run
```

Or simpler — just use docker-compose (next section).

## 4. docker-compose (recommended for prod-like setup)

```bash
# Install Docker inside WSL2 if you're not using Docker Desktop
sudo apt install -y docker.io docker-compose-v2
sudo usermod -aG docker $USER      # log out/in or exec a new shell
```

Then from the project root:

```bash
cp .env.example .env
# edit .env: set POSTGRES_PASSWORD, optionally TELEGRAM_BOT_TOKEN
docker-compose up -d --build
```

This starts PostgreSQL 16 + the crawler app on the `prod` profile. The app waits for the Postgres healthcheck before starting.

## 5. Test the running app

```bash
curl http://localhost:8080/api/v1/crawler/test
curl -X POST http://localhost:8080/api/v1/crawler/crawl \
  -H "Content-Type: application/json" -d @request.json
```

Browser:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs
- H2 Console (dev profile only): http://localhost:8080/h2-console

## 6. Running tests

```bash
mvn test
```

Tests use an H2 in-memory DB via `src/test/resources/application-test.properties`.

## Optional: GraalVM native image

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.1-graal

mvn clean package -Pnative -DskipTests
./target/gen-crawler
```

## Troubleshooting

- **`POSTGRES_PASSWORD is required` from docker-compose** — you didn't set it in `.env` (compose fails fast by design).
- **Port 8080 already in use** — override with `SERVER_PORT=8081 mvn spring-boot:run`.
- **Playwright: missing browser binaries** — first run downloads Chromium automatically; subsequent runs use the cache. Inside Docker this adds a few seconds to startup.
- **Docker daemon permission denied** — you need `sudo usermod -aG docker $USER` and a fresh shell, or use Docker Desktop's WSL integration.
