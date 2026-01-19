package views;

import database.DatabaseConnectionHandler;
import database.DatabaseOperations;
import model.*;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Checkout extends JPanel {
    DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
    private final MainFrame mainFrame;
    private final Order order;
    private JPanel checkoutPanel;
    private JPanel detailsMainPanel;
    private JButton confirmOrderButton;
    private JTextField cardholderNameField;
    private JTextField emailField;
    private JTextField cardNumberField;
    private JTextField cvcField;
    private JTextField expiryDateField;
    private JTextField postcodeField;
    private JTextField houseNumberField;
    private JTextField streetField;
    private JTextField cityField;
    private JTextField countyField;
    private JPanel orderMainPanel;
    private JButton returnToShopButton;
    private JScrollPane orderLineScrollPane;
    private JLabel totalPrice;
    private JPanel orderPanel;
    private JTextField bankCardNameField;

    public Checkout(MainFrame mainFrame, Order order) throws SQLException {
        this.mainFrame = mainFrame;
        this.order = order;

        //Populate details fields
        if (getMainFrame().getCurrentUser() != null){
            emailField.setEnabled(false);
            populateNameFields();
            if (getMainFrame().getCurrentUser().getPaymentMethod() != null){
                populatePaymentFields();
            }

            if (getMainFrame().getCurrentUser().getAddress() != null) {
                populateAddressFields();
            }
        }

        if (order.getOrderLineArrayList().size() != 0) {
            populateOrderLines();
        }

        returnToShopButton.addActionListener(e -> {
            getMainFrame().gotoPage("Shop", new Shop(getMainFrame()).getShopPanel());
        });

        confirmOrderButton.addActionListener(e -> {
            if (!fieldsValid()) {
                return;
//                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Invalid User Details");
            }
            try {
                if (confirmedOrder()) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Order confirmed");
                    Date orderDate = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String dateString = dateFormat.format(orderDate);
                    getMainFrame().setPendingOrder(new Order(getMainFrame().getCurrentUser().getUserID(), dateString, 0, Order.OrderStatus.PENDING, new ArrayList<>()));

                    handler.openConnection();
                    DatabaseOperations operations = new DatabaseOperations();
                    getMainFrame().getPendingOrder().setOrderNumber(operations.createPendingOrder(getMainFrame().getPendingOrder(), handler.getConnection()));

                    getMainFrame().gotoPage("Shop", new Shop(getMainFrame()).getShopPanel());
                } else {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Order could not be confirmed.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                handler.closeConnection();
            }
        });
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public Order getOrder() {
        return order;
    }

    public JPanel getCheckoutPanel() {
        return checkoutPanel;
    }

    public DatabaseConnectionHandler getHandler() {
        return handler;
    }

    private void populateNameFields() {
        emailField.setText(getMainFrame().getCurrentUser().getEmail());
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

    private void populateOrderLines() {
        this.orderPanel.setLayout(new BoxLayout(orderPanel, BoxLayout.Y_AXIS));

        for (OrderLine orderLine : getOrder().getOrderLineArrayList()) {
            JLabel label = new JLabel(String.format(
                    "%s [%s] | Quantity: %d | Price: £%.2f",
                    orderLine.getProduct().getProductName(),
                    orderLine.getProduct().getModellingScale().name(),
                    orderLine.getQuantity(),
                    orderLine.getLineCost()
            ));
            label.setAlignmentX(CENTER_ALIGNMENT);
            this.orderPanel.add(label);
        }

        this.totalPrice.setText("Order Subtotal: £" + String.format("%.2f", order.getTotalCost()));
        this.orderPanel.revalidate();
        this.orderPanel.repaint();
    }

    private boolean fieldsValid(){
        //TODO Check all details are valid
        PaymentMethod paymentMethod  = getMainFrame().getCurrentUser().getPaymentMethod();

        DatabaseOperations operations = new DatabaseOperations();
        boolean userNeedsPayment = false;

        try {
            handler.openConnection();
            ArrayList<Order> orderQueueOrders = operations.getAllOrdersInQueue(handler.getConnection());

            for (Order o : orderQueueOrders) {
                if (o.getOrderStatus().equals(Order.OrderStatus.PENDING)) {
                    continue;
                }
                if (o.getUserID() == getMainFrame().getCurrentUser().getUserID()) {
                    // User has an existing order
                    userNeedsPayment = true;
                    break;
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            handler.closeConnection();
        }

        StringBuilder error = new StringBuilder();
        StringBuilder errorBuilder = new StringBuilder();

        if (userNeedsPayment) {
            String cardNumberText = cardNumberField.getText().strip();
            String cvcText = cvcField.getText().strip();
            String expiryDateText = expiryDateField.getText().strip();
            String cardholderName = cardholderNameField.getText().strip();
            String bankCardName = bankCardNameField.getText().strip();

            boolean cardNumberEmpty = cardNumberText.isEmpty();
            boolean cvcEmpty = cvcText.isEmpty();
            boolean expiryDateEmpty = expiryDateText.isEmpty();
            boolean cardholderNameEmpty = cardholderName.isEmpty();
            boolean bankCardNameEmpty = bankCardName.isEmpty();

            if (cardNumberEmpty && cvcEmpty && expiryDateEmpty && cardholderNameEmpty) {
                error.append("Only your first placed order can be submitted without banking information\n");
            }
            if (cardNumberEmpty) {error.append("Card number cannot be empty\n");}
            if (cvcEmpty) {error.append("CVC cannot be empty\n");}
            if (expiryDateEmpty) {error.append("Expiry date cannot be empty\n");}
            if (cardholderNameEmpty) {error.append("Cardholder name cannot be empty\n");}
            if (bankCardNameEmpty) {error.append("Bank Card Name cannot be empty\n");}

            String expiryDate = null;
            if (!expiryDateEmpty) {
                if (!expiryDateText.matches("^(0[1-9]|1[0-2])/\\d{4}$")) {
                    errorBuilder.append("The entered expiry date must be in the format [MM/YYYY]\n");
                } else {
                    expiryDate = expiryDateText;
                }
            }

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

            String cvc = null;
            if (!cvcEmpty) {
                if (cvcText.matches("\\d{3}")) {
                    cvc = cvcText;
                } else {
                    errorBuilder.append("CVC must only contain 3 digits in the form [XXX]\n");
                }
            }
        }


        String email = emailField.getText().strip();
        String postcode = postcodeField.getText().strip();
        String houseNumberText = houseNumberField.getText().strip();
        String streetName = streetField.getText().strip();
        String city = cityField.getText().strip();
        String county = countyField.getText().strip();

        // Check if fields are empty
        boolean emailEmpty = email.isEmpty();
        boolean postcodeEmpty = postcode.isEmpty();
        boolean houseNumberEmpty = houseNumberText.isEmpty();
        boolean streetNameEmpty = streetName.isEmpty();
        boolean cityEmpty = city.isEmpty();
        boolean countyEmpty = county.isEmpty();

        if (emailEmpty) {error.append("Email cannot be empty\n");}
        if (postcodeEmpty) {error.append("Postcode cannot be empty\n");}
        if (houseNumberEmpty) {error.append("House number cannot be empty\n");}
        if (streetNameEmpty) {error.append("Street name cannot be empty\n");}
        if (cityEmpty) {error.append("Street name cannot be empty\n");}
        if (countyEmpty) {error.append("County cannot be empty\n");}

        if (error.length() > 0) {
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), error.toString());
            return false;
        }

        // Check if valid
        /*
          Card number
          CVC
          expiry
          postcode
          house number
         */

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

        if (!Register.isValidPostcode(postcode)) {
            errorBuilder.append("Please ensure you enter a valid UK postcode\n");
        }

        if (errorBuilder.length() > 0) {
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), errorBuilder.toString());
            return false;
        }

        return true;
    }

    private boolean confirmedOrder(){
        try {
            handler.openConnection();
            DatabaseOperations operations = new DatabaseOperations();
            return operations.confirmOrder(getMainFrame().getPendingOrder().getOrderNumber(), handler.getConnection());
        } catch (SQLException | NoOrderExistsException ex) {
            ex.printStackTrace();
        } finally {
            handler.closeConnection();
        }
        return false;
    }
}
