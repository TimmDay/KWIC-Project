import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Scraper {

    private String corpus = "";

    
    public Scraper(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            Elements links = document.select("p");
            for (Element link : links) {
                corpus += link.text() + " ";
               // System.out.println(corpus);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    public String getCorpus() {return corpus;}

    
    public static void main(String[] args) throws Exception {
      //  Scraper sc = new Scraper("https://en.wikipedia.org/wiki/Ranking");
    }

}


