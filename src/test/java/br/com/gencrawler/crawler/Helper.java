package br.com.gencrawler.crawler;

public class Helper{

    // Non Ajax
    public String getUrl(){
        return "https://www.americanas.com.br/produto/122597474/10692-lego-classic-pecas-criativas?pfm_carac=lego&pfm_page=search&pfm_pos=grid&pfm_type=search_page";
    }

    public String getFinder(){
        return ".price__SalesPrice-ej7lo8-2";
    }

    public String getMatch(){
        return "^.*?().*$";
    }

    // Ajax
    public String getAjaxUrl(){
        return "https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p";
    }

    public String getAjaxFinder(){
        return ".bf-price__best";
    }

    public void setProperty() {
        System.setProperty("webdriver.chrome.driver", "./chromedriver");
    }
}