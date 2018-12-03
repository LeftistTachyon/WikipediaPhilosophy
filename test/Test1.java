
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Test1 {
    public static void main(String[] args) throws IOException {
        Document get = Jsoup.connect("https://en.wikipedia.org/wiki/Philosophy").get();
        System.out.println(get.title());
    }
}