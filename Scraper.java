import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Scraper {

    private String corpus = "";

    
    public Scraper(String url) throws IOException {
   //     try {
            Document document = Jsoup.connect(url).get();
            Elements links = document.select("p");
            for (Element link : links) {
                corpus += link.text() + " ";
               // System.out.println(corpus);
            }
       /// } catch (IOException e) {
       ///    e.printStackTrace();
      //  }
    }

    
    public String getCorpus() {return corpus;}

    
    public static void main(String[] args) {
        try {
            Scraper sc = new Scraper("https://enwikipedia.org/wiki/Ranking"); 
        } catch (IOException ex) {
            System.out.println("sdgdgadg");
            
        }
            }
    }

