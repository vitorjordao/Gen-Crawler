# Gen-Crawler — Architecture

## High-level flow

```mermaid
flowchart LR
    Client[HTTP client]
    subgraph App[Spring Boot application]
        Controller[CrawlerController<br/>REST API]
        Handler[GlobalExceptionHandler]
        Service[CrawlerService<br/>submit / status]
        Processor[CrawlJobProcessor<br/>Async worker]
        Registry[DataExtractor registry]
        subgraph Extractors[DataExtractor SPI]
            Api[ApiExtractor<br/>sourceType=api]
            Jsoup[JsoupWebExtractor<br/>sourceType=web]
            Play[PlaywrightWebExtractor<br/>sourceType=spa]
            Site[WebsiteCrawler<br/>sourceType=site]
            TG[TelegramExtractor<br/>sourceType=telegram]
        end
        Normalizer[PriceNormalizer]
        Pool[ThreadPool<br/>spring.task.execution]
    end
    DB[(PostgreSQL or H2<br/>CrawlJob, ExtractedData)]
    Target[Target websites<br/>and Telegram API]

    Client -- "POST /crawl" --> Controller
    Client -- "GET /status/{id}" --> Controller
    Controller --> Handler
    Controller --> Service
    Service -- "save PENDING" --> DB
    Service -- "dispatch" --> Processor
    Processor -. "runs on" .- Pool
    Processor --> Registry
    Registry --> Api
    Registry --> Jsoup
    Registry --> Play
    Registry --> Site
    Registry --> TG
    Jsoup & Play & Site & Api --> Normalizer
    Api & Jsoup & Play & Site & TG --> Target
    Processor -- "update COMPLETED / FAILED" --> DB
    Service -- "findById" --> DB
```

## Request lifecycle

```mermaid
sequenceDiagram
    participant C as Client
    participant Ctrl as CrawlerController
    participant S as CrawlerService
    participant DB as CrawlJobRepository
    participant P as CrawlJobProcessor (@Async)
    participant E as DataExtractor
    participant Site as Target site

    C->>Ctrl: POST /api/v1/crawler/crawl
    Ctrl->>S: submitCrawlJob(request)
    S->>DB: save(CrawlJob PENDING)
    DB-->>S: CrawlJob with generated UUID
    S->>P: process(jobId, request)  [async, returns void immediately]
    S-->>Ctrl: CrawlResponse ACCEPTED
    Ctrl-->>C: 202 Accepted + jobId

    rect rgba(135,206,235,0.15)
    note over P: Runs on spring.task.execution pool
    P->>DB: find PENDING job, set PROCESSING
    P->>E: pick by sourceType + priority
    E->>Site: HTTP GET / Bot API
    Site-->>E: HTML / JSON
    E-->>P: Map<String, Object>
    P->>DB: save ExtractedData + update COMPLETED
    end

    C->>Ctrl: GET /status/{jobId}
    Ctrl->>S: getCrawlJobStatus
    S->>DB: findById
    DB-->>S: CrawlJob COMPLETED with resultData
    S-->>Ctrl: CrawlResponse
    Ctrl-->>C: 200 + status + data
```

## Site crawl (BFS with host fence)

```mermaid
flowchart TD
    Start[Seed URL] --> Init[Init queue and visited set]
    Init --> Loop{Queue empty or<br/>visited maxPages reached}
    Loop -- no --> Pop[Dequeue URL + depth]
    Pop --> Seen{Already visited}
    Seen -- yes --> Loop
    Seen -- no --> Robots{robots.txt allows}
    Robots -- no --> Loop
    Robots -- yes --> Fetch[Jsoup GET with user-agent]
    Fetch -- error --> Loop
    Fetch -- ok --> Extract[Extract configured fields<br/>or defaults: title, h1, meta, prices]
    Extract --> Append[Append to pages result]
    Append --> Depth{depth less than maxDepth}
    Depth -- no --> Delay
    Depth -- yes --> Links[Enumerate a href links]
    Links --> Filter[Filter: same host, path pattern,<br/>http or https, not yet visited]
    Filter --> Enqueue[Enqueue at depth plus 1]
    Enqueue --> Delay[Sleep delayMs politeness]
    Delay --> Loop
    Loop -- yes --> End[Return seed, host,<br/>visitedCount, pages list]
```

## Package map

```mermaid
flowchart TB
    subgraph controller[controller]
        CrC[CrawlerController]
        CrH[GlobalExceptionHandler]
    end
    subgraph service[service]
        CrS[CrawlerService]
        CrJP[CrawlJobProcessor]
        CrJNF[JobNotFoundException]
    end
    subgraph core[core]
        direction TB
        subgraph ifaces[core.interfaces]
            DE[DataExtractor]
        end
        subgraph extractor[core.extractor]
            Jsoup2[web.JsoupWebExtractor]
            Play2[web.PlaywrightWebExtractor]
            TG2[telegram.TelegramExtractor]
        end
        subgraph crawler[core.crawler]
            WC[WebsiteCrawler]
            RT[RobotsTxtChecker]
        end
        subgraph normalizer[core.normalizer]
            PN[PriceNormalizer]
        end
    end
    subgraph repo[repository]
        CJR[CrawlJobRepository]
        EDR[ExtractedDataRepository]
    end
    subgraph model[model]
        CJ[CrawlJob]
        ED[ExtractedData]
    end
    subgraph dto[dto]
        CrReq[CrawlRequest]
        CrRes[CrawlResponse]
    end
    subgraph cfg[config]
        EC[ExtractorConfig]
    end

    CrC --> CrS
    CrC --> CrH
    CrS --> CrJP
    CrS --> CJR
    CrJP --> DE
    CrJP --> CJR
    CrJP --> EDR
    Jsoup2 -. implements .-> DE
    Play2 -. implements .-> DE
    TG2 -. implements .-> DE
    WC -. implements .-> DE
    WC --> RT
    WC --> PN
    Jsoup2 --> PN
    Play2 --> PN
    EC --> Jsoup2
    EC --> Play2
    EC --> WC
    CJR --> CJ
    EDR --> ED
```

## Data model

```mermaid
erDiagram
    CRAWL_JOBS ||--o{ EXTRACTED_DATA : "has"
    CRAWL_JOBS {
        uuid id PK
        string sourceType
        string sourceUrl
        enum status "PENDING / PROCESSING / COMPLETED / FAILED"
        text requestConfig "serialized CrawlRequest"
        text resultData "serialized extracted map"
        string errorMessage
        timestamp createdAt
        timestamp completedAt
        int retryCount
    }
    EXTRACTED_DATA {
        uuid id PK
        uuid job_id FK
        string fieldName
        string stringValue
        decimal decimalValue
        string currency
        timestamp extractedAt
        timestamp createdAt
    }
```

## Extension points

- **`DataExtractor`** — add a new source by implementing `supports`, `extract`, `getPriority`, then registering the bean. The highest `getPriority()` among those that `supports(sourceType)` wins.
- **`PriceNormalizer.CURRENCY_LOCALES`** — add a new ISO currency by extending the map.
- **`RobotsTxtChecker`** — replace with a more complete parser (e.g., support `Allow`, `Crawl-delay`, sitemaps) if needed.

## Operational notes

- The async executor is auto-configured from `spring.task.execution.pool.*` in `application.yml` (5 core, 10 max, 100 queue).
- Transactions: `CrawlJobProcessor.process` is `@Transactional` + `@Async`; `CrawlerService.submitCrawlJob` stays non-transactional so the `save()` auto-commits before the async dispatch (avoids the classic "async sees an uncommitted row" race).
- UUID generation: `@GeneratedValue(strategy = GenerationType.UUID)` on both entities. The service reads the generated id from the saved entity — never sets it manually.
- Errors flow through `GlobalExceptionHandler`: `JobNotFoundException` → 404, `IllegalArgumentException` / validation → 400, anything else → 500 (logged).
