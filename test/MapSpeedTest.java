
import wikiphil.Main;

public class MapSpeedTest {
    public static void main(String[] args) {
        double start = System.nanoTime();
        System.out.println(Main.map("<p>Jean Claude Balthazar Victor de Chantelauze was (born in <a href=\"/wiki/Montbrison,_Loire\" title=\"Montbrison, Loire\">Montbrison, Loire</a>, on 10 November 1787.\n" +
"He became known after the first <a href=\"/wiki/Bourbon_Restoration\" title=\"Bourbon Restoration\">Bourbon Restoration</a> in 1814 by a very liberal brochure on the draft (constitution) that the conservative senate had to submit to King <a href=\"/wiki/Louis_XVIII_of_France\" title=\"Louis XVIII of France\">Louis XVIII</a>. (He was then appointed deputy )prosecutor in Montbrison.\n" +
"He made a point of resigning during the <a href=\"/wiki/Hundred_Days\" title=\"Hundred Days\">Hundred Days</a> when <a href=\"/wiki/Napoleon\" title=\"Napoleon\">Napoleon</a> returned from exile.\n" +
"After the second Restoration this earned him the position of Advocate-General at the court) of <a href=\"/wiki/Lyon\" title=\"Lyon\">Lyon</a> on 25 October 1815. He was awarded the cross of the <a href=\"/wiki/Legion_of_Honor\" class=\"mw-redirect\" title=\"Legion of Honor\">Legion of Honor</a> in 1821. He was appointed Attorney General first at the court of <a href=\"/wiki/Douai\" title=\"Douai\">Douai</a> on 21 July 1826, and three months later at the court of <a href=\"/wiki/Riom\" title=\"Riom\">Riom</a>.<sup id=\"cite_ref-FOOTNOTERobertCougny189142_1-0\" class=\"reference\"><a href=\"#cite_note-FOOTNOTERobertCougny189142-1\">&#91;1&#93;</a></sup>\n" +
"</p>"));
        double total = System.nanoTime() - start;
        System.out.printf("%.4fms%n", total/1000000);
    }
}