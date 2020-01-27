package br.com.gencrawler.crawler;

public class Helper{

    // Non Ajax
    public String getUrl(){
        return "https://www.americanas.com.br/produto/116802208/livro-geografia-espaco-e-vivencia-8o-ano?pfm_carac=Ensino%20Fundamental%20-%206%C2%BA%20ao%209%C2%BA%20ano&pfm_index=1&pfm_page=category&pfm_pos=grid&pfm_type=vit_product_grid";
    }

    public String getFinder(){
        return "span[class^=\"price__SalesPrice\"]";
    }

    public String getMatch(){
        return "^.*?().*$";
    }

    // Ajax
    public String getAjaxUrl(){
        return "https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p";
    }

    public String getAjaxFinder(){
        return "bf-price__best";
    }

    public void setProperty() {
        System.setProperty("webdriver.chrome.driver", "/home/v/path/chromedriver");
    }
}