# Gen-Crawler

Extensible web crawler microservice that extracts prices and products from multiple sources (static HTML, SPA, Telegram channels) and exposes a REST API for submitting crawl jobs and retrieving results.

## Features

- **Multi-source extraction** — Jsoup (static HTML), Playwright (SPA/JS), Telegram Bot API
- **Full-site crawling** — BFS from a seed URL, stays within the host, respects robots.txt, enforces depth/page/politeness limits (`sourceType=site`)
- **Plugin architecture** — add new sources by implementing `DataExtractor`
- **Async processing** — jobs are dispatched to a thread pool; status polled via REST
- **Price normalization** — locale-aware parsing across 15+ currencies
- **Profile-based config** — H2 in-memory for dev, PostgreSQL for prod
- **Observability-ready** — Spring Boot actuator hooks, structured logs, Swagger UI

Architecture diagrams: see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Tech Stack

- Java 21, Spring Boot 3.3.0, Spring Data JPA
- Jsoup 1.17, Playwright 1.43, telegrambots 8.2
- H2 / PostgreSQL 16, Hibernate 6
- Springdoc OpenAPI 2.5
- Docker multi-stage build, docker-compose

## Quick Start

### Local dev (H2 in-memory)

```bash
mvn spring-boot:run
# defaults to SPRING_PROFILES_ACTIVE=dev (H2, ddl-auto=update, H2 console enabled)
```

### Production with docker-compose (PostgreSQL)

```bash
cp .env.example .env
# edit .env: set POSTGRES_PASSWORD, optionally TELEGRAM_BOT_TOKEN
docker-compose up -d --build
```

Compose starts:
- `crawler` — the Spring Boot app (profile `prod`)
- `postgres` — PostgreSQL 16 with healthcheck; app waits until it's ready

### Standalone JAR

```bash
mvn clean package -DskipTests
SPRING_PROFILES_ACTIVE=prod \
DATABASE_URL=jdbc:postgresql://localhost:5432/crawlerdb \
DATABASE_USERNAME=crawler DATABASE_PASSWORD=... \
java -jar target/gen-crawler-*.jar
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/crawler/crawl` | Submit a crawl job; returns `202 Accepted` with `jobId` |
| GET | `/api/v1/crawler/status/{jobId}` | Poll job status; returns `PENDING` / `PROCESSING` / `COMPLETED` / `FAILED` |
| GET | `/api/v1/crawler/test` | Health-check |
| GET | `/swagger-ui.html` | Interactive API docs |
| GET | `/api-docs` | OpenAPI JSON |
| GET | `/h2-console` | H2 console (dev profile only) |

### Error responses

All errors return a uniform JSON body via `GlobalExceptionHandler`:

| HTTP | Triggered by |
|------|--------------|
| 400 | Malformed UUID, validation failure, invalid currency |
| 404 | `jobId` not found |
| 500 | Unhandled exceptions (with log) |

### Example request

```bash
curl -X POST http://localhost:8080/api/v1/crawler/crawl \
  -H "Content-Type: application/json" \
  -d '{
    "sourceType": "web",
    "sourceUrl": "https://example.com/product",
    "extractors": [
      { "fieldName": "title", "selector": "h1" },
      { "fieldName": "price", "selector": ".price", "currency": "BRL" }
    ]
  }'
```

Response:
```json
{ "jobId": "8f2...", "status": "ACCEPTED", "createdAt": "2026-04-19T12:00:00" }
```

Then:
```bash
curl http://localhost:8080/api/v1/crawler/status/8f2...
```

## Configuration

Environment variables (see [.env.example](.env.example) for the full list):

| Variable | Profile | Default | Purpose |
|----------|---------|---------|---------|
| `SPRING_PROFILES_ACTIVE` | — | `dev` | `dev` (H2) or `prod` (PostgreSQL) |
| `DATABASE_URL` | prod | required | JDBC URL |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | prod | required | DB credentials |
| `DDL_AUTO` | prod | `update` | Hibernate schema strategy; use `validate` after first deploy |
| `TELEGRAM_BOT_TOKEN` | — | empty | Required when `sourceType=telegram` |
| `SERVER_PORT` | — | `8080` | HTTP port |
| `H2_CONSOLE_ENABLED` | dev | `true` | Exposes H2 web console |
| `LOG_LEVEL` / `LOG_LEVEL_SPRING` / `LOG_LEVEL_HIBERNATE` | — | `INFO` / `WARN` / `WARN` | Logger levels |

## Source types

- `api` — **JSON API client**: `HttpClient` + JSONPath selectors, GET/POST/PUT/DELETE, headers for auth (priority 110). See [recipes for Mercado Livre, Shopee, OLX](docs/RECIPES.md).
- `web` — static HTML via Jsoup, one URL, one result (priority 100)
- `site` — **full-site crawler** from a seed URL: BFS, stays within host, respects robots.txt, extracts per-page defaults (title, h1, meta description, detected prices) or your configured fields (priority 95)
- `spa` / `dynamic` — Playwright headless Chromium (priority 90)
- `telegram` — Telegram Bot API. **The bot must be an admin of the target channel** — the Bot API can't read channel history otherwise. Messages are fetched via `getUpdates` (last ~100 queued updates).

### API extractor (`sourceType=api`)

| Field | Description |
|---|---|
| `method` | `GET` (default), `POST`, `PUT`, `DELETE` |
| `body` | Raw body (typically JSON) for POST/PUT |
| `headers` | Auth/UA/content-type |
| `extractors[].selector` | **JSONPath** expression (e.g. `$.results[*].title`) |
| `extractors[].currency` | Optional — passes matched values through `PriceNormalizer` |
| `validateCertificates` | `false` to skip TLS validation |

Example (generic JSON API):
```json
{
  "sourceType": "api",
  "sourceUrl": "https://api.example.com/items?q=sword",
  "headers": {"Authorization": "Bearer TOKEN"},
  "extractors": [
    {"fieldName": "names", "selector": "$.results[*].title"},
    {"fieldName": "prices", "selector": "$.results[*].price", "currency": "BRL"}
  ]
}
```

Empty `extractors: []` returns the whole payload under `data.payload` — useful for probing an unknown API.

### Site crawl parameters (only for `sourceType=site`)

| Field | Default | Description |
|---|---|---|
| `maxPages` | 50 | Hard limit on pages visited |
| `maxDepth` | 3 | Max link depth from the seed |
| `sameHost` | `true` | Don't leave the seed's host |
| `respectRobotsTxt` | `true` | Honor `Disallow` rules for user-agent `*` |
| `delayMs` | 500 | Politeness delay between fetches (ms) |
| `pathPattern` | — | Optional regex — only URLs whose path matches are enqueued |
| `extractors` | `[]` | Empty list → extract defaults (title/h1/meta/prices). Non-empty → run your CSS selectors on each page |

Example:

```bash
curl -X POST http://localhost:8080/api/v1/crawler/crawl \
  -H "Content-Type: application/json" \
  -d '{
    "sourceType": "site",
    "sourceUrl": "https://example.com",
    "maxPages": 20,
    "maxDepth": 2,
    "pathPattern": "^/(blog|docs)/",
    "extractors": []
  }'
```

The status response's `data` will contain `seed`, `host`, `visitedCount`, and an array of `pages` (each with `url`, `depth`, `data`).

## Extractor configuration

Each entry in `extractors[]`:

| Field | Type | Description |
|-------|------|-------------|
| `fieldName` | string (required) | Output key |
| `selector` | string (required) | CSS selector (web/spa) or field path (telegram: `text`, `caption`, `date`, `from.username`, `chat.title`, …) |
| `attribute` | string | For web/spa: `text` (default), `href`, `src`, etc. |
| `regex` | string | Applied to raw value to narrow down |
| `currency` | enum | BRL, USD, EUR, GBP, JPY, AUD, CAD, CHF, CNY, ARS, CLP, COP, MXN, PEN, UYU → triggers [`PriceNormalizer`](src/main/java/br/com/gencrawler/core/normalizer/PriceNormalizer.java) |
| `required` | boolean | Throw if selector matches nothing |

## Project structure

```
src/main/java/br/com/gencrawler/
├── config/           # Spring configuration (extractor registry)
├── controller/       # REST controllers + GlobalExceptionHandler
├── core/
│   ├── interfaces/   # DataExtractor SPI
│   ├── extractor/    # Jsoup / Playwright / Telegram / Api extractors
│   ├── crawler/      # WebsiteCrawler + RobotsTxtChecker (sourceType=site)
│   └── normalizer/   # PriceNormalizer
├── dto/              # CrawlRequest, CrawlResponse
├── model/            # JPA entities (CrawlJob, ExtractedData)
├── repository/       # Spring Data JPA repositories
└── service/          # CrawlerService, CrawlJobProcessor (async)
```

## Adding a new extractor

1. Implement [`DataExtractor`](src/main/java/br/com/gencrawler/core/interfaces/DataExtractor.java) (`supports`, `extract`, `getPriority`).
2. Annotate with `@Component` **or** register a `@Bean` in [`ExtractorConfig`](src/main/java/br/com/gencrawler/config/ExtractorConfig.java) — not both.
3. Add the new bean to the `dataExtractors` list in `ExtractorConfig`.
4. Highest `getPriority()` wins when multiple extractors `supports()` the same source type.

## Testing

```bash
mvn test
```

Test suite covers: `PriceNormalizer`, `CrawlerService` (end-to-end with H2), `CrawlerController` (MockMvc), `JsoupWebExtractor` (WireMock), `TelegramExtractor` (unit).

## Native image (GraalVM — experimental)

```bash
mvn clean package -Pnative -DskipTests
./target/gen-crawler
```

## License

MIT — see [LICENSE](LICENSE)
