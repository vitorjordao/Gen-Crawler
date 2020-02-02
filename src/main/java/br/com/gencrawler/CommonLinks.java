package br.com.gencrawler;

public class CommonLinks {

    public static String baseUrl(final String fullUrl){
        return fullUrl.substring(0, fullUrl.indexOf("/", 8));
    }

    public static boolean is(final String url, final String fullUrl) {
        return fullUrl.matches("^.*?(" + url + ").*$");
    }

}