package wikiphil;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * A class that gives Documents from a Queue
 */
public class DocumentRequester implements Runnable {

    /**
     * A finished product
     */
    private Document product;

    /**
     * A request
     */
    private String request;

    /**
     * Ready for request?
     */
    private boolean valSet = false;

    /**
     * Stop?
     */
    private boolean stop = false;

    /**
     * AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA.
     */
    public DocumentRequester() {
        product = null;
        request = null;
    }

    /**
     * Requests a Document
     *
     * @param location the location of the document
     * @return the requested Document
     * @throws InterruptedException if something goes wrong
     */
    public synchronized Document request(String location)
            throws InterruptedException {
        // put
        while (valSet) {
            wait();
        }
        valSet = true;
        request = location;
        notify();
        wait();
        return product;
    }

    /**
     * Stops the {@code run()} method.
     */
    public void stop() {
        stop = true;
    }

    @Override
    public synchronized void run() {
        while (!stop) {
            if (!valSet) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    System.err.println("Interrupted.");
                }
            }
            try {
                product = Jsoup.connect(request).get();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (IllegalArgumentException iae) {
                System.out.println("IAE: " + request + " != valid");
            }
            valSet = false;
            notifyAll();
        }
    }
}
