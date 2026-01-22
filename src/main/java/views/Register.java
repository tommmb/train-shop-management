package views;

import database.DatabaseConnectionHandler;
import database.DatabaseOperations;
import model.Address;

import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends JPanel {
    DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
    private JPanel registerPanel;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField forenameField;
    private JTextField surnameField;
    private JTextField postcodeField;
    private JTextField houseNumberField;
    private JTextField streetField;
    private JTextField cityField;
    private JTextField countyField;
    private JButton registerButton;
    private JButton returnToLoginButton;
    private final MainFrame mainFrame;


    public Register(MainFrame mainFrame) throws SQLException {
        this.mainFrame = mainFrame;
        initComponents();
        this.setLayout(new BorderLayout());
        this.add(registerPanel, BorderLayout.CENTER);

        registerButton.addActionListener(e -> {

            String email = emailField.getText().strip();
            String password = new String(passwordField.getPassword());
            String firstName = forenameField.getText().strip();
            String lastName = surnameField.getText().strip();
            String postcode = postcodeField.getText().strip();
            String houseNumberText = houseNumberField.getText().strip();

            boolean houseNumberEmpty = houseNumberText.isEmpty();
            boolean houseNumberNotInt = false;
            String houseNumber = null;
            try {
                houseNumber = houseNumberText;
            } catch (NumberFormatException ex) {
                houseNumberNotInt = true;
            }

            String street = streetField.getText().strip();
            String city = cityField.getText().strip();
            String county = countyField.getText().strip();

            boolean emailEmpty = email.isEmpty();
            boolean passwordEmpty = password.length() == 0;
            boolean firstNameEmpty = firstName.isEmpty();
            boolean lastNameEmpty = lastName.isEmpty();
            boolean postcodeEmpty = postcode.isEmpty();
            boolean streetEmpty = street.isEmpty();
            boolean cityEmpty = city.isEmpty();
            boolean countyEmpty = county.isEmpty();

            ArrayList<String> emptyFields = new ArrayList<>();
            if (emailEmpty) {
                emptyFields.add("Email");
            }
            if (passwordEmpty) {
                emptyFields.add("Password");
            }
            if (firstNameEmpty) {
                emptyFields.add("First Name");
            }
            if (lastNameEmpty) {
                emptyFields.add("Last Name");
            }
            if (postcodeEmpty) {
                emptyFields.add("Postcode");
            }
            if (houseNumberEmpty) {
                emptyFields.add("House Number");
            }
            if (streetEmpty) {
                emptyFields.add("Street");
            }
            if (cityEmpty) {
                emptyFields.add("City");
            }
            if (countyEmpty) {
                emptyFields.add("County");
            }


            if (emptyFields.size() > 0) {
                StringBuilder message = new StringBuilder();
                message.append("The following fields cannot be empty: \n\n");
                for (String field : emptyFields) {
                    message.append(field).append("\n");
                }
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), message.toString());
                return;
            }

            if (!isValidPostcode(postcode) || !isValidEmail(email)) {
                String output = "The following fields are invalid\n\n";
                if (!isValidPostcode(postcode)) {
                    output += "Postcode\n";
                }
                if (!isValidEmail(email)){
                    output += "Email\n";
                }
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), output);
                return;
            }

            try {
                if ((houseNumber == null) || houseNumberNotInt || (Integer.parseInt(houseNumber) <= 0)) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "House number must be a positive integer.");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "House number must be a positive integer.");
                return;
            }

            try {
                getHandler().openConnection();
                DatabaseOperations operations = new DatabaseOperations();
                Address address = new Address(postcode, Integer.parseInt(houseNumber), street, city, county);

                //Hash password
                SecureRandom random = new SecureRandom();
                byte[] salt = new byte[16];
                random.nextBytes(salt);

                String saltDB = HexFormat.of().formatHex(salt);

                try {
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(saltDB.getBytes());
                    byte[] hash = md.digest(password.getBytes());
                    password = HexFormat.of().formatHex(hash);

                } catch (NoSuchAlgorithmException er){
                    er.printStackTrace();
                    return;
                }

                if (operations.register(firstName, lastName, email, password, saltDB, address, getHandler().getConnection())) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Registered successfully");

                    getMainFrame().gotoPage("Login", new Login(getMainFrame()).getLoginPanel());
                } else {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "A user already exists with the entered email.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                if (getHandler() != null) {
                    getHandler().closeConnection();
                }
            }


        });
        returnToLoginButton.addActionListener(e -> {
            try {
                getMainFrame().gotoPage("Login", new Login(getMainFrame()).getLoginPanel());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    public JPanel getRegisterPanel() {
        return registerPanel;
    }

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidPostcode(String postcode) {
        String postcodeRegex = "^(GIR ?0AA|((([A-Z]{1,2}[0-9]{1,2})|(([A-Z]{1,2}[0-9][A-HJKS-UW])|([A-Z]{1,2}[A-HK-Y][0-9]{1,2})|([A-Z][0-9][A-Z]))) ?[0-9][A-Z]{2}))$";

        Pattern pattern = Pattern.compile(postcodeRegex);
        Matcher matcher = pattern.matcher(postcode);
        return matcher.matches();
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public DatabaseConnectionHandler getHandler() {
        return handler;
    }

    private void initComponents() {
        // Main register panel with border layout
        registerPanel = new JPanel(new BorderLayout());

        // Center panel with form
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(BorderFactory.createLineBorder(new Color(0xDFDFDB)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title
        JLabel titleLabel = new JLabel("Register");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(titleLabel, gbc);

        // Spacers
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        centerPanel.add(Box.createHorizontalGlue(), gbc);

        gbc.gridx = 6;
        centerPanel.add(Box.createHorizontalGlue(), gbc);

        // Login credentials panel
        JPanel credentialsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints credGbc = new GridBagConstraints();
        credGbc.insets = new Insets(3, 5, 3, 5);

        JLabel credentialsLabel = new JLabel("Login Credentials");
        credGbc.gridx = 0;
        credGbc.gridy = 0;
        credGbc.gridwidth = 2;
        credGbc.anchor = GridBagConstraints.CENTER;
        credentialsPanel.add(credentialsLabel, credGbc);

        JLabel emailLabel = new JLabel("Email");
        credGbc.gridx = 0;
        credGbc.gridy = 1;
        credGbc.gridwidth = 1;
        credGbc.anchor = GridBagConstraints.EAST;
        credentialsPanel.add(emailLabel, credGbc);

        emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(150, emailField.getPreferredSize().height));
        credGbc.gridx = 1;
        credGbc.anchor = GridBagConstraints.WEST;
        credGbc.fill = GridBagConstraints.HORIZONTAL;
        credentialsPanel.add(emailField, credGbc);

        JLabel passwordLabel = new JLabel("Password");
        credGbc.gridx = 0;
        credGbc.gridy = 2;
        credGbc.anchor = GridBagConstraints.EAST;
        credGbc.fill = GridBagConstraints.NONE;
        credentialsPanel.add(passwordLabel, credGbc);

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(150, passwordField.getPreferredSize().height));
        credGbc.gridx = 1;
        credGbc.anchor = GridBagConstraints.WEST;
        credGbc.fill = GridBagConstraints.HORIZONTAL;
        credentialsPanel.add(passwordField, credGbc);

        JLabel personalInfoLabel = new JLabel("Personal Information");
        credGbc.gridx = 0;
        credGbc.gridy = 3;
        credGbc.gridwidth = 2;
        credGbc.anchor = GridBagConstraints.CENTER;
        credentialsPanel.add(personalInfoLabel, credGbc);

        JLabel forenameLabel = new JLabel("First Name");
        credGbc.gridx = 0;
        credGbc.gridy = 4;
        credGbc.gridwidth = 1;
        credGbc.anchor = GridBagConstraints.EAST;
        credentialsPanel.add(forenameLabel, credGbc);

        forenameField = new JTextField();
        forenameField.setPreferredSize(new Dimension(150, forenameField.getPreferredSize().height));
        credGbc.gridx = 1;
        credGbc.anchor = GridBagConstraints.WEST;
        credGbc.fill = GridBagConstraints.HORIZONTAL;
        credentialsPanel.add(forenameField, credGbc);

        JLabel surnameLabel = new JLabel("Last Name");
        credGbc.gridx = 0;
        credGbc.gridy = 5;
        credGbc.anchor = GridBagConstraints.EAST;
        credGbc.fill = GridBagConstraints.NONE;
        credentialsPanel.add(surnameLabel, credGbc);

        surnameField = new JTextField();
        surnameField.setPreferredSize(new Dimension(150, surnameField.getPreferredSize().height));
        credGbc.gridx = 1;
        credGbc.anchor = GridBagConstraints.WEST;
        credGbc.fill = GridBagConstraints.HORIZONTAL;
        credentialsPanel.add(surnameField, credGbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        centerPanel.add(credentialsPanel, gbc);

        // Address panel
        JPanel addressPanel = new JPanel(new GridBagLayout());
        GridBagConstraints addrGbc = new GridBagConstraints();
        addrGbc.insets = new Insets(3, 5, 3, 5);

        JLabel addressInfoLabel = new JLabel("Address Information");
        addrGbc.gridx = 0;
        addrGbc.gridy = 0;
        addrGbc.gridwidth = 2;
        addrGbc.anchor = GridBagConstraints.CENTER;
        addressPanel.add(addressInfoLabel, addrGbc);

        JLabel postcodeLabel = new JLabel("Postcode");
        addrGbc.gridx = 0;
        addrGbc.gridy = 1;
        addrGbc.gridwidth = 1;
        addrGbc.anchor = GridBagConstraints.EAST;
        addressPanel.add(postcodeLabel, addrGbc);

        postcodeField = new JTextField();
        postcodeField.setPreferredSize(new Dimension(150, postcodeField.getPreferredSize().height));
        addrGbc.gridx = 1;
        addrGbc.anchor = GridBagConstraints.WEST;
        addrGbc.fill = GridBagConstraints.HORIZONTAL;
        addressPanel.add(postcodeField, addrGbc);

        JLabel houseNumberLabel = new JLabel("House No.");
        addrGbc.gridx = 0;
        addrGbc.gridy = 2;
        addrGbc.anchor = GridBagConstraints.EAST;
        addrGbc.fill = GridBagConstraints.NONE;
        addressPanel.add(houseNumberLabel, addrGbc);

        houseNumberField = new JTextField();
        houseNumberField.setPreferredSize(new Dimension(150, houseNumberField.getPreferredSize().height));
        addrGbc.gridx = 1;
        addrGbc.anchor = GridBagConstraints.WEST;
        addrGbc.fill = GridBagConstraints.HORIZONTAL;
        addressPanel.add(houseNumberField, addrGbc);

        JLabel streetLabel = new JLabel("Street");
        addrGbc.gridx = 0;
        addrGbc.gridy = 3;
        addrGbc.anchor = GridBagConstraints.EAST;
        addrGbc.fill = GridBagConstraints.NONE;
        addressPanel.add(streetLabel, addrGbc);

        streetField = new JTextField();
        streetField.setPreferredSize(new Dimension(150, streetField.getPreferredSize().height));
        addrGbc.gridx = 1;
        addrGbc.anchor = GridBagConstraints.WEST;
        addrGbc.fill = GridBagConstraints.HORIZONTAL;
        addressPanel.add(streetField, addrGbc);

        JLabel cityLabel = new JLabel("City");
        addrGbc.gridx = 0;
        addrGbc.gridy = 4;
        addrGbc.anchor = GridBagConstraints.EAST;
        addrGbc.fill = GridBagConstraints.NONE;
        addressPanel.add(cityLabel, addrGbc);

        cityField = new JTextField();
        cityField.setPreferredSize(new Dimension(150, cityField.getPreferredSize().height));
        addrGbc.gridx = 1;
        addrGbc.anchor = GridBagConstraints.WEST;
        addrGbc.fill = GridBagConstraints.HORIZONTAL;
        addressPanel.add(cityField, addrGbc);

        JLabel countyLabel = new JLabel("County");
        addrGbc.gridx = 0;
        addrGbc.gridy = 5;
        addrGbc.anchor = GridBagConstraints.EAST;
        addrGbc.fill = GridBagConstraints.NONE;
        addressPanel.add(countyLabel, addrGbc);

        countyField = new JTextField();
        countyField.setPreferredSize(new Dimension(150, countyField.getPreferredSize().height));
        addrGbc.gridx = 1;
        addrGbc.anchor = GridBagConstraints.WEST;
        addrGbc.fill = GridBagConstraints.HORIZONTAL;
        addressPanel.add(countyField, addrGbc);

        gbc.gridx = 4;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        centerPanel.add(addressPanel, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints btnGbc = new GridBagConstraints();
        btnGbc.insets = new Insets(5, 5, 5, 5);

        registerButton = new JButton("Register");
        btnGbc.gridx = 1;
        btnGbc.gridy = 0;
        btnGbc.fill = GridBagConstraints.HORIZONTAL;
        buttonPanel.add(registerButton, btnGbc);

        JLabel alreadyHaveLabel = new JLabel("Already have an account?");
        btnGbc.gridx = 1;
        btnGbc.gridy = 1;
        btnGbc.anchor = GridBagConstraints.CENTER;
        btnGbc.fill = GridBagConstraints.NONE;
        buttonPanel.add(alreadyHaveLabel, btnGbc);

        returnToLoginButton = new JButton("Return to Login");
        btnGbc.gridx = 1;
        btnGbc.gridy = 2;
        btnGbc.fill = GridBagConstraints.HORIZONTAL;
        buttonPanel.add(returnToLoginButton, btnGbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 5;
        centerPanel.add(buttonPanel, gbc);

        registerPanel.add(centerPanel, BorderLayout.CENTER);
    }
}
