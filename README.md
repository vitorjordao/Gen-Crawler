# Gen-Crawler

This is a crawler project make in Java language

[![Clojars Project](https://img.shields.io/clojars/v/br.com.gencrawler.crawler/gen-crawler.svg)](https://clojars.org/br.com.gencrawler.crawler/gen-crawler)

## How to start

There are two methods to build your crawler, the first one is SimpleCollector, this method is focused in performance, it's will make so downloading the HTML page and extracting the information, the second method is AjaxCollector, it's focused in will working at pages with AJAX.

### SimpleCollector

#### Create collector:

```
final SimpleCollector collector = new SimpleCollector(<URL>, <FINDER>, <MATCHER>);
```

URL     = Crawled url page         = "https://www.americanas.com.br/produto/122597474/10692-lego-classic-pecas-criativas?pfm_carac=lego&pfm_page=search&pfm_pos=grid&pfm_type=search_page"

FINDER  = CSS selector             = ".price__SalesPrice-ej7lo8-2"

MATCHER = Regex applied in the tag = "^.*?().*$"

#### Run crawler:

```
collector.run();
```

#### Get a list of itens:

```
List<String> itens = collector.getItems();
```

#### Get a set of urls:

```
Set<String> urls = collector.getURLs();
```

### AjaxCollector

#### Set a chromedriver:

```
System.setProperty("webdriver.chrome.driver", <PATH>);
```

PATH = A path for chromedriver = "./chromedriver"

#### Create collector:

```
final AjaxCollector collector = new AjaxCollector(<URL>, <FINDER>, <MATCHER>);
```

URL     = Crawled url page         = "https://www.americanas.com.br/produto/122597474/10692-lego-classic-pecas-criativas?pfm_carac=lego&pfm_page=search&pfm_pos=grid&pfm_type=search_page"

FINDER  = CSS selector             = ".price__SalesPrice-ej7lo8-2"

MATCHER = Regex applied in the tag = "^.*?().*$"

#### Run crawler:

```
collector.run();
```

#### Get a list of itens:

```
List<String> itens = collector.getItems();
```

#### Get a set of urls:

```
Set<String> urls = collector.getURLs();
```

# Help with grammar and spelling errors:
Because I am not fluent in English, they are likely to have grammar and spelling mistakes, so I will accept any help in this.

# Help with documentation:
I accept any help with the documentation you add to the project.

# Help with the code:
Any help in the code that helps to improve the quality or quantity of futures will be totally welcome.
