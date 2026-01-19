package views;

import database.DatabaseConnectionHandler;
import database.DatabaseOperations;
import model.Order;
import model.OrderLine;
import model.Product;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class Shop extends JPanel {
    private JPanel shopPanel;
    private JPanel buttonPanel;
    private CardLayout productCardLayout;
    private JButton trainSetsButton;
    private JButton locomotivesButton;
    private JButton controllersButton;
    private JButton allProductsButton;
    private JButton trackPacksButton;
    private JButton tracksButton;
    private JButton rollingStockButton;
    private JScrollPane inventoryScrollPane;
    private JPanel customInventoryPanel;
    private JButton orderHistoryButton;
    private JButton checkoutButton;
    private JButton accountSettingsButton;
    private JButton staffManagerControlsButton;
    private JPanel shopOrderPanel;
    private JLabel totalPrice;
    private JButton clearCurrentOrderButton;
    private final MainFrame mainFrame;
    private Order order;

    public Shop(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        createUIComponents();
        initComponents();

        // int userID, String date, double totalCost, OrderStatus orderStatus, ArrayList<OrderLine> orderLineArrayList
        assert mainFrame.getCurrentUser() != null;

        JPanel allProducts = setupProductPanel(mainFrame.getProducts(), "AllProducts");
        JPanel trainSets = setupProductPanel(mainFrame.getTrainSetProducts(), "TrainSet");
        JPanel trackPacks = setupProductPanel(mainFrame.getTrackPackProducts(), "TrackPack");
        JPanel controllers = setupProductPanel(mainFrame.getControllerProducts(), "Controller");
        JPanel locomotives = setupProductPanel(mainFrame.getLocomotiveProducts(), "Locomotive");
        JPanel rollingStocks = setupProductPanel(mainFrame.getRollingStockProducts(), "RollingStock");
        JPanel tracks = setupProductPanel(mainFrame.getTrackProducts(), "Track");
        showCardPanel(allProducts, "AllProducts");

        trainSetsButton.addActionListener(e -> showCardPanel(trainSets, "TrainSet"));
        trackPacksButton.addActionListener(e -> showCardPanel(trackPacks, "TrackPack"));
        controllersButton.addActionListener(e -> showCardPanel(controllers, "Controller"));
        locomotivesButton.addActionListener(e -> showCardPanel(locomotives, "Locomotive"));
        rollingStockButton.addActionListener(e -> showCardPanel(rollingStocks, "RollingStock"));
        tracksButton.addActionListener(e -> showCardPanel(tracks, "Track"));
        allProductsButton.addActionListener(e -> showCardPanel(allProducts, "AllProducts"));

        this.totalPrice.setText("Order Subtotal: £" + String.format("%.2f", order.getTotalCost()));

        orderHistoryButton.addActionListener(e -> {
            getMainFrame().gotoPage("OrderHistory", new OrderHistory(getMainFrame()).getOrderHistoryPanel());
        });
        accountSettingsButton.addActionListener(e -> {
            try {
                getMainFrame().gotoPage("AccountSettings", new AccountSettings(getMainFrame()).getAccountSettingsPanel());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        checkoutButton.addActionListener(e -> {
            try {
                getMainFrame().gotoPage("Checkout", new Checkout(getMainFrame(), order).getCheckoutPanel());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        clearCurrentOrderButton.addActionListener(e -> {
            DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
            DatabaseOperations operations = new DatabaseOperations();
            try {
                handler.openConnection();
                ArrayList<OrderLine> orderLines = getOrder().getOrderLineArrayList();
                while (orderLines.size() > 0){
                    OrderLine orderLine = orderLines.get(0);
                    orderLine.setQuantity(0);
                    getOrder().removeOrderLine(orderLine);
                    operations.updateOrder(getMainFrame().getPendingOrder().getOrderNumber(), orderLine, handler.getConnection());  //TODO This is very slow
                    getMainFrame().getPendingOrder().setTotalCost();
                }
                renderOrder();
                getCheckoutButton().setEnabled(false);
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

    public JPanel getShopPanel() {
        return shopPanel;
    }

    private void createUIComponents() {
        this.productCardLayout = new CardLayout();
        this.customInventoryPanel = new JPanel(productCardLayout);

        this.checkoutButton = new JButton("Checkout");

        staffManagerControlsButton = new JButton();
        staffManagerControlsButton.addActionListener(e -> {
            User user = getMainFrame().getCurrentUser();
            if (!user.isStaff() && !user.isManager()) {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), String.format("User %s does not have permission to access this page", user.getEmail()));
                return;
            }

            getMainFrame().gotoPage("StaffManagerControls", new StaffManagerControls(getMainFrame()).getStaffManagerControlsPanel());

        });


        if (getMainFrame().getCurrentUser() != null) {
            staffManagerControlsButton.setVisible(getMainFrame().getCurrentUser().isStaff() || getMainFrame().getCurrentUser().isManager());
        }

        this.shopOrderPanel = new JPanel();
        this.shopOrderPanel.setLayout(new BoxLayout(shopOrderPanel, BoxLayout.Y_AXIS));
        this.order = getMainFrame().getPendingOrder();

        if (this.order.getOrderLineArrayList().isEmpty()) {
            getCheckoutButton().setEnabled(false);
        }

        this.totalPrice = new JLabel();
        renderOrder();
    }

    private JPanel createProductPanel(Product product) {
        JPanel singleOrderPanel = new JPanel(new GridBagLayout());

//        int quantity = operations.getProductQuantityByProductID(product.getProductID(), handler.getConnection());
        JLabel productSummary = new JLabel(
                "<html>" +
                        "Item Name: <b>" + product.getProductName() +
                        "</b> | Scale: <b>" + product.getModellingScale() +
                        "</b></html>");

        JButton productDetailsButton = new JButton("View Info");
        productDetailsButton.addActionListener((e) -> {
            getMainFrame().gotoPage("ViewProductDetails", new ViewProductDetails(getMainFrame(), product).getProductDetailsPanel());
        });

        DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
        DatabaseOperations operations = new DatabaseOperations();

        JButton addToBasketButton = new JButton("Add to Basket");
        addToBasketButton.addActionListener(e -> {
            getCheckoutButton().setEnabled(true);

            int quantity = 1;
            for (OrderLine o : getMainFrame().getPendingOrder().getOrderLineArrayList()) {
                quantity = 1;
                if (o.getProduct().getProductID() == product.getProductID()) {
                    quantity = o.getQuantity() + 1;
                }
            }

            OrderLine orderLine = new OrderLine(product, quantity, product.getBrandName());
            getOrder().addOrderLine(orderLine);
            getOrder().setTotalCost();
            renderOrder();

            try {
                handler.openConnection();
                operations.updateOrder(getMainFrame().getPendingOrder().getOrderNumber(), orderLine, handler.getConnection());
                getMainFrame().getPendingOrder().setTotalCost();

            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                handler.closeConnection();
            }
        });

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 2; // Column 0
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.weightx = 1.0;
        constraints.weighty = 0;

        singleOrderPanel.add(productSummary, constraints);

        constraints.gridx = 0;
        constraints.weightx = 0;
        singleOrderPanel.add(productDetailsButton, constraints);

        constraints.gridx = 1;
        singleOrderPanel.add(addToBasketButton, constraints);

        return singleOrderPanel;
    }

    public JPanel setupProductPanel(List<Product> products, String panelName) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        for (Product p : products) {
            panel.add(createProductPanel(p));
        }

        if (products.isEmpty()) {
            JLabel errorLabel = new JLabel("There are no " + panelName + " products to display.");
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            errorLabel.setAlignmentY(Component.TOP_ALIGNMENT);
            panel.add(errorLabel);
        }

        return panel;
    }

    private void showCardPanel(JPanel panel, String panelName) {
        customInventoryPanel.removeAll();
        customInventoryPanel.add(panel, panelName);


        productCardLayout.show(customInventoryPanel, panelName);
        customInventoryPanel.revalidate();
        customInventoryPanel.repaint();
        inventoryScrollPane.revalidate();
        inventoryScrollPane.repaint();
    }

    private Order getOrder() {
        return this.order;
    }

    private void renderOrder() {
        getShopOrderPanel().removeAll();
        JPanel singleOrderLinePanel = new JPanel();
        singleOrderLinePanel.setLayout(new GridBagLayout());

        int y = 0;
        GridBagConstraints constraints = new GridBagConstraints();

        for (OrderLine orderLine : getOrder().getOrderLineArrayList()) {
            constraints.anchor = GridBagConstraints.CENTER;
            constraints.gridx = 0;
            constraints.gridy = y;

            JLabel label = new JLabel(String.format(
                    "%s [%s] | Quantity: %d | Price: £%.2f",
                    orderLine.getProduct().getProductName(),
                    orderLine.getProduct().getModellingScale().name(),
                    orderLine.getQuantity(),
                    orderLine.getLineCost()
            ));
            singleOrderLinePanel.add(label, constraints);

            JButton removeProductButton = new JButton("-");
            constraints.gridx = 1;

            DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
            DatabaseOperations operations = new DatabaseOperations();
            removeProductButton.addActionListener(e -> {
                try {
                    handler.openConnection();

                    if (orderLine.getQuantity() == 1) {
                        orderLine.setQuantity(0);
                        // updateOrderLine DB call, quantity = 0
                        getOrder().removeOrderLine(orderLine);
                        if (getOrder().getOrderLineArrayList().isEmpty()) {
                            getCheckoutButton().setEnabled(false);
                        }
                    } else {
                        orderLine.setQuantity(orderLine.getQuantity() - 1);
                        orderLine.setLineCost();
                    }
                    renderOrder();
                    boolean res = operations.updateOrder(getMainFrame().getPendingOrder().getOrderNumber(), orderLine, handler.getConnection());
                    getMainFrame().getPendingOrder().setTotalCost();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    handler.closeConnection();
                }
            });

            singleOrderLinePanel.add(removeProductButton, constraints);

            JButton addProductButton = new JButton("+");
            constraints.gridx = 2;
            addProductButton.addActionListener(e -> {
                try {
                    handler.openConnection();
                    orderLine.setQuantity(orderLine.getQuantity() + 1);
                    orderLine.setLineCost();
                    getOrder().addOrderLine(orderLine);
                    renderOrder();
                    operations.updateOrder(getMainFrame().getPendingOrder().getOrderNumber(), orderLine, handler.getConnection());
                    getMainFrame().getPendingOrder().setTotalCost();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    handler.closeConnection();
                }
            });

            singleOrderLinePanel.add(addProductButton, constraints);
            getShopOrderPanel().add(singleOrderLinePanel);
            y++;
        }

        getOrder().setTotalCost();
        this.totalPrice.setText("Order Subtotal: £" + String.format("%.2f", order.getTotalCost()));
        getShopOrderPanel().revalidate();
        getShopOrderPanel().repaint();
    }

    public JPanel getShopOrderPanel() {
        return shopOrderPanel;
    }

    public void setShopOrderPanel(JPanel shopOrderPanel) {
        this.shopOrderPanel = shopOrderPanel;
    }

    public JButton getCheckoutButton() {
        return checkoutButton;
    }

    private void initComponents() {
        // Main shop panel with border layout
        shopPanel = new JPanel(new BorderLayout());
        shopPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top panel with title and staff controls button
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Shop");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.PLAIN, 16));
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(staffManagerControlsButton, BorderLayout.EAST);

        // Left panel with products
        JPanel leftPanel = new JPanel(new BorderLayout());

        // Button panel for product categories
        buttonPanel = new JPanel(new GridLayout(3, 3, 5, 5));
        allProductsButton = new JButton("All Products");
        trainSetsButton = new JButton("Train Sets");
        controllersButton = new JButton("Controllers");
        locomotivesButton = new JButton("Locomotives");
        trackPacksButton = new JButton("Track Packs");
        tracksButton = new JButton("Tracks");
        rollingStockButton = new JButton("Rolling Stock");

        buttonPanel.add(new JLabel()); // empty cell
        buttonPanel.add(allProductsButton);
        buttonPanel.add(new JLabel()); // empty cell
        buttonPanel.add(trainSetsButton);
        buttonPanel.add(controllersButton);
        buttonPanel.add(locomotivesButton);
        buttonPanel.add(trackPacksButton);
        buttonPanel.add(tracksButton);
        buttonPanel.add(rollingStockButton);

        leftPanel.add(buttonPanel, BorderLayout.NORTH);

        // Scroll pane for inventory
        inventoryScrollPane = new JScrollPane(customInventoryPanel);
        inventoryScrollPane.setBorder(BorderFactory.createEmptyBorder());
        leftPanel.add(inventoryScrollPane, BorderLayout.CENTER);

        // Right panel with order details
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JLabel orderLabel = new JLabel("Your Order", SwingConstants.CENTER);
        rightPanel.add(orderLabel, BorderLayout.NORTH);

        JScrollPane orderScrollPane = new JScrollPane(shopOrderPanel);
        orderScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        orderScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rightPanel.add(orderScrollPane, BorderLayout.CENTER);

        JPanel orderBottomPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        orderBottomPanel.add(totalPrice);
        orderBottomPanel.add(checkoutButton);
        clearCurrentOrderButton = new JButton("Clear Current Order");
        orderBottomPanel.add(clearCurrentOrderButton);
        rightPanel.add(orderBottomPanel, BorderLayout.SOUTH);

        // Right side buttons panel
        JPanel rightSideButtonsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        accountSettingsButton = new JButton("Account Settings");
        orderHistoryButton = new JButton("Order History");
        rightSideButtonsPanel.add(staffManagerControlsButton);
        rightSideButtonsPanel.add(accountSettingsButton);
        rightSideButtonsPanel.add(orderHistoryButton);

        // Combine right panel and buttons
        JPanel rightOuterPanel = new JPanel(new BorderLayout());
        rightOuterPanel.add(rightSideButtonsPanel, BorderLayout.NORTH);
        rightOuterPanel.add(rightPanel, BorderLayout.CENTER);

        // Add all to main panel
        shopPanel.add(topPanel, BorderLayout.NORTH);
        shopPanel.add(leftPanel, BorderLayout.CENTER);
        shopPanel.add(rightOuterPanel, BorderLayout.EAST);
    }
}
