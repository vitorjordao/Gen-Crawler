# Gen-Crawler — Recipes de fornecedor

Como usar o `ApiExtractor` (`sourceType=api`) contra APIs reais. Cada seção descreve o que funciona hoje e o que as defesas do fornecedor exigem.

> Todos os exemplos são `curl -X POST` contra `http://localhost:8080/api/v1/crawler/crawl`. Submeta o body e pegue o `jobId`; depois `GET /status/{jobId}`.

---

## Mercado Livre

**Status em 2026:** A API pública `/sites/MLB/search` (antes aberta) agora responde `403 UNAUTHORIZED`. Mercado Livre fechou os endpoints de busca — é preciso um **access token OAuth** ([docs oficiais](https://developers.mercadolivre.com.br/pt_br/autenticacao-e-autorizacao)).

### 1. Registre uma app em https://developers.mercadolivre.com.br → "Minhas aplicações"
   - Anote `APP_ID` e `CLIENT_SECRET`.

### 2. Gere um token (client-credentials, para chamadas server-to-server):
```bash
TOKEN=$(curl -s -X POST https://api.mercadolibre.com/oauth/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=APP_ID&client_secret=CLIENT_SECRET" \
  | python3 -c 'import sys,json;print(json.load(sys.stdin)["access_token"])')
```

### 3. Busque produtos:
```json
{
  "sourceType": "api",
  "sourceUrl": "https://api.mercadolibre.com/sites/MLB/search?q=playstation%205&limit=5",
  "headers": { "Authorization": "Bearer $TOKEN" },
  "extractors": [
    { "fieldName": "titles", "selector": "$.results[*].title" },
    { "fieldName": "prices", "selector": "$.results[*].price", "currency": "BRL" },
    { "fieldName": "urls", "selector": "$.results[*].permalink" },
    { "fieldName": "conditions", "selector": "$.results[*].condition" },
    { "fieldName": "total", "selector": "$.paging.total" }
  ]
}
```

### 4. Detalhe de um item (uma chamada por ID):
```json
{
  "sourceType": "api",
  "sourceUrl": "https://api.mercadolibre.com/items/MLB3456789012",
  "headers": { "Authorization": "Bearer $TOKEN" },
  "extractors": [
    { "fieldName": "title", "selector": "$.title" },
    { "fieldName": "price", "selector": "$.price", "currency": "BRL" },
    { "fieldName": "available", "selector": "$.available_quantity" },
    { "fieldName": "seller", "selector": "$.seller_id" },
    { "fieldName": "pictures", "selector": "$.pictures[*].url" }
  ]
}
```

Limites: token client_credentials tem rate limit mais apertado. Para scraping em volume, o fluxo OAuth de usuário é melhor.

---

## Shopee

**Status em 2026:** O endpoint `shopee.com.br/api/v4/search/search_items` é o que o site usa internamente, mas está **protegido por anti-bot** (`af-ac-enc-dat` token derivado por JS + assinatura de request). Sem executar o JS da página, chamadas retornam `403` (`error: 90309999`).

### Caminhos viáveis

**A. Shopee Open Platform API (oficial, requer registro como parceiro):**
- Portal: https://open.shopee.com/
- Precisa ser vendedor, afiliado ou parceiro aprovado. Fluxo OAuth específico. Não é uma opção para quem só quer consultar catálogo público.

**B. Reaproveitar cookies/headers de uma sessão válida** (gambiarra):
   1. Abra shopee.com.br no navegador e faça uma busca.
   2. No DevTools → Network → pegue o request `/api/v4/search/search_items` e copie os headers (`cookie`, `af-ac-enc-dat`, `x-api-source`, `x-sap-ri`, etc.).
   3. Use esses headers no nosso `ApiExtractor`:

```json
{
  "sourceType": "api",
  "sourceUrl": "https://shopee.com.br/api/v4/search/search_items?by=relevancy&keyword=notebook&limit=10&newest=0&order=desc&page_type=search",
  "headers": {
    "User-Agent": "Mozilla/5.0 ...",
    "Cookie": "SPC_F=...; SPC_EC=...; csrftoken=...",
    "x-api-source": "pc",
    "af-ac-enc-dat": "<copiado do DevTools>",
    "Referer": "https://shopee.com.br/search?keyword=notebook"
  },
  "extractors": [
    { "fieldName": "names", "selector": "$.items[*].item_basic.name" },
    { "fieldName": "prices", "selector": "$.items[*].item_basic.price" },
    { "fieldName": "shops", "selector": "$.items[*].item_basic.shop_location" }
  ]
}
```

⚠️ Cookies expiram em horas/dias. Não é sustentável para produção — a API oficial é o caminho correto se você precisa de dados contínuos.

**C. Playwright com plugin stealth** (caminho técnico):
- `sourceType=spa` + Playwright, mas rodando com extras de stealth (evadir detecção de `navigator.webdriver`). O Playwright Java vanilla **não** tem stealth embutido — você teria que chamar o Python Playwright via subprocess ou rodar um Node sidecar. Fora do escopo do projeto atual.

---

## OLX

**Status em 2026:** A OLX roteia todo tráfego público por Cloudflare. Chamadas diretas (sem resolver o challenge JS) recebem a página HTML do Cloudflare "Attention Required" — o nosso `ApiExtractor` detecta isso e falha com "API did not return JSON".

### Opções

**A. OLX Pro API (oficial, requer parceria):**
- Dashboard: https://ajuda.olx.com.br/olx-pro
- Requer cadastro como empresa vendedora. Não é aberto pra consulta genérica.

**B. HTML scraping via `sourceType=site` com User-Agent real:**
   A OLX renderiza uma boa parte do HTML no servidor. Crawl via Jsoup funciona **parcialmente** se o Cloudflare não disparar o challenge (depende de IP reputation):

```json
{
  "sourceType": "site",
  "sourceUrl": "https://www.olx.com.br/brasil?q=playstation%205",
  "headers": {
    "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    "Accept-Language": "pt-BR,pt;q=0.9"
  },
  "maxPages": 5,
  "maxDepth": 1,
  "delayMs": 2000,
  "sameHost": true,
  "extractors": []
}
```

Se o Cloudflare permitir, o default extractor pega `<title>`, `<h1>` e preços de texto. Se bloquear, você recebe o HTML do Cloudflare e nenhum campo útil.

**C. Resolver Cloudflare com Playwright + stealth** — mesma limitação que a Shopee; fora do escopo atual.

---

## Resumo honesto

| Fornecedor | Caminho viável | Esforço | Sustentável |
|---|---|---|---|
| Mercado Livre | OAuth + Authorization header | Baixo (registrar app) | Sim |
| Shopee | API oficial (parceiro) **ou** cookies de sessão | Alto/médio | Só via oficial |
| OLX | OLX Pro API **ou** HTML com sorte | Alto | Só via oficial |

Nosso `ApiExtractor` é genérico — aceita qualquer JSON API com auth via headers. A parte difícil de scraping em 2026 **não é o cliente HTTP, é a autenticação/anti-bot dos fornecedores**.

---

## Notas de teste (drgames.com.br + Shopee via Playwright)

### djrgames.com.br — loja de jogos usados (funciona!)

Teste feito em 2026-04-19: `https://djrgames.com.br/` responde **302 → `/leilao/public/store/index.php`** (loja). A página tem um `<div class="products-grid">` com cards, cada card com `<a href="view.php?id=N" class="cardlink">` para páginas de produto. O `robots.txt` tem `User-agent: *` + `Allow: /` + `Disallow` só em áreas internas (admin, data, storage, vendor, etc.) — nosso crawler é bem-vindo.

Request usada:

```json
{
  "sourceType": "site",
  "sourceUrl": "https://djrgames.com.br/",
  "maxPages": 12,
  "maxDepth": 2,
  "delayMs": 400,
  "respectRobotsTxt": true,
  "sameHost": true,
  "extractors": [
    {"fieldName": "pageTitle", "selector": "title"},
    {"fieldName": "price", "selector": ".price"},
    {"fieldName": "image", "selector": ".main-img", "attribute": "src"}
  ]
}
```

Resultado real em <6 s, 12 páginas visitadas, incluindo 4 produtos com título e preço:

| # | URL | título | preço |
|---|---|---|---|
| … | `/leilao/public/store/view.php?id=1059` | Madagascar Kartz — Loja DJRGAMES | R$ 85,00 680 DJR |
| … | `/leilao/public/store/view.php?id=1058` | Tomb Raider: Underworld — Loja DJRGAMES | R$ 75,00 600 DJR |
| … | `/leilao/public/store/view.php?id=1057` | Deus Ex: Human Revolution (LACRADO) — Loja DJRGAMES | R$ 110,00 880 DJR |
| … | `/leilao/public/store/view.php?id=1054` | Heavy Fire: Shattered Spear — Loja DJRGAMES | R$ 85,00 680 DJR |

### Configuração completa — catálogo inteiro (113/113 produtos)

Pra pegar o **catálogo inteiro** com preço BRL (convertido para `BigDecimal`), preço em DJR coins e URL da imagem:

```json
{
  "sourceType": "site",
  "sourceUrl": "https://djrgames.com.br/",
  "maxPages": 200,
  "maxDepth": 3,
  "delayMs": 200,
  "respectRobotsTxt": true,
  "sameHost": true,
  "pathPattern": "^/(leilao/public/store/(view|index)\\.php|$)",
  "extractors": [
    {"fieldName": "pageTitle", "selector": "title"},
    {"fieldName": "priceBRL", "selector": ".price", "regex": "R\\$\\s*[0-9.,]+", "currency": "BRL"},
    {"fieldName": "priceDJR", "selector": ".price", "regex": "[0-9]+\\s*DJR"},
    {"fieldName": "imageUrl", "selector": ".main-img", "attribute": "style", "regex": "(?<=url\\(')[^']+(?=')"}
  ]
}
```

Resultado observado em 2026-04-19 (crawl durou ~30 s com `delayMs=200`):

- **total visited=129** (16 páginas de navegação/index + 113 páginas de produto)
- **priceBRL: 113/113** (cada produto com `BigDecimal(preço)`)
- **imageUrl: 113/113** (URL relativa do `img.php?src=…`)

#### Detalhes importantes

- **Por que 113 e não 123?** A página `index.php?mode=3d&page=1` mostra 10 produtos em destaque (IDs 970–981), mas esses 10 IDs **já estão** na listagem normal (aparecem na `index.php?page=3`). O `mode=3d` é um filtro visual, não uma categoria com produtos exclusivos. A união `pages 1..5` = 113 produtos únicos = catálogo total.

- **`pathPattern` importante.** Sem ele, o BFS gastava cotas em `login.php`, `cart.php`, `sorteios.php`, `djrcoins_extrato.php` (este último retorna 401 — é pulado, mas consome slot). O regex `^/(leilao/public/store/(view|index)\.php|$)` aceita **só** home, listagens e páginas de produto.

- **`.price` concatena BRL + DJR.** O HTML tem `<span class="price">R$ 85,00 680 DJR</span>`. Um único seletor extrai ambos; regex separa:
  - `R\$\s*[0-9.,]+` → "R$ 85,00"
  - `[0-9]+\s*DJR` → "680 DJR"

- **Imagem é CSS inline, não `<img>`.** A estrutura é `<div class="main-img" style="background-image:url('img.php?src=...')"></div>`. Não tem atributo `src`. Extrai via `attribute: "style"` + regex com lookbehind/lookahead `(?<=url\(')[^']+(?=')`.

- **Paginação é descoberta automaticamente.** A home linka `?page=2,3,4,5` — cada depth+1. Com `maxDepth=3`, o BFS: home (d=0) → `?page=N` (d=1) → `view.php?id=X` (d=2). Tudo sob `maxDepth=3`.

- **401 em páginas privadas.** `djrcoins_extrato.php` e algumas outras exigem auth. O crawler loga `WARN Failed to fetch … 401` e segue.

### Shopee via Playwright — o que esperar

Depois de `sudo playwright install-deps chromium` + `playwright install chromium`, o Playwright Java consegue renderizar shopee.com.br. No teste real observado:

- **Navegação funcionou** — o Chromium carregou a página.
- **`title` foi extraído** (`Extracted 1 fields from SPA`).
- **`h1` deu timeout** — a home da Shopee não tem um `<h1>` no DOM pós-hidratação, então aguardar por ele estoura o timeout.

A partir desta versão o `PlaywrightWebExtractor` usa **timeout por seletor curto** (default 3s, configurável via `selectorTimeoutMs`). Seletores que não existem são pulados silenciosamente em vez de travar 30s.

Exemplo com timeout curto:

```json
{
  "sourceType": "spa",
  "sourceUrl": "https://shopee.com.br",
  "timeoutSeconds": 30,
  "selectorTimeoutMs": 2000,
  "extractors": [
    {"fieldName": "title", "selector": "title"},
    {"fieldName": "navLinks", "selector": "a[href*='/search']"},
    {"fieldName": "productNames", "selector": "[class*='item-card'] [class*='name']"}
  ]
}
```

**Sobre o warning do Playwright** `Host system is missing dependencies` (libxslt, libwoff2dec, libvpx, libevent, …): esses libs são **só para Firefox/Webkit**. Se você só usa Chromium, o warning é cosmético. O `playwright install-deps chromium` instala o que importa. Se quiser silenciar o warning, rode `playwright install chromium` (sem instalar Firefox/Webkit) em vez de `playwright install`.

Para uso real contra Shopee, ainda vale o aviso anterior: mesmo com Playwright funcionando, o site detecta `navigator.webdriver` e costuma mostrar challenge. O caminho sustentável é um dos da seção Shopee acima (Open Platform API oficial ou cookies de sessão).
