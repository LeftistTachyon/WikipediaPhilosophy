
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionTest {
    public static void main(String[] args) {
        try {
            throw new Exception();
        } catch (Exception ex) {
            Logger.getLogger(ExceptionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}