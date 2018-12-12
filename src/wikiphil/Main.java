package wikiphil;

import java.io.IOException;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    /**
     * # of pages to visit
     */
    private static final int PAGES_TO_VISIT = 100;

    /**
     * # of cores on this computer
     */
    private static final int CORES = Runtime.getRuntime().availableProcessors();

    /**
     * Keeps track of how many pages to go
     */
    private static int cntr = PAGES_TO_VISIT;

    /**
     * # of threads finished executing
     */
    private static int finished = 0;

    /**
     * A DocumentRequester for requesting documents
     */
    private static DocumentRequester dr;

    /**
     * All keeping track of statistics
     */
    private static double totalTime = 0;
    private static int max = -1, toPhil = 0;
    private static String title = null;

    /**
     * Updates statistics
     *
     * @param title the title of the page
     * @param hops the number of hops
     * @param millis the amount of milliseconds taken to get there
     */
    private static void newPage(String title, int hops, double millis) {
        if (hops > -1) {
            toPhil++;
        }
        if (hops > max) {
            Main.title = title;
            max = hops;
        }
        totalTime += millis;
    }

    private static double lastStart = 0;

    /**
     * The main method
     *
     * @param args the command line arguments
     * @throws java.io.IOException AAAAAAAAAAAAAAAAAAAA
     */
    public static void main(String[] args) throws IOException {
        dr = new DocumentRequester();
        new Thread(dr).start();

        int coresToUse = /*Math.max(CORES - 2, 1)*/ 4;
        System.out.println("Detected " + CORES + " cores; using "
                + (coresToUse + 1) + ".");

        lastStart = System.nanoTime();
        for (int i = 0; i < coresToUse; i++) {
            new Thread(() -> {
                while (cntr > 0) {
                    try {
                        Document tempD = dr.request(
                                "https://en.wikipedia.org/wiki/Special:Random");
                        int hops = hopsToPhilosophy(tempD);
                        double total = System.nanoTime() - lastStart;
                        total /= 1000000;
                        String title_ = tempD.selectFirst("h1#firstHeading").text();
                        if (cntr > 0) {
                            newPage(title_, hops, total);
                        } else {
                            break;
                        }
                        cntr--;
                        System.out.printf("%-100s", title_ + ": " + hops + " hops");
                        System.out.printf("%.3f ms%n", total);
                        lastStart = System.nanoTime();
                    } catch (InterruptedException | IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (++finished == coresToUse) {
                    printStatistics();
                    dr.stop();
                }
            }).start();
        }

        // the number of pages to try to get to philosophy from
        /*final int pagesToVisit = 100;
        double avgTime = 0;
        int max = -1, toPhil = 0;
        String title = null;
        for (int i = 0; i < pagesToVisit; i++) {
            double start = System.nanoTime();
            Document tempD = Jsoup.connect(
                    "https://en.wikipedia.org/wiki/Special:Random").get();
            int hops = hopsToPhilosophy(
                    tempD);
            if (hops >= 0) {
                toPhil++;
            }
            String temp = tempD.selectFirst("h1#firstHeading").text();
            if (hops > max) {
                max = hops;
                title = temp;
            }
            double total = System.nanoTime() - start;
            System.out.printf("%-100s", temp
                    + ": " + hops + " hops");
            System.out.printf("%.3f ms%n", total /= 1000000);
            avgTime += total;
        }
        System.out.printf("%nMax hops: %d hops - %s%n", max, title);
        System.out.printf("Avg time: %.3f ms%n", avgTime / pagesToVisit);
        System.out.printf("%% to philosophy: %.2f%%%n",
                ((double) toPhil * 100) / pagesToVisit);*/
        /*double start = System.nanoTime();
        traceToPhilosophy("https://en.wikipedia.org/wiki/1858_in_architecture");
        // also 2018–19 Hamburger SV season
        double total = System.nanoTime() - start;
        System.out.printf("%nTotal:\t%.3f ms%n", total / 1000000);*/
    }

    /**
     * Prints out the statistics.
     */
    private static void printStatistics() {
        System.out.printf("%nMax hops: %d hops - %s%n", max, title);
        System.out.printf("Avg time: %.3f ms%n", totalTime / PAGES_TO_VISIT);
        System.out.printf("%% to philosophy: %.2f%%%n",
                ((double) toPhil * 100) / PAGES_TO_VISIT);
    }

    /**
     * Loops through a single random page
     *
     * @param current the Document to start at
     * @return how many hops to Philosophy, -1 if it doesn't
     * @throws IOException if something goes wrong
     * @throws InterruptedException if something gets interrupted
     */
    public static int hopsToPhilosophy(Document current) throws IOException,
            InterruptedException {
        HashSet<String> sanity = new HashSet<>();
        /*Document current = Jsoup.parse(parse(Jsoup.connect(
                    "https://en.wikipedia.org/wiki/Special:Random")
                .get().outerHtml()));*/
        int output = 0;
        while (!current.title().equals("Philosophy - Wikipedia")) {
            String title = current.selectFirst("h1#firstHeading").text();
            // System.out.println(title);
            if (!sanity.add(title)) {
                /*if(title.equals("Existence") || title.equals("Reality"))
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
            current = dr.request("https://en.wikipedia.org" + toGo.attr("href"));
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
     * @throws InterruptedException if somethings is interrupted
     */
    public static int forceToPhilosophy(Document current) throws IOException,
            InterruptedException {
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
            HashSet<String> visited, int hops) throws IOException, InterruptedException {
        /*Document current = Jsoup.parse(parse(Jsoup.connect(
                    "https://en.wikipedia.org/wiki/Special:Random")
                .get().outerHtml()));*/
        if (!current.title().equals("Philosophy - Wikipedia")) {
            String title = current.selectFirst("h1#firstHeading").text();
            // System.out.println(title);
            if (!visited.add(title)) {
                /*if(title.equals("Existence") || title.equals("Reality"))
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
                                    dr.request("https://en.wikipedia.org"
                                            + link.attr("href")),
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
                                    dr.request("https://en.wikipedia.org"
                                            + link.attr("href")),
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
                                    dr.request("https://en.wikipedia.org"
                                            + link.attr("href")),
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
