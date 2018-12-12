
import org.jsoup.nodes.Document;
import wikiphil.Main.DocumentRequester;

public class DocumentRequesterTest {
    public static void main(String[] args) {
        // final int cores = Runtime.getRuntime().availableProcessors();
        DocumentRequester dr = new DocumentRequester();
        new Thread(dr).start();
        for (int i = 0; i < 2; i++) {
            final int ii = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 10; j++) {
                        final int jj = j;
                        try {
                            Document d = dr
                                    .request("https://en.wikipedia.org/wiki/Special:Random");
                            System.out.println((ii*10+jj) + ": " + d.location());
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }
}