package google;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;

public class Searcher {
    static final String GOOGLE_SEARCH_URL = "https://www.google.com/search";
    public static final int PRINT_PAGES_NUM = 17;

    public static HashMap<String, String> Search(String fileName, String fileType, int pageNum) throws Exception {

        String searchURL = GOOGLE_SEARCH_URL
                + "?q=" + fileName
                + " filetype:" + fileType
                + "&num=" + PRINT_PAGES_NUM
                + "&start=" + (pageNum - 1) * PRINT_PAGES_NUM;

        HashMap<String, String> hashResult = new HashMap<String, String>();

        Document doc = Jsoup.connect(searchURL).userAgent("Mozilla/5.0").get();

        Elements results = doc.select("h3.r > a");

        for (Element result : results) {
            String linkHref = result.attr("href");
            linkHref = linkHref.substring(7, linkHref.indexOf("&"));
            String linkText = result.text();

            hashResult.put(linkText, linkHref);
        }

        return hashResult;
    }
}
