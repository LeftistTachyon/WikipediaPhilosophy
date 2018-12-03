package wikiphil;

import java.io.IOException;
import java.util.HashSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * The main class
 * @author Jed Wang
 */
public class Main {
    /**
     * The main method
     * @param args the command line arguments
     * @throws java.io.IOException AAAAAAAAAAAAAAAAAAAAs
     */
    public static void main(String[] args) throws IOException {
        /*int cores = Runtime.getRuntime().availableProcessors();
        while(cores-->0) {
            new Thread(() -> {
                for(int i = 0; i < 10; i++) {
                    try {
                        run_();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
        }*/
        for(int i = 0; i < 1; i++) {
            run_();
        }
    }
    
    /**
     * Loops through a single random page
     * @throws IOException if something goes wrong
     */
    public static void run_() throws IOException {
        HashSet<String> sanity = new HashSet<>();
        /*Document current = Jsoup.parse(parse(Jsoup.connect(
                    "https://en.wikipedia.org/wiki/Special:Random")
                .get().outerHtml()));*/
        Document current = Jsoup.connect(
                "https://en.wikipedia.org/wiki/Special:Random").get();
        while(!current.title().equals("Philosophy - Wikipedia")) {
            String title = current.selectFirst("h1#firstHeading").text();
            System.out.println(title);
            if(!sanity.add(title)) {
                System.err.println("Oh noes! We\'re in a loop!\n");
                return;
            }
            Elements bodyStuff = current.select("div#bodyContent");
            Elements parags = bodyStuff.select("p");
            Element toGo = null;
            System.out.println(parags.select("a[href]"));
            if(parags.select("a[href]").isEmpty()) {
                outer: for (Element parag : parags) {
                    Document p = Jsoup.parse(parse(parag.outerHtml()));
                    Elements links = p.select("a[href]");
                    if(links.isEmpty()) continue;
                    for(Element link : links) {
                        String linkHref = link.attr("href");
                        if(!"".equals(linkHref) && 
                                !linkHref.contains("#") && !linkHref.contains(":") 
                                && !linkHref.contains("redlink") && 
                                !linkHref.contains("upload.wikimedia.org")) {
                            toGo = link;
                            break outer;
                        }
                    }
                }
            } else {
                Elements listElements = bodyStuff.select("li");
                outer: for(Element listElement: listElements) {
                    Document li = Jsoup.parse(parse(listElement.outerHtml()));
                    Elements links = li.select("a[href]");
                    if(links.isEmpty()) continue;
                    for(Element link : links) {
                        String linkHref = link.attr("href");
                        if(!linkHref.contains("#") && !linkHref.contains(":") 
                                && !linkHref.contains("redlink") && 
                                !linkHref.contains("upload.wikimedia.org")) {
                            toGo = link;
                            break outer;
                        }
                    }
                }
            }
            /*Elements links = parags.select("a[href]");
            Element toGo = null;
            if(links.isEmpty()) {
                Elements listLinks = bodyStuff.select("li").select("a[href]");
                for(Element listLink : listLinks) {
                    String linkHref = listLink.attr("href");
                    if(!linkHref.contains("#") && !linkHref.contains(":") 
                            && !linkHref.contains("redlink") && 
                            !linkHref.contains("upload.wikimedia.org")) {
                        toGo = listLink;
                        break;
                    }
                }
            } else {
                for(Element link : links) {
                    String linkHref = link.attr("href");
                    if(!"".equals(linkHref) && 
                            !linkHref.contains("#") && !linkHref.contains(":") 
                            && !linkHref.contains("redlink") && 
                            !linkHref.contains("upload.wikimedia.org")) {
                        toGo = link;
                        break;
                    }
                }
            }*/
            if(toGo == null) {
                System.err.println("AAAAAAAAAAAAAAAAAAAAAAAA\n");
                return;
            }
            /*current = Jsoup.parse(parse(
                            Jsoup.connect(
                    "https://en.wikipedia.org" + toGo.attr("href"))
                .get().outerHtml()));*/
            current = Jsoup.connect(
                    "https://en.wikipedia.org" + toGo.attr("href")).get();
        }
        Element title = current.selectFirst("h1#firstHeading");
        System.out.println("Got to Philosophy! " + title.text() + "\n");
    }
    
    public static String parse(String document) {
        /*String[] data = document.split("\\(.*?\\)");
        String total = "";
        for (String string : data) {
            total += string;
        }
        return total;*/
        return document.replaceAll("\\(.*?\\)", "");
    }
}