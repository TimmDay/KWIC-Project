import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;

/**
 * SCRAPER
 * a class to retrieve text from the web
 * specifically, the contents of the paragraph tags of english language wikipedia articles
 */
public class Scraper {

    private String corpus = "";  // instance variable, to store the result of the web scrape

    /**
     * CONSTRUCTOR
     * use jsoup to scrape the html of a wikipedia article for the text contained by paragraph tags
     * @param url
     * @throws IOException
     */
    public Scraper(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        Elements links = document.select("p");
        for (Element link : links) {
            corpus += link.text() + " "; // add the contents
        }
    }

    // GET METHOD
    /**
     * return the result of the jsoup web scrape as a string
     * @return String - the result of the web scrape
     */
    public String getCorpus() {return corpus;}
}

