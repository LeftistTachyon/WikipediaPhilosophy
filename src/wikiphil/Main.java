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
 *
 * @author Jed Wang
 */
public class Main {
    static final int PAGES_TO_VISIT = 1000;
    static double avgTime = 0, lastStart = 0;
    static int max = -1, toPhil = 0, finished = 0;
    static String maxTitle = null;
    
    static final Object PRINT_LOCK = new Object();

    /**
     * The main method
     *
     * @param args the command line arguments
     * @throws java.io.IOException AAAAAAAAAAAAAAAAAAAA
     */
    public static void main(String[] args) throws IOException {
        /*final int cores = Runtime.getRuntime().availableProcessors(), 
                aaa = 100 / cores;*/

        // the number of pages to try to get to philosophy from
        for (int i = 0; i < PAGES_TO_VISIT; i++) {
            lastStart = System.nanoTime();
            new Thread(() -> {
                try {
                    Document tempD = Jsoup.connect(
                            "https://en.wikipedia.org/wiki/Special:Random").get();
                    int hops = hopsToPhilosophy(tempD);
                    if (hops >= 0) {
                        toPhil++;
                    }
                    String temp = tempD.selectFirst("h1#firstHeading").text();
                    if (hops > max) {
                        max = hops;
                        maxTitle = temp;
                    }
                    double total = 0;
                    synchronized(PRINT_LOCK) {
                        System.out.printf("%-100s", temp
                                + ": " + hops + " hops");
                        total = System.nanoTime() - lastStart;
                        System.out.printf("%.3f ms%n", total /= 1000000);
                    }
                    avgTime += total;
                    lastStart = System.nanoTime();
                    
                    if(++finished == PAGES_TO_VISIT) {
                        System.out.printf("%nMax hops: %d hops - %s%n", max, maxTitle);
                        System.out.printf("Avg time: %.3f ms%n", avgTime / PAGES_TO_VISIT);
                        System.out.printf("%% to philosophy: %.2f%%%n",
                                ((double) toPhil * 100) / PAGES_TO_VISIT);
                    }
                } catch (IOException ex) {
                    System.err.println(ex.toString());
                }
            }).start();
        }

        /*double start = System.nanoTime();
        traceToPhilosophy("https://en.wikipedia.org/wiki/1858_in_architecture");
        // also 2018–19 Hamburger SV season
        double total = System.nanoTime() - start;
        System.out.printf("%nTotal:\t%.3f ms%n", total / 1000000);*/
    }

    /**
     * Loops through a single random page
     *
     * @param current the Document to start at
     * @return how many hops to Philosophy, -1 if it doesn't
     * @throws IOException if something goes wrong
     */
    public static int hopsToPhilosophy(Document current) throws IOException {
        HashSet<String> sanity = new HashSet<>();
        /*Document current = Jsoup.parse(parse(Jsoup.connect(
                    "https://en.wikipedia.org/wiki/Special:Random")
                .get().outerHtml()));*/
        int output = 0;
        while (!current.title().equals("Philosophy - Wikipedia")) {
            String title = current.selectFirst("h1#firstHeading").text();
            // System.out.println(maxTitle);
            if (!sanity.add(title)) {
                /*if(maxTitle.equals("Existence") || maxTitle.equals("Reality"))
                    return -2;*/
                return -output - 1;
            }

            current.select("table").remove();
            current.select("sup").remove();

            Elements bodyStuff = current.select("div#bodyContent");
            Elements parags = bodyStuff.select("p");
            Element toGo = null;
            if (parags.select("a[href]").isEmpty()) {
                Elements listElements = bodyStuff.select("li");
                outer:
                for (Element listElement : listElements) {
                    String temp = listElement.outerHtml();
                    temp = temp.replace("<br>", "");
                    temp = temp.replace("</br>", "");
                    TreeMap<Integer, Integer> map = map(temp);
                    Document li = Jsoup.parse(temp);
                    Elements links = li.select("a[href]");
                    if (links.isEmpty()) {
                        continue;
                    }
                    for (Element link : links) {
                        if (link.parent().is("span#coordinates")) {
                            continue outer;
                        }
                        String outer = link.outerHtml();
                        if (map.get(map.floorKey(temp.indexOf(outer))) != 0) {
                            continue;
                        }
                        String linkHref = link.attr("href");
                        if (!linkHref.contains("#") && !linkHref.contains(":")
                                && !linkHref.contains("action=edit")
                                && !linkHref.contains("upload.wikimedia.org")) {
                            toGo = link;
                            break outer;
                        }
                    }
                }
            } else {
                outer:
                for (Element parag : parags) {
                    String temp = parag.outerHtml();
                    temp = temp.replace("<br>", "");
                    temp = temp.replace("</br>", "");
                    TreeMap<Integer, Integer> map = map(temp);
                    Document p = Jsoup.parse(temp);
                    Elements links = p.select("a[href]");
                    if (links.isEmpty()) {
                        continue;
                    }
                    for (Element link : links) {
                        if (link.parent().is("span#coordinates")) {
                            continue outer;
                        }
                        String outer = link.outerHtml();
                        if (map.get(map.floorKey(temp.indexOf(outer))) != 0) {
                            continue;
                        }
                        String linkHref = link.attr("href");
                        if (!"".equals(linkHref)
                                && !linkHref.contains("#") && !linkHref.contains(":")
                                && !linkHref.contains("action=edit")
                                && !linkHref.contains("upload.wikimedia.org")) {
                            toGo = link;
                            break outer;
                        }
                    }
                }
                if (toGo == null) {
                    Elements listElements = bodyStuff.select("li");
                    outer:
                    for (Element listElement : listElements) {
                        String temp = listElement.outerHtml();
                        temp = temp.replace("<br>", "");
                        temp = temp.replace("</br>", "");
                        TreeMap<Integer, Integer> map = map(temp);
                        Document li = Jsoup.parse(temp);
                        Elements links = li.select("a[href]");
                        if (links.isEmpty()) {
                            continue;
                        }
                        for (Element link : links) {
                            if (link.parent().is("span#coordinates")) {
                                continue outer;
                            }
                            String outer = link.outerHtml();
                            if (map.get(map.floorKey(temp.indexOf(outer))) != 0) {
                                continue;
                            }
                            String linkHref = link.attr("href");
                            if (!linkHref.contains("#") && !linkHref.contains(":")
                                    && !linkHref.contains("action=edit")
                                    && !linkHref.contains("upload.wikimedia.org")) {
                                toGo = link;
                                break outer;
                            }
                        }
                    }
                }
            }
            if (toGo == null) {
                return -output - 1;
            }
            current = Jsoup.connect(
                    "https://en.wikipedia.org" + toGo.attr("href")).get();
            output++;
        }
        return output;
    }

    /**
     * Traces an article to philosophy, for debugging purposes
     *
     * @param toConnect the article to connect to
     * @throws IOException if something goes wrong
     */
    public static void traceToPhilosophy(String toConnect) throws IOException {
        int steps = 0;
        double start, total;
        start = System.nanoTime();
        HashSet<String> sanity = new HashSet<>();
        Document current = Jsoup.connect(
                toConnect).get();
        total = System.nanoTime() - start;
        System.out.printf("Connect:\t%.3f ms%n%n", total / 1000000);
        while (!current.title().equals("Philosophy - Wikipedia")) {
            start = System.nanoTime();
            String title = current.selectFirst("h1#firstHeading").text();
            System.out.println(title);
            if (!sanity.add(title)) {
                if (title.equals("Existence") || title.equals("Reality")) {
                    System.out.println("Exited due to E-R loop");
                }
                System.out.println("Couldn\'t reach philosophy.");
                System.out.println("Total steps: " + steps);
                return;
            }

            current.select("table").remove();
            current.select("sup").remove();

            Elements bodyStuff = current.select("div#bodyContent");
            Elements parags = bodyStuff.select("p");
            Element toGo = null;
            if (parags.select("a[href]").isEmpty()) {
                Elements listElements = bodyStuff.select("li");
                outer:
                for (Element listElement : listElements) {
                    String temp = listElement.outerHtml();
                    temp = temp.replace("<br>", "");
                    temp = temp.replace("</br>", "");

                    TreeMap<Integer, Integer> map = map(temp);
                    Document li = Jsoup.parse(temp);
                    Elements links = li.select("a[href]");
                    if (links.isEmpty()) {
                        continue;
                    }
                    for (Element link : links) {
                        if (link.parent().is("span#coordinates")) {
                            continue outer;
                        }
                        String outer = link.outerHtml();
                        if (map.get(map.floorKey(temp.indexOf(outer))) != 0) {
                            continue;
                        }
                        String linkHref = link.attr("href");
                        if (!linkHref.contains("#") && !linkHref.contains(":")
                                && !linkHref.contains("redlink")
                                && !linkHref.contains("upload.wikimedia.org")) {
                            toGo = link;
                            break outer;
                        }
                    }
                }
            } else {
                outer:
                for (Element parag : parags) {
                    String temp = parag.outerHtml();
                    temp = temp.replace("<br>", "");
                    temp = temp.replace("</br>", "");

                    TreeMap<Integer, Integer> map = map(temp);
                    Document p = Jsoup.parse(temp);
                    Elements links = p.select("a[href]");
                    if (links.isEmpty()) {
                        continue;
                    }
                    for (Element link : links) {
                        if (link.parent().is("span#coordinates")) {
                            continue outer;
                        }
                        String outer = link.outerHtml();
                        if (map.get(map.floorKey(temp.indexOf(outer))) != 0) {
                            continue;
                        }
                        String linkHref = link.attr("href");
                        if (!"".equals(linkHref)
                                && !linkHref.contains("#") && !linkHref.contains(":")
                                && !linkHref.contains("redlink")
                                && !linkHref.contains("upload.wikimedia.org")) {
                            toGo = link;
                            break outer;
                        }
                    }
                }
                if (toGo == null) {
                    Elements listElements = bodyStuff.select("li");
                    outer:
                    for (Element listElement : listElements) {
                        String temp = listElement.outerHtml();
                        temp = temp.replace("<br>", "");
                        temp = temp.replace("</br>", "");

                        TreeMap<Integer, Integer> map = map(temp);
                        Document li = Jsoup.parse(temp);
                        Elements links = li.select("a[href]");
                        if (links.isEmpty()) {
                            continue;
                        }
                        for (Element link : links) {
                            if (link.parent().is("span#coordinates")) {
                                continue outer;
                            }
                            String outer = link.outerHtml();
                            if (map.get(map.floorKey(temp.indexOf(outer))) != 0) {
                                continue;
                            }
                            String linkHref = link.attr("href");
                            if (!linkHref.contains("#") && !linkHref.contains(":")
                                    && !linkHref.contains("action=edit")
                                    && !linkHref.contains("upload.wikimedia.org")) {
                                toGo = link;
                                break outer;
                            }
                        }
                    }
                }
            }
            if (toGo == null) {
                System.out.println("Couldn\'t reach philosophy.");
                System.out.println("Total steps: " + steps);
                return;
            }
            total = System.nanoTime() - start;
            System.out.printf("Decision:\t%.3f ms%n", total / 1000000);
            start = System.nanoTime();
            current = Jsoup.connect(
                    "https://en.wikipedia.org" + toGo.attr("href")).get();
            total = System.nanoTime() - start;
            System.out.printf("Reconnect:\t%.3f ms%n%n", total / 1000000);
            steps++;
        }
        System.out.println("Reached philosophy!");
        System.out.println("Total steps: " + steps);
    }

    /**
     * Forces a link pathway to Philosophy
     *
     * @param current the Document to start with
     * @return the number of steps on the forced pathway
     * @throws IOException if something goes wrong
     */
    public static int forceToPhilosophy(Document current) throws IOException {
        return forceToPhilosophy(current, new HashSet<>(), 0);
    }

    /**
     * Forces a link pathway to Philosophy
     *
     * @param current the Document to start with
     * @param visited a HashSet of pages that have been already visited
     * @param hops the number of current hops already taken
     * @return the number of steps on the forced pathway
     * @throws IOException if something goes wrong
     */
    private static int forceToPhilosophy(Document current,
            HashSet<String> visited, int hops) throws IOException {
        /*Document current = Jsoup.parse(parse(Jsoup.connect(
                    "https://en.wikipedia.org/wiki/Special:Random")
                .get().outerHtml()));*/
        if (!current.title().equals("Philosophy - Wikipedia")) {
            String title = current.selectFirst("h1#firstHeading").text();
            // System.out.println(maxTitle);
            if (!visited.add(title)) {
                /*if(maxTitle.equals("Existence") || maxTitle.equals("Reality"))
                    return -2;*/
                return -hops - 1;
            }

            current.select("table").remove();
            current.select("sup").remove();

            Elements bodyStuff = current.select("div#bodyContent");
            Elements parags = bodyStuff.select("p");
            if (parags.select("a[href]").isEmpty()) {
                Elements listElements = bodyStuff.select("li");
                outer:
                for (Element listElement : listElements) {
                    String temp = listElement.outerHtml();
                    temp = temp.replace("<br>", "");
                    temp = temp.replace("</br>", "");
                    TreeMap<Integer, Integer> map = map(temp);
                    Document li = Jsoup.parse(temp);
                    Elements links = li.select("a[href]");
                    if (links.isEmpty()) {
                        continue;
                    }
                    for (Element link : links) {
                        if (link.parent().is("span#coordinates")) {
                            continue outer;
                        }
                        String outer = link.outerHtml();
                        if (map.get(map.floorKey(temp.indexOf(outer))) != 0) {
                            continue;
                        }
                        String linkHref = link.attr("href");
                        if (!linkHref.contains("#") && !linkHref.contains(":")
                                && !linkHref.contains("action=edit")
                                && !linkHref.contains("upload.wikimedia.org")) {
                            // try this one
                            int force = forceToPhilosophy(
                                    Jsoup.connect("https://en.wikipedia.org"
                                            + link.attr("href")).get(),
                                    new HashSet<>(visited), hops + 1);
                            if (force >= 0) {
                                return force;
                            }
                        }
                    }
                }
            } else {
                outer:
                for (Element parag : parags) {
                    String temp = parag.outerHtml();
                    temp = temp.replace("<br>", "");
                    temp = temp.replace("</br>", "");
                    TreeMap<Integer, Integer> map = map(temp);
                    Document p = Jsoup.parse(temp);
                    Elements links = p.select("a[href]");
                    if (links.isEmpty()) {
                        continue;
                    }
                    for (Element link : links) {
                        if (link.parent().is("span#coordinates")) {
                            continue outer;
                        }
                        String outer = link.outerHtml();
                        if (map.get(map.floorKey(temp.indexOf(outer))) != 0) {
                            continue;
                        }
                        String linkHref = link.attr("href");
                        if (!"".equals(linkHref)
                                && !linkHref.contains("#") && !linkHref.contains(":")
                                && !linkHref.contains("action=edit")
                                && !linkHref.contains("upload.wikimedia.org")) {
                            // try this one
                            int force = forceToPhilosophy(
                                    Jsoup.connect("https://en.wikipedia.org"
                                            + link.attr("href")).get(),
                                    new HashSet<>(visited), hops + 1);
                            if (force >= 0) {
                                return force;
                            }
                        }
                    }
                }
                Elements listElements = bodyStuff.select("li");
                outer:
                for (Element listElement : listElements) {
                    String temp = listElement.outerHtml();
                    temp = temp.replace("<br>", "");
                    temp = temp.replace("</br>", "");
                    TreeMap<Integer, Integer> map = map(temp);
                    Document li = Jsoup.parse(temp);
                    Elements links = li.select("a[href]");
                    if (links.isEmpty()) {
                        continue;
                    }
                    for (Element link : links) {
                        if (link.parent().is("span#coordinates")) {
                            continue outer;
                        }
                        String outer = link.outerHtml();
                        if (map.get(map.floorKey(temp.indexOf(outer))) != 0) {
                            continue;
                        }
                        String linkHref = link.attr("href");
                        if (!linkHref.contains("#") && !linkHref.contains(":")
                                && !linkHref.contains("action=edit")
                                && !linkHref.contains("upload.wikimedia.org")) {
                            // try this one
                            int force = forceToPhilosophy(
                                    Jsoup.connect("https://en.wikipedia.org"
                                            + link.attr("href")).get(),
                                    new HashSet<>(visited), hops + 1);
                            if (force >= 0) {
                                return force;
                            }
                        }
                    }
                }
            }
            return -hops - 1;
        }
        return hops;
    }

    /**
     * Parses a document
     *
     * @param document the String representation of the document
     * @return a parsed document
     * @deprecated don't use this
     */
    public static String parse(final String document) {
        String copy = document;
        TreeMap<Integer, Integer> parentheses = new TreeMap<>();
        parentheses.put(0, 0);
        int open = 0, close = 0, add = 0;
        while (open >= 0 || close >= 0) {
            open = copy.indexOf("(");
            close = copy.indexOf(")");
            if (open == -1) {
                if (close == -1) {
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
                if (close == -1) {
                    // only opening
                    // temp = open;
                    parentheses.put(add + open,
                            parentheses.get(parentheses.lastKey()) + 1);
                    open++;
                    add += open;
                    copy = copy.substring(open);
                } else {
                    // both
                    if (open < close) {
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
     *
     * @param document String thing
     * @return Map
     */
    public static TreeMap<Integer, Integer> map(final String document) {
        return map(document, "(", ")");
    }

    /**
     * Map the thing
     *
     * @param document String thing
     * @param openS the opening symbol
     * @param closeS the closing symbol
     * @return Map
     */
    public static TreeMap<Integer, Integer> map(final String document,
            final String openS, final String closeS) {
        String copy = document;
        TreeMap<Integer, Integer> parentheses = new TreeMap<>();
        parentheses.put(0, 0);
        int open, close, add = 0, in = 0;
        for (;;) {
            open = copy.indexOf(openS);
            close = copy.indexOf(closeS);
            if (open == -1) {
                if (close == -1) {
                    // nothing works
                    break;
                } else {
                    // only closing
                    // temp = close;
                    if (in > 0) {
                        parentheses.put(add + close,
                                --in);
                    }
                    close++;
                    add += close;
                    copy = copy.substring(close);
                }
            } else {
                if (close == -1) {
                    // only opening
                    // temp = open;
                    parentheses.put(add + open,
                            ++in);
                    open++;
                    add += open;
                    copy = copy.substring(open);
                } else {
                    // both
                    if (open < close) {
                        parentheses.put(add + open,
                                ++in);
                        open++;
                        add += open;
                        copy = copy.substring(open);
                    } else {
                        if (in > 0) {
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
