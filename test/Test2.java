
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Test2 {
    public static void main(String[] args) throws IOException {
        Document get = Jsoup.connect("https://en.wikipedia.org/wiki/Object").get();
        Elements bodyStuff = get.select("div#bodyContent");
        Elements parags = bodyStuff.select("p");
        Elements links = parags.select("a[href]");
        if(links.isEmpty()) {
            Elements listLinks = bodyStuff.select("li").select("a[href]");
            for(Element listLink : listLinks) {
                System.out.println(listLink);
            }
        } else {
            for(Element link : links) {
                System.out.println(link);
            }
        }
    }
}