package br.com.gencrawler.crawler;

public class Helper{

    // Non Ajax
    public String getUrl(){
        return "https://www.americanas.com.br/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?ordenacao=topSelling&origem=omega&chave=brd_hs_dt_0_livros-6-ao-9_material-escolar18&pfm_carac=BLOCO%201&pfm_index=0&pfm_page=special&pfm_pos=contenttop3&pfm_type=vit_spacey";
    }

    public String getPaginator(){
        return "a[href^=\"/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?\"]";
    }

    public String getFinder(){
        return "div[class^=\"product-grid-item\"] a[class^=\"Link-bwhjk3-2\"]";
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

    public String getAjaxFinder2(){
        return "strong[class^=\"skuPrice\"]";
    }

    public String getAjaxMatch(){
        return "^.*?(R$).*$";
    }

    public void setProperty() {
        System.setProperty("webdriver.gecko.driver", "/home/v/path/geckodriver");
    }
}