package views;

import database.DatabaseConnectionHandler;
import database.DatabaseOperations;
import model.Address;
import model.PaymentMethod;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class AccountSettings extends JPanel {
    DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
    private final MainFrame mainFrame;
    private JPanel accountSettingsPanel;
    private JButton confirmButton;
    private JButton returnToShopButton;
    private JTextField cardNumberField;
    private JTextField expiryDateField;
    private JTextField cvcField;
    private JTextField emailField;
    private JTextField confirmEmailField;
    private JPasswordField oldPasswordField;
    private JPasswordField confirmNewPasswordField;
    private JPasswordField newPasswordField;
    private JTextField cardholderNameField;
    private JButton deleteAccountButton;
    private JTextField currentEmailField;
    private JTextField postcodeField;
    private JTextField houseNumberField;
    private JTextField streetField;
    private JTextField cityField;
    private JTextField countyField;
    private JTextField bankCardNameField;

    public AccountSettings(MainFrame mainFrame) throws SQLException  {
        this.mainFrame = mainFrame;
        initComponents();

        // Populate all fields
        if (getMainFrame().getCurrentUser() != null){
            currentEmailField.setEnabled(false);
            currentEmailField.setText(getMainFrame().getCurrentUser().getEmail());
            if (getMainFrame().getCurrentUser().getPaymentMethod() != null){
                populatePaymentFields();
            }

            if (getMainFrame().getCurrentUser().getAddress() != null) {
                populateAddressFields();
            }

            if (!getMainFrame().getCurrentUser().isCustomer()) {
                cardNumberField.setEnabled(false);
                cvcField.setEnabled(false);
                expiryDateField.setEnabled(false);
                cardholderNameField.setEnabled(false);
            }
        } else {
            return;
        }


        confirmButton.addActionListener(e -> {
            String newEmail = emailField.getText().strip();
            String confirmEmail = confirmEmailField.getText().strip();
            String oldPassword = new String(oldPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmNewPassword = new String (confirmNewPasswordField.getPassword());
            String cardNumberText = cardNumberField.getText().strip();
            String bankCardName = bankCardNameField.getText().strip();
            String cvcText = cvcField.getText().strip();
            String expiryDateText = expiryDateField.getText().strip();
            String cardholderName = cardholderNameField.getText().strip();
            String postcode = postcodeField.getText().strip();
            String houseNumberText = houseNumberField.getText().strip();
            String streetName = streetField.getText().strip();
            String city = cityField.getText().strip();
            String county = countyField.getText().strip();

            boolean newEmailEmpty = newEmail.isEmpty();
            boolean confirmEmailEmpty = confirmEmail.isEmpty();
            boolean oldPasswordEmpty = oldPassword.isEmpty();
            boolean newPasswordEmpty = newPassword.isEmpty();
            boolean confirmNewPasswordEmpty = confirmNewPassword.isEmpty();
            boolean cardNumberEmpty = cardNumberText.isEmpty();
            boolean cvcEmpty = cvcText.isEmpty();
            boolean expiryDateEmpty = expiryDateText.isEmpty();
            boolean cardholderNameEmpty = cardholderName.isEmpty();
            boolean bankCardNameEmpty = bankCardName.isEmpty();
            boolean postcodeEmpty = postcode.isEmpty();
            boolean houseNumberEmpty = houseNumberText.isEmpty();
            boolean streetNameEmpty = streetName.isEmpty();
            boolean cityEmpty = city.isEmpty();
            boolean countyEmpty = county.isEmpty();

            StringBuilder errorBuilder = new StringBuilder();

            // Remove all hyphens and whitespace from the card number
            String cleanedCardNumberText = cardNumberText.replaceAll("[-\\s]", "");
            String cardNumber = null;
            if (!cardNumberEmpty) {
                if (!cleanedCardNumberText.matches("^\\d{16}$")) {
                    errorBuilder.append("Card numbers must contain exactly 16 digits.\n");
                } else {
                    cardNumber = cleanedCardNumberText;
                }
            }

            if (!postcodeEmpty) {
                if (!Register.isValidPostcode(postcode)) {
                    errorBuilder.append("Please ensure you enter a valid UK postcode.\n");
                }
            }

            String cvc = null;
            if (!cvcEmpty) {
                if (cvcText.matches("\\d{3}")) {
                    cvc = cvcText;
                } else {
                    errorBuilder.append("CVC must only contain 3 digits in the form [XXX]\n");
                }
            }

            Integer houseNumber = null;
            if (!houseNumberEmpty) {
                try {
                    int parsedHouseNumber = Integer.parseInt(houseNumberText);
                    if (parsedHouseNumber > 0) {
                        houseNumber = parsedHouseNumber;
                    }

                    if (houseNumber == null) {
                        throw new NumberFormatException();
                    }

                } catch (NumberFormatException ex ) {
                    errorBuilder.append("House numbers must be positive integers\n");
                }
            }

            String expiryDate = null;
            if (!expiryDateEmpty) {
                if (!expiryDateText.matches("^(0[1-9]|1[0-2])/\\d{4}$")) {
                    errorBuilder.append("The entered expiry date must be in the format [MM/YYYY]\n");
                } else {
                    expiryDate = expiryDateText;
                }
            }

            if (!newEmail.equals(confirmEmail)) {
                errorBuilder.append("The entered emails do not match.\n");
            }

            if (newPassword.equals(confirmNewPassword)) {
                if (oldPassword.equals(newPassword)) {
                    // Set XpasswordEmpty to true; password doesn't need to be updated
                    oldPasswordEmpty = true;
                    newPasswordEmpty = true;
                    confirmNewPasswordEmpty = true;
                }
            } else {
//                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "The entered passwords do not match.");
                errorBuilder.append("The entered passwords (New Password, Confirm Password) do not match.\n");
            }

            int emptyEmailFields = 2;
            int emptyPasswordFields = 3;
            int emptyPaymentMethodFields = 5;
            int emptyAddressFields = 5;

            if (!newEmailEmpty) {emptyEmailFields--;}
            if (!confirmEmailEmpty) {emptyEmailFields--;}

            if (!oldPasswordEmpty) {emptyPasswordFields--;}
            if (!newPasswordEmpty) {emptyPasswordFields--;}
            if (!confirmNewPasswordEmpty) {emptyPasswordFields--;}

            if (!cardNumberEmpty) {emptyPaymentMethodFields--;}
            if (!cvcEmpty) {emptyPaymentMethodFields--;}
            if (!expiryDateEmpty) {emptyPaymentMethodFields--;}
            if (!cardholderNameEmpty) {emptyPaymentMethodFields--;}
            if (!bankCardNameEmpty) {emptyPaymentMethodFields--;}

            if (!postcodeEmpty) {emptyAddressFields--;}
            if (!houseNumberEmpty) {emptyAddressFields--;}
            if (!streetNameEmpty) {emptyAddressFields--;}
            if (!cityEmpty) {emptyAddressFields--;}
            if (!countyEmpty) {emptyAddressFields--;}

            if (emptyEmailFields == 1 ||
                emptyPasswordFields > 0 && emptyPasswordFields < 3 ||
                emptyPaymentMethodFields > 0 && emptyPaymentMethodFields < 5 ||
                emptyAddressFields > 0 && emptyAddressFields < 5) {
                errorBuilder.append("Please ensure that for each section, you fill in all the fields or leave them all blank. Partially completed sections are not allowed.");
            }

            if (errorBuilder.length() > 0) {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), errorBuilder.toString());
                return;
            }

            StringBuilder stringBuilder = new StringBuilder();

            if (emptyEmailFields == 0 || emptyPasswordFields == 0 || emptyPaymentMethodFields == 0 || emptyAddressFields == 0) {
                try {
                    getHandler().openConnection();
                    DatabaseOperations operations = new DatabaseOperations();

                    if (emptyEmailFields == 0) {
                        if (!Register.isValidEmail(newEmail) && !Register.isValidEmail(confirmEmail)) {
                            stringBuilder.append("The entered emails are not valid.");
                        } else {
                            if (operations.changeUserEmail(getMainFrame().getCurrentUser().getUserID(), newEmail, getHandler().getConnection())) {
                                getMainFrame().getCurrentUser().setEmail(newEmail);
                                stringBuilder.append("Your email has been updated successfully.\n");
                            } else {
                                stringBuilder.append("A user already exists with the entered email.\n");
                            }
                        }
                    }

                    if (emptyPasswordFields == 0) {
                        if (operations.changeUserPassword(getMainFrame().getCurrentUser(), oldPassword, newPassword, getHandler().getConnection())) {
                            stringBuilder.append("Your password has been updated successfully.\n");
                        } else {
                            stringBuilder.append("Your old password was entered incorrectly. Please try again.\n");
                        }
                    }

                    if (emptyPaymentMethodFields == 0) {
                        String cardNumberEncrypted = MainFrame.encryptText(cardNumber);
                        String cvcEncrypted = MainFrame.encryptText(cvc);
                        String expiryEncrypted = MainFrame.encryptText(expiryDateText);
                        String cardholderEncrypted = MainFrame.encryptText(cardholderName);
                        String bankCardNameEncrypted = MainFrame.encryptText(bankCardName);

                        PaymentMethod paymentMethod = new PaymentMethod(cardNumberEncrypted, cvcEncrypted, expiryEncrypted, cardholderEncrypted, bankCardNameEncrypted);
                        if (operations.updateCardDetails(getMainFrame().getCurrentUser().getUserID(), paymentMethod, getHandler().getConnection())) {

                            getMainFrame().getCurrentUser().setPaymentMethod(paymentMethod);
                            stringBuilder.append("Your payment method has been updated successfully.\n");
                        } else {
                            stringBuilder.append("An error occurred while updating your payment method.\n");
                        }
                    }

                    if (emptyAddressFields == 0) {
                        Address address = new Address(postcode, houseNumber, streetName, city, county);
                        if (operations.updateAddressDetails(getMainFrame().getCurrentUser().getUserID(), getMainFrame().getCurrentUser().getAddress(), address, getHandler().getConnection())) {
                            getMainFrame().getCurrentUser().setAddress(address);
                            stringBuilder.append("Your address details has been updated successfully.\n");
                        } else {
                            stringBuilder.append("An error occurred while updating your address details.\n");
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if (getHandler() != null) {
                        getHandler().closeConnection();
                    }
                }
            }

            try {
                getMainFrame().gotoPage("AccountSettings", new AccountSettings(getMainFrame()).getAccountSettingsPanel());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            if (stringBuilder.length() > 0) {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), stringBuilder.toString());
            } else {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Your details have not been changed.");
            }
        });
        returnToShopButton.addActionListener(e -> {
            getMainFrame().gotoPage("Shop", new Shop(getMainFrame()).getShopPanel());
        });

        deleteAccountButton.addActionListener(e -> {
            String password = JOptionPane.showInputDialog(JOptionPane.getRootFrame(), "Enter your password to confirm account deletion:");

            if (password != null && !password.isEmpty() && getMainFrame().getCurrentUser().passwordsMatch(password)) {
                int response = JOptionPane.showConfirmDialog(
                        JOptionPane.getRootFrame(),
                        "Do you really want to delete your account?",
                        "Confirm Account Deletion",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (response == JOptionPane.OK_OPTION) {
                    try {
                        getHandler().openConnection();
                        DatabaseOperations operations = new DatabaseOperations();

                        if (operations.deleteAccount(getMainFrame().getCurrentUser().getUserID(), getHandler().getConnection())) {
                            getMainFrame().setCurrentUser(null);
                            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Account successfully deleted.");
                            getMainFrame().gotoPage("Login", new Login(getMainFrame()).getLoginPanel());
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    } finally {
                        if (getHandler() != null) {
                            getHandler().closeConnection();
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Incorrect password.");
            }
        });
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public JPanel getAccountSettingsPanel() {
        return accountSettingsPanel;
    }

    public DatabaseConnectionHandler getHandler() {
        return handler;
    }

    private void populatePaymentFields() {
        String cardNumber = getMainFrame().getCurrentUser().getPaymentMethod().getCardNumber();
        String cvc = getMainFrame().getCurrentUser().getPaymentMethod().getCVC();
        String expiry = getMainFrame().getCurrentUser().getPaymentMethod().getExpiryDate();
        String cardholder = getMainFrame().getCurrentUser().getPaymentMethod().getCardHolderName();
        String bankCardName = getMainFrame().getCurrentUser().getPaymentMethod().getBankCardName();

        SecretKey key = MainFrame.getEncryptionKey();
        assert key != null;

        try {
            String cardNumberDecrypted = MainFrame.decryptText(cardNumber);
            String cvcDecrypted = MainFrame.decryptText(cvc);
            String expiryDecrypted = MainFrame.decryptText(expiry);
            String cardholderDecrypted = MainFrame.decryptText(cardholder);
            String bankCardNameDecrypted = MainFrame.decryptText(bankCardName);

            cardNumberField.setText(cardNumberDecrypted);
            cvcField.setText(cvcDecrypted);
            expiryDateField.setText(expiryDecrypted);
            cardholderNameField.setText(cardholderDecrypted);
            bankCardNameField.setText(bankCardNameDecrypted);
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    private void populateAddressFields() {
        postcodeField.setText(getMainFrame().getCurrentUser().getAddress().getPostcode());
        houseNumberField.setText(String.valueOf(getMainFrame().getCurrentUser().getAddress().getHouseNumber()));
        streetField.setText(getMainFrame().getCurrentUser().getAddress().getRoadName());
        cityField.setText(getMainFrame().getCurrentUser().getAddress().getCityName());
        countyField.setText(getMainFrame().getCurrentUser().getAddress().getCounty());
    }

    private void initComponents() {
        // Main panel
        accountSettingsPanel = new JPanel(new GridBagLayout());
        accountSettingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title
        JLabel titleLabel = new JLabel("Account Settings");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 7;
        gbc.anchor = GridBagConstraints.CENTER;
        accountSettingsPanel.add(titleLabel, gbc);

        // Email section
        JLabel emailSectionLabel = new JLabel("Email");
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        accountSettingsPanel.add(emailSectionLabel, gbc);

        JLabel currentEmailLabel = new JLabel("Current Email");
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        accountSettingsPanel.add(currentEmailLabel, gbc);

        currentEmailField = new JTextField();
        currentEmailField.setPreferredSize(new Dimension(150, currentEmailField.getPreferredSize().height));
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        accountSettingsPanel.add(currentEmailField, gbc);

        JLabel newEmailLabel = new JLabel("New Email");
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        accountSettingsPanel.add(newEmailLabel, gbc);

        emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(150, emailField.getPreferredSize().height));
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        accountSettingsPanel.add(emailField, gbc);

        JLabel confirmEmailLabel = new JLabel("Confirm Email");
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        accountSettingsPanel.add(confirmEmailLabel, gbc);

        confirmEmailField = new JTextField();
        confirmEmailField.setPreferredSize(new Dimension(150, confirmEmailField.getPreferredSize().height));
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        accountSettingsPanel.add(confirmEmailField, gbc);

        // Password section
        JLabel passwordSectionLabel = new JLabel("Password");
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        accountSettingsPanel.add(passwordSectionLabel, gbc);

        JLabel oldPasswordLabel = new JLabel("Old Password");
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        accountSettingsPanel.add(oldPasswordLabel, gbc);

        oldPasswordField = new JPasswordField();
        oldPasswordField.setPreferredSize(new Dimension(150, oldPasswordField.getPreferredSize().height));
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        accountSettingsPanel.add(oldPasswordField, gbc);

        JLabel newPasswordLabel = new JLabel("New Password");
        gbc.gridx = 1;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        accountSettingsPanel.add(newPasswordLabel, gbc);

        newPasswordField = new JPasswordField();
        newPasswordField.setPreferredSize(new Dimension(150, newPasswordField.getPreferredSize().height));
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        accountSettingsPanel.add(newPasswordField, gbc);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password");
        gbc.gridx = 1;
        gbc.gridy = 11;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        accountSettingsPanel.add(confirmPasswordLabel, gbc);

        confirmNewPasswordField = new JPasswordField();
        confirmNewPasswordField.setPreferredSize(new Dimension(150, confirmNewPasswordField.getPreferredSize().height));
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        accountSettingsPanel.add(confirmNewPasswordField, gbc);

        // Bank details section
        JLabel bankDetailsLabel = new JLabel("Bank Details");
        gbc.gridx = 4;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        accountSettingsPanel.add(bankDetailsLabel, gbc);

        JLabel cardNumberLabel = new JLabel("Card Number");
        gbc.gridx = 4;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        accountSettingsPanel.add(cardNumberLabel, gbc);

        cardNumberField = new JTextField();
        cardNumberField.setPreferredSize(new Dimension(150, cardNumberField.getPreferredSize().height));
        gbc.gridx = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        accountSettingsPanel.add(cardNumberField, gbc);

        JLabel expiryLabel = new JLabel("Expiry Date");
        gbc.gridx = 4;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        accountSettingsPanel.add(expiryLabel, gbc);

        expiryDateField = new JTextField();
        expiryDateField.setPreferredSize(new Dimension(150, expiryDateField.getPreferredSize().height));
        gbc.gridx = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        accountSettingsPanel.add(expiryDateField, gbc);

        JLabel cvcLabel = new JLabel("CVC");
        gbc.gridx = 4;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        accountSettingsPanel.add(cvcLabel, gbc);

        cvcField = new JTextField();
        cvcField.setPreferredSize(new Dimension(150, cvcField.getPreferredSize().height));
        gbc.gridx = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        accountSettingsPanel.add(cvcField, gbc);

        JLabel cardholderLabel = new JLabel("Cardholder Name");
        gbc.gridx = 4;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        accountSettingsPanel.add(cardholderLabel, gbc);

        cardholderNameField = new JTextField();
        cardholderNameField.setPreferredSize(new Dimension(150, cardholderNameField.getPreferredSize().height));
        gbc.gridx = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        accountSettingsPanel.add(cardholderNameField, gbc);

        JLabel bankCardNameLabel = new JLabel("Bank Card Name");
        gbc.gridx = 4;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        accountSettingsPanel.add(bankCardNameLabel, gbc);

        bankCardNameField = new JTextField();
        bankCardNameField.setPreferredSize(new Dimension(150, bankCardNameField.getPreferredSize().height));
        gbc.gridx = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        accountSettingsPanel.add(bankCardNameField, gbc);

        // Address section
        JLabel addressLabel = new JLabel("Address");
        gbc.gridx = 4;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        accountSettingsPanel.add(addressLabel, gbc);

        JLabel postcodeLabel = new JLabel("Postcode");
        gbc.gridx = 4;
        gbc.gridy = 10;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        accountSettingsPanel.add(postcodeLabel, gbc);

        postcodeField = new JTextField();
        postcodeField.setPreferredSize(new Dimension(150, postcodeField.getPreferredSize().height));
        gbc.gridx = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        accountSettingsPanel.add(postcodeField, gbc);

        JLabel houseNumberLabel = new JLabel("House Number");
        gbc.gridx = 4;
        gbc.gridy = 11;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        accountSettingsPanel.add(houseNumberLabel, gbc);

        houseNumberField = new JTextField();
        houseNumberField.setPreferredSize(new Dimension(150, houseNumberField.getPreferredSize().height));
        gbc.gridx = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        accountSettingsPanel.add(houseNumberField, gbc);

        JLabel streetLabel = new JLabel("Street");
        gbc.gridx = 4;
        gbc.gridy = 12;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        accountSettingsPanel.add(streetLabel, gbc);

        streetField = new JTextField();
        streetField.setPreferredSize(new Dimension(150, streetField.getPreferredSize().height));
        gbc.gridx = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        accountSettingsPanel.add(streetField, gbc);

        JLabel cityLabel = new JLabel("City");
        gbc.gridx = 4;
        gbc.gridy = 13;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        accountSettingsPanel.add(cityLabel, gbc);

        cityField = new JTextField();
        cityField.setPreferredSize(new Dimension(150, cityField.getPreferredSize().height));
        gbc.gridx = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        accountSettingsPanel.add(cityField, gbc);

        JLabel countyLabel = new JLabel("County");
        gbc.gridx = 4;
        gbc.gridy = 14;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        accountSettingsPanel.add(countyLabel, gbc);

        countyField = new JTextField();
        countyField.setPreferredSize(new Dimension(150, countyField.getPreferredSize().height));
        gbc.gridx = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        accountSettingsPanel.add(countyField, gbc);

        // Buttons
        confirmButton = new JButton("Confirm Changes");
        gbc.gridx = 2;
        gbc.gridy = 15;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        accountSettingsPanel.add(confirmButton, gbc);

        returnToShopButton = new JButton("Return to Shop");
        gbc.gridx = 2;
        gbc.gridy = 16;
        accountSettingsPanel.add(returnToShopButton, gbc);

        deleteAccountButton = new JButton("Delete Account");
        gbc.gridx = 4;
        gbc.gridy = 16;
        accountSettingsPanel.add(deleteAccountButton, gbc);
    }
}
