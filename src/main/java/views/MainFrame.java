package views;
import database.DatabaseConnectionHandler;
import database.DatabaseOperations;
import model.*;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.*;
import java.awt.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel = new JPanel(cardLayout);
    private User currentUser = null;
    private JMenuBar menuBar;
    private JMenuItem currentUserLabel;
    private boolean viewOrderDetailsManagement = false;
    private boolean viewOrderQueueDetailsManagement = false;
    private boolean viewUserDetailsOrderQueue = false;
    private ArrayList<Product> products = new ArrayList<>();
    private java.util.List<Product> trainSetProducts = new ArrayList<>();
    private java.util.List<Product> trackPackProducts = new ArrayList<>();
    private java.util.List<Product> controllerProducts = new ArrayList<>();
    private java.util.List<Product> trackProducts = new ArrayList<>();
    private java.util.List<Product> locomotiveProducts = new ArrayList<>();
    private List<Product> rollingStockProducts = new ArrayList<>();
    private Product productToUpdate;
    private int newProductTypeNumber;
    private boolean returnToInventoryManagement = false;
    private Order pendingOrder = null;
    private static SecretKey encryptionKey = null;

    public MainFrame() {
        setTitle("Trains of Sheffield");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600); // Or pack(), if you prefer
        setLocationRelativeTo(null);

        menuBar = new JMenuBar();
        JMenuItem signOutButton = new JMenuItem("Sign Out");;
        currentUserLabel = new JMenuItem();
        currentUserLabel.setEnabled(false);

        signOutButton.addActionListener((e) -> {
            int response = JOptionPane.showConfirmDialog(
                    JOptionPane.getRootFrame(),
                    "Do you really wish to sign out?",
                    "Confirm Sign Out",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (response == JOptionPane.OK_OPTION) {
                if (getCurrentUser() != null) {
                    setCurrentUser(null);
                    setPendingOrder(null);
                    try {
                        Map<JPanel, String> jPanelStringHashMap = new HashMap<>();
                        String panelName = "Login";
                        JPanel panel = new Login(this).getLoginPanel();
                        jPanelStringHashMap.put(panel, panelName);
                        replaceJPanels(jPanelStringHashMap);
                        showCard(panelName);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        menuBar.add(signOutButton);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(currentUserLabel);

        DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
        try {
            handler.openConnection();

            if (this.products.size() == 0){
                setProducts(handler.getConnection());
            }

            JPanel panel = new Login(this).getLoginPanel();
            String panelName = "Login";
            cardPanel.add(panel, panelName);
            add(cardPanel);
            showCard("Login");
            setVisible(true);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            handler.closeConnection();
        }
    }

    public void showCard(String cardName) {
        cardLayout.show(cardPanel, cardName);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;

        updateMenuBar();

    }

    public JPanel getCardPanel() {
        return cardPanel;
    }

    public void updateMenuBar() {
        if (getCurrentUser() == null) {
            setJMenuBar(null);
        } else {
            getCurrentUserLabel().setText("Signed in as: " + getCurrentUser().getEmail());
            setJMenuBar(menuBar);
        }

        revalidate();
        repaint();
    }

    public JMenuItem getCurrentUserLabel() {
        return currentUserLabel;
    }

    public void replaceJPanels(Map<JPanel, String> jPanelMap) throws SQLException {
        getCardPanel().removeAll();

        for (Map.Entry<JPanel, String> jPanel : jPanelMap.entrySet()) {
            JPanel panel = jPanel.getKey();
            String panelName = jPanel.getValue();
            getCardPanel().add(panel, panelName);
        }

        getCardPanel().revalidate();
        getCardPanel().repaint();
    }

    public boolean isViewUserDetailsOrderQueue() {
        return viewUserDetailsOrderQueue;
    }

    public void setViewUserDetailsOrderQueue(boolean viewUserDetailsOrderQueue) {
        this.viewUserDetailsOrderQueue = viewUserDetailsOrderQueue;
    }

    public boolean isViewOrderDetailsManagement() {
        return viewOrderDetailsManagement;
    }

    public void setViewOrderDetailsManagement(boolean viewOrderDetailsManagement) {
        this.viewOrderDetailsManagement = viewOrderDetailsManagement;
    }

    public boolean isViewOrderQueueDetailsManagement() {
        return viewOrderQueueDetailsManagement;
    }

    public void setViewOrderQueueDetailsManagement(boolean viewOrderQueueDetailsManagement) {
        this.viewOrderQueueDetailsManagement = viewOrderQueueDetailsManagement;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public void setProducts(Connection connection) {
        DatabaseOperations operations = new DatabaseOperations();
        this.products.clear();
        this.trainSetProducts.clear();
        this.trackPackProducts.clear();
        this.controllerProducts.clear();
        this.trackProducts.clear();
        this.locomotiveProducts.clear();
        this.rollingStockProducts.clear();

        try {
            this.products = operations.getAllProducts(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (Product product : getProducts()) {
            try {
                if (product instanceof TrainSet p) {
                    trainSetProducts.add(p);
                }

                else if (product instanceof TrackPack p) {
                    trackPackProducts.add(p);
                }

                if (product instanceof Controller p) {
                    controllerProducts.add(p);
                }

                if (product instanceof TrackPiece p) {
                    trackProducts.add(p);
                }

                if (product instanceof Locomotive p) {
                    locomotiveProducts.add(p);
                }

                else if (product instanceof RollingStock p) {
                    rollingStockProducts.add(p);
                }
            } catch (ClassCastException ignored) {}
        }
    }

    public List<Product> getTrainSetProducts() {
        return trainSetProducts;
    }

    public List<Product> getTrackPackProducts() {
        return trackPackProducts;
    }

    public List<Product> getControllerProducts() {
        return controllerProducts;
    }

    public List<Product> getTrackProducts() {
        return trackProducts;
    }

    public List<Product> getLocomotiveProducts() {
        return locomotiveProducts;
    }

    public List<Product> getRollingStockProducts() {
        return rollingStockProducts;
    }

    public Product getProductToUpdate() {
        return productToUpdate;
    }

    public void setProductToUpdate(Product productToUpdate) {
        this.productToUpdate = productToUpdate;
    }

    public int getNewProductTypeNumber() {
        return newProductTypeNumber;
    }

    public void setNewProductTypeNumber(int newProductTypeNumber) {
        this.newProductTypeNumber = newProductTypeNumber;
    }

    public boolean isReturnToInventoryManagement() {
        return returnToInventoryManagement;
    }

    public void setReturnToInventoryManagement(boolean returnToInventoryManagement) {
        this.returnToInventoryManagement = returnToInventoryManagement;
    }

    public Order getPendingOrder() {
        return pendingOrder;
    }

    public void setPendingOrder(Order pendingOrder) {
        this.pendingOrder = pendingOrder;
    }

    public void gotoPage(String name, JPanel panel){
        DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
        try {
            handler.openConnection();
            setProducts(handler.getConnection());
            Map<JPanel, String> jPanelStringHashMap = new HashMap<>();
            jPanelStringHashMap.put(panel, name);
            this.replaceJPanels(jPanelStringHashMap);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.showCard(name);
    }

    public static SecretKey getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(SecretKey encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public static String encryptText(String plainText) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[cipher.getBlockSize()];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, getEncryptionKey(), ivSpec);

        byte[] encrypted = cipher.doFinal(plainText.getBytes());
        String ivBase64 = Base64.getEncoder().encodeToString(iv);
        String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);

        return ivBase64 + ":" + encryptedBase64;
    }

    public static String decryptText(String encryptedTextWithIv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String[] parts = encryptedTextWithIv.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException();
        }

        String ivBase64 = parts[0];
        String encryptedTextBase64 = parts[1];
        byte[] iv = Base64.getDecoder().decode(ivBase64);
        byte[] encryptedText = Base64.getDecoder().decode(encryptedTextBase64);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, getEncryptionKey(), ivSpec);

        byte[] decrypted = cipher.doFinal(encryptedText);
        return new String(decrypted);
    }

    public static byte[] hashPassword(char[] password, byte[] salt, int iterations, int keyLength) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return skf.generateSecret(spec).getEncoded();
    }


}