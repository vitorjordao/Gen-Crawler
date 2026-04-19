# Gen-Crawler Project Documentation

## Project Overview
Gen-Crawler is an extensible web crawler microservice that extracts data (primarily prices and products) from multiple sources: static e-commerce pages, single-page applications, and Telegram channels. It exposes a REST API to submit crawl jobs and poll their results asynchronously.

## Technology Stack
- **Java 21**, Spring Boot 3.3.0, Maven 3.9.9
- **Database**: H2 (in-memory) for the `dev` profile, PostgreSQL 16 for the `prod` profile
- **Scraping**: Jsoup 1.17.2 (static HTML), Playwright 1.43.0 (SPA)
- **Telegram**: telegrambots 8.2.0 (bot must be admin of target channel)
- **API docs**: Springdoc OpenAPI 2.5.0 (Swagger UI)
- **Containers**: Docker multi-stage + docker-compose with PostgreSQL + healthcheck
- **Native image** (GraalVM): configured behind the `native` profile, not run in CI

## Project Structure
```
src/main/java/br/com/gencrawler/
├── config/           # ExtractorConfig — registers DataExtractor beans
├── controller/       # CrawlerController + GlobalExceptionHandler
├── core/
│   ├── extractor/    # Jsoup, Playwright, Telegram implementations
│   ├── crawler/      # WebsiteCrawler (sourceType=site) + RobotsTxtChecker
│   ├── interfaces/   # DataExtractor SPI
│   └── normalizer/   # PriceNormalizer (locale-aware currency parsing)
├── dto/              # CrawlRequest, CrawlResponse
├── model/            # CrawlJob, ExtractedData (JPA entities, @GeneratedValue UUID)
├── repository/       # Spring Data JPA repositories
└── service/          # CrawlerService (sync) + CrawlJobProcessor (@Async)
```

## Execution flow
1. `POST /api/v1/crawler/crawl` → `CrawlerService.submitCrawlJob` saves a `CrawlJob` (status `PENDING`) and commits immediately (no outer `@Transactional`, so Spring Data JPA's save auto-commits before the async dispatch).
2. `CrawlerService` then calls `CrawlJobProcessor.process` — which is `@Async` and lives in a **separate bean** so the Spring AOP proxy is honored (no self-invocation).
3. `CrawlJobProcessor.process` picks the highest-priority `DataExtractor` that `supports()` the request's `sourceType`, runs `extract()`, persists results via `ExtractedDataRepository`, and flips the job to `COMPLETED` / `FAILED`.
4. Async pool is configured from `spring.task.execution.pool.*` in `application.yml` (5 core, 10 max, 100 queue) — Spring Boot auto-configures the executor; no custom `AsyncConfigurer` needed.

## REST API
- `POST /api/v1/crawler/crawl` → `202 Accepted` with `jobId`
- `GET /api/v1/crawler/status/{jobId}` → `200` with job state; `404` if the ID doesn't exist; `400` for malformed UUIDs
- `GET /api/v1/crawler/test` — simple health probe
- `GET /api-docs`, `GET /swagger-ui.html`, `GET /h2-console` (dev only)

All errors flow through `GlobalExceptionHandler` (uniform JSON body).

## Spring profiles
- **dev** (default) — H2 in-memory, `ddl-auto=update`, H2 console enabled, DEBUG logs.
- **prod** — PostgreSQL, `ddl-auto` configurable via `DDL_AUTO` (use `validate` after first deploy), H2 console off.
- **test** — used by the test suite (`application-test.properties`), H2 with `create-drop`.

Select via `SPRING_PROFILES_ACTIVE` (default `dev`). The Dockerfile sets `prod` by default.

## Environment variables
| Variable | Scope | Default | Purpose |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | all | `dev` | profile selector |
| `DATABASE_URL` / `DATABASE_DRIVER` / `DATABASE_USERNAME` / `DATABASE_PASSWORD` / `DATABASE_DIALECT` | prod | — | JDBC config |
| `DDL_AUTO` | prod | `update` | Hibernate schema strategy |
| `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | compose | — | PostgreSQL container |
| `TELEGRAM_BOT_TOKEN` / `TELEGRAM_BOT_USERNAME` | all | empty | required when `sourceType=telegram` |
| `SERVER_PORT` | all | `8080` | |
| `H2_CONSOLE_ENABLED` | dev | `true` | |
| `LOG_LEVEL` / `LOG_LEVEL_SPRING` / `LOG_LEVEL_HIBERNATE` | all | `INFO` / `WARN` / `WARN` | |

No credentials are hardcoded. `.env.example` is the canonical list.

## Building and running

```bash
# dev (H2)
mvn spring-boot:run

# tests
mvn test

# package JAR
mvn clean package -DskipTests

# docker-compose (prod profile, PostgreSQL)
cp .env.example .env  # edit POSTGRES_PASSWORD, TELEGRAM_BOT_TOKEN
docker-compose up -d --build

# native (experimental)
mvn clean package -Pnative -DskipTests
```

## Code conventions
- Java 21: prefer records, switch expressions, pattern matching.
- Lombok (`@Data`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`) — excluded from the Spring Boot repackaged jar.
- Entity IDs: `@GeneratedValue(strategy = GenerationType.UUID)` — let Hibernate generate; don't set the ID manually in the service layer.
- Use `Locale.forLanguageTag(...)` (the `new Locale(...)` constructor is deprecated).
- Use `RoundingMode.HALF_UP` (not `BigDecimal.ROUND_HALF_UP`).
- Controller methods stay thin — throw; let `GlobalExceptionHandler` map HTTP status.
- Async work goes through a **separate bean** to avoid self-invocation bypassing the `@Async` proxy.

## Testing patterns
- Unit: `PriceNormalizerTest`, `TelegramExtractorTest` (pure JUnit).
- Integration with WireMock: `JsoupWebExtractorTest` (HTTP stubbed on dynamic port).
- `@SpringBootTest` + H2 + `@ActiveProfiles("test")`: `CrawlerServiceTest`. Uses Awaitility for the async completion path.
- `@WebMvcTest` + `@MockBean`: `CrawlerControllerTest` (covers 202/400/404 paths).

## Adding a new extractor
1. Implement `DataExtractor` (`supports`, `extract`, `getPriority`).
2. Register it **once**: either `@Component` on the class **or** `@Bean` in `ExtractorConfig` — not both (Spring will complain about duplicate beans).
3. Add the new bean to the `dataExtractors` list factory method in `ExtractorConfig`.
4. Highest `getPriority()` wins when multiple extractors support the same `sourceType`.

## Source types
- `web` — single page via Jsoup (priority 100). `extractors` required.
- `site` — full-site BFS crawler via `WebsiteCrawler` (priority 95). Stays within seed's host, respects robots.txt, politeness delay, max pages/depth. `extractors` optional — empty list triggers default page extraction (title/h1/meta/prices).
- `spa` / `dynamic` — Playwright headless Chromium (priority 90).
- `telegram` — Bot API via `OkHttpTelegramClient` (priority 80).

## Architecture diagrams
Mermaid diagrams (flow, sequence, site crawl, packages, ER) live in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Known limitations
- **Telegram Bot API can't read arbitrary channel history.** The bot must be an admin of the target channel/group and messages arrive via `getUpdates`. For historical or public-only channel scraping, consider MTProto (e.g., TDLight) — not included.
- **Playwright needs browser binaries.** In Docker, these are downloaded on first run; allow a few extra seconds for first requests.
- **Site crawl is single-threaded per job.** One job crawls sequentially with politeness delay. Concurrent jobs run in parallel via the async pool. The `RobotsTxtChecker` is per-job (not shared across jobs).
