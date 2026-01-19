import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.security.SecureRandom;
import java.util.Base64;
import views.MainFrame;

// open a connection to the server
// execute a query/update (one or more)
// iterate over the results of a query
// release server resources - otherwise will crash!

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
    /*
    TODO
        -Clean up codebase (remove redundant comments, create functions for repeated code)
        -Testing
     */
}