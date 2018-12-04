package wikiphil;

import java.io.IOException;
import java.util.HashSet;
import java.util.TreeMap;
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
                "https://en.wikipedia.org/wiki/United_States").get();
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
            if(parags.select("a[href]").isEmpty()) {
                Elements listElements = bodyStuff.select("li");
                outer: for(Element listElement: listElements) {
                    String temp = listElement.outerHtml();
                    TreeMap<Integer, Integer> map = map(temp);
                    Document li = Jsoup.parse(temp);
                    Elements links = li.select("a[href]");
                    if(links.isEmpty()) continue;
                    for(Element link : links) {
                        if(link.text().equals("Coordinates")) continue outer;
                        String linkHref = link.attr("href");
                        if(!linkHref.contains("#") && !linkHref.contains(":")
                                && !linkHref.contains("redlink") && 
                                !linkHref.contains("upload.wikimedia.org")) {
                            toGo = link;
                            break outer;
                        }
                    }
                }
            } else {
                outer: for (Element parag : parags) {
                    String temp = parag.outerHtml();
                    TreeMap<Integer, Integer> map = map(temp);
                    Document p = Jsoup.parse(temp);
                    Elements links = p.select("a[href]");
                    if(links.isEmpty()) continue;
                    for(Element link : links) {
                        if(link.text().equals("Coordinates")) continue outer;
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
            }
            if(toGo == null) {
                System.err.println("AAAAAAAAAAAAAAAAAAAAAAAA\n");
                return;
            }
            current = Jsoup.connect(
                    "https://en.wikipedia.org" + toGo.attr("href")).get();
        }
        Element title = current.selectFirst("h1#firstHeading");
        System.out.println("Got to Philosophy! " + title.text() + "\n");
    }
    
    /**
     * Parses a document
     * @param document the String representation of the document
     * @return a parsed document
     * @deprecated don't use this
     */
    public static String parse(final String document) {
        String copy = document;
        TreeMap<Integer, Integer> parentheses = new TreeMap<>();
        parentheses.put(0, 0);
        int open = 0, close = 0, add = 0;
        while(open >= 0 || close >= 0) {
            open = copy.indexOf("("); 
            close = copy.indexOf(")");
            if(open == -1) {
                if(close == -1) {
                    // nothing works
                    break;
                } else {
                    // only closing
                    // temp = close;
                    parentheses.put(add + close, 
                            parentheses.get(parentheses.lastKey()) - 1);
                    close++;
                    add += close;
                    copy = copy.substring(close);
                }
            } else {
                if(close == -1) {
                    // only opening
                    // temp = open;
                    parentheses.put(add + open, 
                            parentheses.get(parentheses.lastKey()) + 1);
                    open++;
                    add += open;
                    copy = copy.substring(open);
                } else {
                    // both
                    if(open < close) {
                        parentheses.put(add + open, 
                                parentheses.get(parentheses.lastKey()) + 1);
                        open++;
                        add += open;
                        copy = copy.substring(open);
                    } else {
                        parentheses.put(add + close, 
                                parentheses.get(parentheses.lastKey()) - 1);
                        close++;
                        add += close;
                        copy = copy.substring(close);
                    }
                }
            }
        }
        System.out.println(parentheses.toString());
        return document;
        // return document.replaceAll("\\(.*?\\)", "");
    }
    
    /**
     * Map the thing
     * @param document String thing
     * @return Map
     */
    public static TreeMap<Integer, Integer> map(final String document) {
        String copy = document;
        TreeMap<Integer, Integer> parentheses = new TreeMap<>();
        parentheses.put(0, 0);
        int open = 0, close = 0, add = 0;
        while(open >= 0 || close >= 0) {
            open = copy.indexOf("("); 
            close = copy.indexOf(")");
            if(open == -1) {
                if(close == -1) {
                    // nothing works
                    break;
                } else {
                    // only closing
                    // temp = close;
                    parentheses.put(add + close, 
                            parentheses.get(parentheses.lastKey()) - 1);
                    close++;
                    add += close;
                    copy = copy.substring(close);
                }
            } else {
                if(close == -1) {
                    // only opening
                    // temp = open;
                    parentheses.put(add + open, 
                            parentheses.get(parentheses.lastKey()) + 1);
                    open++;
                    add += open;
                    copy = copy.substring(open);
                } else {
                    // both
                    if(open < close) {
                        parentheses.put(add + open, 
                                parentheses.get(parentheses.lastKey()) + 1);
                        open++;
                        add += open;
                        copy = copy.substring(open);
                    } else {
                        parentheses.put(add + close, 
                                parentheses.get(parentheses.lastKey()) - 1);
                        close++;
                        add += close;
                        copy = copy.substring(close);
                    }
                }
            }
        }
        return parentheses;
    }
}