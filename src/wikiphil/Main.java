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
        /*final int cores = Runtime.getRuntime().availableProcessors(), 
                aaa = 100 / cores;
        for(int z = 0;z<cores;z++) {
            new Thread(() -> {
                for(int i = 0; i < aaa; i++) {
                    try {
                        double start = System.nanoTime();
                        Document tempD = Jsoup.connect(
                                "https://en.wikipedia.org/wiki/Special:Random").get();
                        String temp = tempD.selectFirst("h1#firstHeading").text() + 
                                ": " + hopsToPhilosophy(
                                tempD.location()) + " hops";
                        System.out.printf("%-75s", temp);
                        double total = System.nanoTime() - start;
                        System.out.printf("%.4fms%n", total/1000000);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
        }*/
        for(int i = 0; i < 100; i++) {
            double start = System.nanoTime();
            Document tempD = Jsoup.connect(
                    "https://en.wikipedia.org/wiki/Special:Random").get();
            String temp = tempD.selectFirst("h1#firstHeading").text() + 
                    ": " + forceToPhilosophy(
                    tempD.location()) + " hops";
            System.out.printf("%-100s", temp);
            double total = System.nanoTime() - start;
            System.out.printf("%.4fms%n", total/1000000);
        }
        // traceToPhilosophy("https://en.wikipedia.org/wiki/State_governments_of_India");
    }
    
    /**
     * Loops through a single random page
     * @param toConnect the page to connect to
     * @return how many hops to Philosophy, -1 if it doesn't
     * @throws IOException if something goes wrong
     */
    public static int hopsToPhilosophy(String toConnect) throws IOException {
        HashSet<String> sanity = new HashSet<>();
        /*Document current = Jsoup.parse(parse(Jsoup.connect(
                    "https://en.wikipedia.org/wiki/Special:Random")
                .get().outerHtml()));*/
        Document current = Jsoup.connect(
                toConnect).get();
        int output = 0;
        while(!current.title().equals("Philosophy - Wikipedia")) {
            String title = current.selectFirst("h1#firstHeading").text();
            // System.out.println(title);
            if(!sanity.add(title)) {
                /*if(title.equals("Existence") || title.equals("Reality"))
                    return -2;*/
                return -output-1;
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
                        if(link.parent().is("span#coordinates")) 
                            continue outer;
                        String outer = link.outerHtml();
                        if(map.get(map.floorKey(temp.indexOf(outer))) != 0) 
                            continue;
                        String linkHref = link.attr("href");
                        if(!linkHref.contains("#") && !linkHref.contains(":")
                                && !linkHref.contains("action=edit") && 
                                !linkHref.contains("upload.wikimedia.org")) {
                            toGo = link;
                            break outer;
                        }
                    }
                }
            } else {
                outer: for (Element parag : parags) {
                    if(parag.parent().tagName().equals("td")) 
                        continue;
                    String temp = parag.outerHtml();
                    TreeMap<Integer, Integer> map = map(temp);
                    Document p = Jsoup.parse(temp);
                    Elements links = p.select("a[href]");
                    if(links.isEmpty()) continue;
                    for(Element link : links) {
                        if(link.parent().is("span#coordinates")) 
                            continue outer;
                        String outer = link.outerHtml();
                        if(map.get(map.floorKey(temp.indexOf(outer))) != 0) 
                            continue;
                        String linkHref = link.attr("href");
                        if(!"".equals(linkHref) &&
                                !linkHref.contains("#") && !linkHref.contains(":") 
                                && !linkHref.contains("action=edit") &&
                                !linkHref.contains("upload.wikimedia.org")) {
                            toGo = link;
                            break outer;
                        }
                    }
                }
            }
            if(toGo == null) {
                return -output-1;
            }
            current = Jsoup.connect(
                    "https://en.wikipedia.org" + toGo.attr("href")).get();
            output++;
        }
        return output;
    }
    
    /**
     * Traces an article to philosophy, for debugging purposes
     * @param toConnect the article to connect to
     * @throws IOException if something goes wrong
     */
    public static void traceToPhilosophy(String toConnect) throws IOException {
        HashSet<String> sanity = new HashSet<>();
        Document current = Jsoup.connect(
                toConnect).get();
        while(!current.title().equals("Philosophy - Wikipedia")) {
            String title = current.selectFirst("h1#firstHeading").text();
            System.out.println(title);
            if(!sanity.add(title)) {
                if(title.equals("Existence") || title.equals("Reality"))
                    System.out.println("Exited due to E-R loop");
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
                        if(link.parent().is("span#coordinates")) 
                            continue outer;
                        String outer = link.outerHtml();
                        if(map.get(map.floorKey(temp.indexOf(outer))) != 0) 
                            continue;
                        String linkHref = link.attr("href");
                        if(!linkHref.contains("#") && !linkHref.contains(":")
                                && !linkHref.contains("redlink") && 
                                !linkHref.contains("upload.wikimedia.org")) {
                            toGo = link;
                            System.out.println(link.parents());
                            break outer;
                        }
                    }
                }
            } else {
                outer: for (Element parag : parags) {
                    if(parag.parent().tagName().equals("td")) 
                        continue;
                    String temp = parag.outerHtml();
                    TreeMap<Integer, Integer> map = map(temp);
                    Document p = Jsoup.parse(temp);
                    Elements links = p.select("a[href]");
                    if(links.isEmpty()) continue;
                    for(Element link : links) {
                        if(link.parent().is("span#coordinates")) 
                            continue outer;
                        String outer = link.outerHtml();
                        if(map.get(map.floorKey(temp.indexOf(outer))) != 0) 
                            continue;
                        String linkHref = link.attr("href");
                        if(!"".equals(linkHref) &&
                                !linkHref.contains("#") && !linkHref.contains(":") 
                                && !linkHref.contains("redlink") &&
                                !linkHref.contains("upload.wikimedia.org")) {
                            toGo = link;
                            for(Element parent : toGo.parents()) {
                                System.out.print(parent.nodeName() + " ");
                            }
                            System.out.println();
                            break outer;
                        }
                    }
                }
            }
            if(toGo == null) {
                return;
            }
            current = Jsoup.connect(
                    "https://en.wikipedia.org" + toGo.attr("href")).get();
        }
    }
    
    /**
     * Forces a link pathway to Philosophy
     * @param toConnect the page to start at
     * @return the number of steps on the forced pathway
     * @throws IOException if something goes wrong
     */
    public static int forceToPhilosophy(String toConnect) throws IOException {
        return forceToPhilosophy(toConnect, new HashSet<>(), 0);
    }
    
    /**
     * Forces a link pathway to Philosophy
     * @param toConnect the page to start at
     * @param visited a HashSet of pages that have been already visited
     * @param hops the number of current hops already taken
     * @return the number of steps on the forced pathway
     * @throws IOException if something goes wrong
     */
    private static int forceToPhilosophy(String toConnect, 
            HashSet<String> visited, int hops) throws IOException {
        /*Document current = Jsoup.parse(parse(Jsoup.connect(
                    "https://en.wikipedia.org/wiki/Special:Random")
                .get().outerHtml()));*/
        Document current = Jsoup.connect(
                toConnect).get();
        if(!current.title().equals("Philosophy - Wikipedia")) {
            String title = current.selectFirst("h1#firstHeading").text();
            // System.out.println(title);
            if(!visited.add(title)) {
                /*if(title.equals("Existence") || title.equals("Reality"))
                    return -2;*/
                return -hops-1;
            }
            Elements bodyStuff = current.select("div#bodyContent");
            Elements parags = bodyStuff.select("p");
            if(parags.select("a[href]").isEmpty()) {
                Elements listElements = bodyStuff.select("li");
                outer: for(Element listElement: listElements) {
                    String temp = listElement.outerHtml();
                    TreeMap<Integer, Integer> map = map(temp);
                    Document li = Jsoup.parse(temp);
                    Elements links = li.select("a[href]");
                    if(links.isEmpty()) continue;
                    for(Element link : links) {
                        if(link.parent().is("span#coordinates")) 
                            continue outer;
                        String outer = link.outerHtml();
                        if(map.get(map.floorKey(temp.indexOf(outer))) != 0) 
                            continue;
                        String linkHref = link.attr("href");
                        if(!linkHref.contains("#") && !linkHref.contains(":")
                                && !linkHref.contains("action=edit") && 
                                !linkHref.contains("upload.wikimedia.org")) {
                            // try this one
                            int force = forceToPhilosophy(
                                    "https://en.wikipedia.org" 
                                            + link.attr("href"), 
                                    new HashSet<>(visited), hops+1);
                            if(force >= 0) return force;
                        }
                    }
                }
            } else {
                outer: for (Element parag : parags) {
                    if(parag.parent().tagName().equals("td")) 
                        continue;
                    String temp = parag.outerHtml();
                    TreeMap<Integer, Integer> map = map(temp);
                    Document p = Jsoup.parse(temp);
                    Elements links = p.select("a[href]");
                    if(links.isEmpty()) continue;
                    for(Element link : links) {
                        if(link.parent().is("span#coordinates")) 
                            continue outer;
                        String outer = link.outerHtml();
                        if(map.get(map.floorKey(temp.indexOf(outer))) != 0) 
                            continue;
                        String linkHref = link.attr("href");
                        if(!"".equals(linkHref) &&
                                !linkHref.contains("#") && !linkHref.contains(":") 
                                && !linkHref.contains("action=edit") &&
                                !linkHref.contains("upload.wikimedia.org")) {
                            // try this one
                            int force = forceToPhilosophy(
                                    "https://en.wikipedia.org" 
                                            + link.attr("href"), 
                                    new HashSet<>(visited), hops+1);
                            if(force >= 0) return force;
                        }
                    }
                }
            }
            return -hops-1;
        }
        return hops;
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
        int open, close, add = 0, in = 0;
        for(;;) {
            open = copy.indexOf("("); 
            close = copy.indexOf(")");
            if(open == -1) {
                if(close == -1) {
                    // nothing works
                    break;
                } else {
                    // only closing
                    // temp = close;
                    if(in > 0) {
                        parentheses.put(add + close,
                                --in);
                    }
                    close++;
                    add += close;
                    copy = copy.substring(close);
                }
            } else {
                if(close == -1) {
                    // only opening
                    // temp = open;
                    parentheses.put(add + open, 
                            ++in);
                    open++;
                    add += open;
                    copy = copy.substring(open);
                } else {
                    // both
                    if(open < close) {
                        parentheses.put(add + open, 
                                ++in);
                        open++;
                        add += open;
                        copy = copy.substring(open);
                    } else {
                        if(in > 0) {
                            parentheses.put(add + close,
                                    --in);
                        }
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