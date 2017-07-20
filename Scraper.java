import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Scraper {

    private String corpus = "";

    
    public Scraper(String url) throws IOException {
  
            Document document = Jsoup.connect(url).get();
            Elements links = document.select("p");
            for (Element link : links) {
                corpus += link.text() + " ";
               // System.out.println(corpus);
            }
   
    }

    
    public String getCorpus() {return corpus;}

    
 
    }

