package views;

import database.DatabaseConnectionHandler;
import database.DatabaseOperations;
import model.Order;
import model.Role;
import model.User;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Login extends JPanel {
    DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
    private JPanel loginPanel;
    private JPanel entryPanel;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JButton loginButton;
    private JButton registerButton;
    private String email = "";
    private String password = null;
    private final MainFrame mainFrame;

    public Login(MainFrame mainFrame) throws SQLException {
        this.mainFrame = mainFrame;
        initComponents();
        this.entryPanel.setBorder(null);
        loginButton.addActionListener(e -> {

            email = emailField.getText();
            password = new String(passwordField.getPassword());

            boolean emailEmpty = email.isEmpty();
            boolean passwordEmpty = password.isEmpty();

            if (emailEmpty || passwordEmpty) {
                String output = "The following fields cannot be empty: \n\n";
                if (emailEmpty) {
                    output += "Email\n";
                }
                if (passwordEmpty) {
                    output += "Password\n";
                }
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), output);
                return;
            }

            if (!Register.isValidEmail(email)) {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "The entered email is invalid.");
                return;
            }

            try {
                getHandler().openConnection();
                DatabaseOperations operations = new DatabaseOperations();
                if (operations.signIn(email, password, getHandler().getConnection())) {
                    User signedInUser = operations.getUserByEmail(email, getHandler().getConnection());
                    getMainFrame().setCurrentUser(signedInUser);

                    byte[] salt = operations.getEncryptionSalt(signedInUser.getUserID(), getHandler().getConnection());
                    char[] plainTextPasswordCharArray = password.toCharArray();
                    byte[] hashedPassword = MainFrame.hashPassword(plainTextPasswordCharArray, salt, 10000, 256);
                    SecretKey aesKey = new SecretKeySpec(hashedPassword, 0, 32, "AES");
                    getMainFrame().setEncryptionKey(aesKey);

                    for (Role role : getMainFrame().getCurrentUser().getUserRoles()) {
                        switch (role.getRoleName()) {
                            case "CUSTOMER" -> getMainFrame().getCurrentUser().setCustomer(true);
                            case "STAFF" -> getMainFrame().getCurrentUser().setStaff(true);
                            case "MANAGER" -> getMainFrame().getCurrentUser().setManager(true);
                        }
                    }

                    getMainFrame().setPendingOrder(
                            operations.getPendingOrderByUserID(signedInUser.getUserID(), handler.getConnection()));

                    // Order still null -> no pending order -> create pending order for the user
                    if (getMainFrame().getPendingOrder() == null) {
                        Date orderDate = new Date();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String dateString = dateFormat.format(orderDate);
                        getMainFrame().setPendingOrder(new Order(signedInUser.getUserID(), dateString, 0,
                                Order.OrderStatus.PENDING, new ArrayList<>()));
                        getMainFrame().getPendingOrder().setOrderNumber(operations
                                .createPendingOrder(getMainFrame().getPendingOrder(), handler.getConnection()));
                    } else {
                        // not null -> a pending order exists
                        Order existingOrder = operations.getOrderByOrderID(
                                getMainFrame().getPendingOrder().getOrderNumber(),
                                getMainFrame().getProducts(),
                                handler.getConnection());

                        if (existingOrder != null) {
                            // A pending order with orderLines exists -> set pending order as that
                            getMainFrame().setPendingOrder(existingOrder);
                            getMainFrame().getPendingOrder().setTotalCost();
                        }
                    }

                    if (getMainFrame().getCurrentUser().isStaff() || getMainFrame().getCurrentUser().isManager()) {
                        getMainFrame().gotoPage("InventoryManagement",
                                new InventoryManagement(getMainFrame()).getInventoryManagementPanel());
                    } else {
                        getMainFrame().gotoPage("Shop", new Shop(getMainFrame()).getShopPanel());
                    }

                    emailField.setText("");
                    passwordField.setText("");
                } else {
                    JOptionPane.showMessageDialog(mainFrame.getRootPane(), "Incorrect email or password.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (getHandler() != null) {
                    getHandler().closeConnection();
                }
            }
        });

        registerButton.addActionListener(e -> {
            try {
                getMainFrame().gotoPage("Register", new Register(getMainFrame()).getRegisterPanel());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    public JPanel getLoginPanel() {
        return loginPanel;
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public DatabaseConnectionHandler getHandler() {
        return handler;
    }

    private void initComponents() {
        // Main login panel
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title panel with "Sign In" label
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("Sign In");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.PLAIN, 16));
        titlePanel.add(titleLabel);

        // Entry panel with email and password fields
        entryPanel = new JPanel(new GridBagLayout());
        GridBagConstraints entryGbc = new GridBagConstraints();
        entryGbc.insets = new Insets(5, 5, 5, 5);

        JLabel emailLabel = new JLabel("Email");
        entryGbc.gridx = 0;
        entryGbc.gridy = 0;
        entryGbc.anchor = GridBagConstraints.EAST;
        entryPanel.add(emailLabel, entryGbc);

        emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(150, emailField.getPreferredSize().height));
        entryGbc.gridx = 1;
        entryGbc.gridy = 0;
        entryGbc.anchor = GridBagConstraints.WEST;
        entryGbc.fill = GridBagConstraints.HORIZONTAL;
        entryPanel.add(emailField, entryGbc);

        JLabel passwordLabel = new JLabel("Password");
        entryGbc.gridx = 0;
        entryGbc.gridy = 1;
        entryGbc.anchor = GridBagConstraints.EAST;
        entryGbc.fill = GridBagConstraints.NONE;
        entryPanel.add(passwordLabel, entryGbc);

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(150, passwordField.getPreferredSize().height));
        entryGbc.gridx = 1;
        entryGbc.gridy = 1;
        entryGbc.anchor = GridBagConstraints.WEST;
        entryGbc.fill = GridBagConstraints.HORIZONTAL;
        entryPanel.add(passwordField, entryGbc);

        // Login button
        loginButton = new JButton("Login");

        // Register button
        registerButton = new JButton("Register");

        // Layout the main panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        loginPanel.add(Box.createVerticalGlue(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        loginPanel.add(titlePanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(entryPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        loginPanel.add(loginButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        loginPanel.add(registerButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        loginPanel.add(Box.createVerticalGlue(), gbc);
    }
}
