package views;

import database.DatabaseConnectionHandler;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class InventoryManagement {
    private CardLayout productCardLayout;
    private JPanel customInventoryManagementPanel;
    private JPanel inventoryManagementPanel;
    private JButton returnToStaffButton;
    private JButton trainSetsButton;
    private JButton controllersButton;
    private JButton locomotivesButton;
    private JButton rollingStockButton;
    private JButton trackPacksButton;
    private JButton addNewProductButton;
    private JButton updateExistingProductButton;
    private final MainFrame mainFrame;
    private JScrollPane inventoryScrollPane;
    private JButton tracksButton;
    private JButton allProductsButton;

    public InventoryManagement(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        createUIComponents();
        initComponents();

        // Create all cards
        // TrainSet TrackPack Controller Track Locomotive RollingStock
        // On button click, simply show that card

        returnToStaffButton.addActionListener(e -> {
            getMainFrame().gotoPage("StaffManagerControls", new StaffManagerControls(getMainFrame()).getStaffManagerControlsPanel());
        });


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

        updateExistingProductButton.addActionListener(e -> {
            String productIDText = JOptionPane.showInputDialog(JOptionPane.getRootFrame(), "Enter the Product ID of the product you wish to update");

            if (productIDText == null) {
                return;
            } else if (productIDText.isEmpty()) {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "The entered Product ID is invalid");
                return;
            }

            DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
            try {
                int productID = Integer.parseInt(productIDText);
                handler.openConnection();
                Product product = null;

                for (Product x : getMainFrame().getProducts()) {
                    if (x.getProductID() == productID) {
                        product = x;
                    }
                }

                if (product == null) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "No product exists with the given Product ID");
                    return;
                }

                getMainFrame().setProductToUpdate(product);

                getMainFrame().gotoPage("ViewProductDetails", new UpdateProduct(getMainFrame()).getUpdateProductPanel());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "The entered Product ID is invalid");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        addNewProductButton.addActionListener(e -> {
            String stringBuilder = "<html>Product Type" +
                    "<br><br>1: Locomotive" +
                    "<br>2: Carriage" +
                    "<br>3: Wagon" +
                    "<br>4: Track Piece" +
                    "<br>5: Controller";

            String newProductTypeNumberText = JOptionPane.showInputDialog(JOptionPane.getRootFrame(), stringBuilder);
            if (newProductTypeNumberText == null) {
                return;
            }

            int newProductTypeNumber;
            try {
                newProductTypeNumber = Integer.parseInt(newProductTypeNumberText);

                if (newProductTypeNumber != 1 && newProductTypeNumber != 2 &&
                    newProductTypeNumber != 3 && newProductTypeNumber != 4 &&
                    newProductTypeNumber != 5
                ) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Please enter a valid Product Type number (1,2,3,4,5)");
                return;
            }

            getMainFrame().setNewProductTypeNumber(newProductTypeNumber);

            getMainFrame().gotoPage("AddNewProduct", new AddNewProduct(getMainFrame()).getAddNewProductPanel());
        });
    }

    public JPanel getInventoryManagementPanel() {
        return inventoryManagementPanel;
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        // Card Layout
        // Create all cards
        // TrainSet TrackPack Controller Track Locomotive RollingStock
        // On button click, simply show that card

        this.productCardLayout = new CardLayout();
        this.customInventoryManagementPanel = new JPanel(productCardLayout);
    }

    public JPanel getCustomInventoryManagementPanel() {
        return customInventoryManagementPanel;
    }

    public void setCustomInventoryManagementPanel(JPanel customInventoryManagementPanel) {
        this.customInventoryManagementPanel = customInventoryManagementPanel;
    }

    private JPanel createProductPanel(Product product) {
        JPanel singleOrderPanel = new JPanel(new GridBagLayout());

        JLabel productSummary = new JLabel(
                "<html>ID: <b>" + product.getProductID() +
                        "</b> | Code: <b>" + product.getProductCode() +
                        "</b> | Name: <b>" + product.getProductName() +
                        "</b> | Brand: <b>" + product.getBrandName() +
                        "</b> | Scale: <b>" + product.getModellingScale() +
                        "</b> | Quantity: <b>" + product.getStock() + "</b></html>");

        JButton productDetailsButton = new JButton("View Product Information");
        productDetailsButton.addActionListener((e) -> {
            getMainFrame().setReturnToInventoryManagement(true);
            getMainFrame().gotoPage("ViewProductDetails", new ViewProductDetails(getMainFrame(), product).getProductDetailsPanel());
        });

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.gridx = 0; // Column 0
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.weightx = 1.0;
        constraints.weighty = 0;

        singleOrderPanel.add(productSummary, constraints);

        constraints.gridx = 1;
        constraints.weightx = 0;
        singleOrderPanel.add(productDetailsButton, constraints);

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
        customInventoryManagementPanel.removeAll();
        customInventoryManagementPanel.add(panel, panelName);


        productCardLayout.show(customInventoryManagementPanel, panelName);
        customInventoryManagementPanel.revalidate();
        customInventoryManagementPanel.repaint();
        inventoryScrollPane.revalidate();
        inventoryScrollPane.repaint();
    }

    private void initComponents() {
        // Main panel with border layout
        inventoryManagementPanel = new JPanel(new BorderLayout());
        inventoryManagementPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title label at the top
        JLabel titleLabel = new JLabel("Inventory Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.PLAIN, 16));
        inventoryManagementPanel.add(titleLabel, BorderLayout.NORTH);

        // Center panel with controls and inventory
        JPanel centerPanel = new JPanel(new BorderLayout());

        // Control panel with buttons
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Return button
        returnToStaffButton = new JButton("Return to Staff Control Panel");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        controlPanel.add(returnToStaffButton, gbc);

        // Add/Edit product buttons
        JPanel addEditPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addNewProductButton = new JButton("Add New Product");
        updateExistingProductButton = new JButton("Edit Existing Product");
        addEditPanel.add(addNewProductButton);
        addEditPanel.add(updateExistingProductButton);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 5;
        controlPanel.add(addEditPanel, gbc);

        // Product type label
        JLabel selectLabel = new JLabel("Select Product Type to View Products", SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 5;
        controlPanel.add(selectLabel, gbc);

        // All products button
        allProductsButton = new JButton("All Products");
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        controlPanel.add(allProductsButton, gbc);

        // Product type buttons row 1
        trainSetsButton = new JButton("Train Sets");
        gbc.gridx = 0;
        gbc.gridy = 4;
        controlPanel.add(trainSetsButton, gbc);

        controllersButton = new JButton("Controllers");
        gbc.gridx = 1;
        gbc.gridy = 4;
        controlPanel.add(controllersButton, gbc);

        locomotivesButton = new JButton("Locomotives");
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        controlPanel.add(locomotivesButton, gbc);

        // Product type buttons row 2
        trackPacksButton = new JButton("Track Packs");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        controlPanel.add(trackPacksButton, gbc);

        tracksButton = new JButton("Tracks");
        gbc.gridx = 1;
        gbc.gridy = 5;
        controlPanel.add(tracksButton, gbc);

        rollingStockButton = new JButton("Rolling Stock");
        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        controlPanel.add(rollingStockButton, gbc);

        centerPanel.add(controlPanel, BorderLayout.NORTH);

        // Scroll pane for inventory
        inventoryScrollPane = new JScrollPane(customInventoryManagementPanel);
        inventoryScrollPane.setBorder(BorderFactory.createEmptyBorder());
        centerPanel.add(inventoryScrollPane, BorderLayout.CENTER);

        inventoryManagementPanel.add(centerPanel, BorderLayout.CENTER);
    }
}
