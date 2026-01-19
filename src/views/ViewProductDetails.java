package views;

import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ViewProductDetails {
    private JPanel productDetailsPanel;
    private JButton returnToInventoryManagementButton;
    private JPanel customProductJPanel;
    private final MainFrame mainFrame;
    private final Product product;

    public ViewProductDetails(MainFrame mainFrame, Product product) {
        this.mainFrame = mainFrame;
        this.product = product;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        customProductJPanel = new JPanel();
        customProductJPanel.setLayout(new BoxLayout(customProductJPanel, BoxLayout.Y_AXIS));

        JPanel returnButtonPanel = new JPanel();
        returnButtonPanel.setLayout(new BoxLayout(returnButtonPanel, BoxLayout.X_AXIS));

        returnButtonPanel.add(Box.createHorizontalGlue());

        returnToInventoryManagementButton = new JButton();
        if (getMainFrame().getCurrentUser() != null) {
            if (getMainFrame().getCurrentUser().isManager() || getMainFrame().getCurrentUser().isStaff()) {
                if (getMainFrame().isReturnToInventoryManagement()) {
                    returnToInventoryManagementButton.setText("Return to Inventory Management");
                } else {
                    returnToInventoryManagementButton.setText("Return to Shop");
                }
            } else {
                returnToInventoryManagementButton.setText("Return to Shop");
            }
        } else {
            returnToInventoryManagementButton.setText("Return to Shop");
        }

        returnToInventoryManagementButton.addActionListener(e -> {
            if (getMainFrame().isReturnToInventoryManagement()) {
                getMainFrame().setReturnToInventoryManagement(false);
                getMainFrame().gotoPage("InventoryManagement", new InventoryManagement(getMainFrame()).getInventoryManagementPanel());
            } else {
                getMainFrame().setReturnToInventoryManagement(false);
                getMainFrame().gotoPage("Shop", new Shop(getMainFrame()).getShopPanel());
            }
        });

        returnButtonPanel.add(returnToInventoryManagementButton);
        returnButtonPanel.add(Box.createHorizontalGlue());

        customProductJPanel.add(returnButtonPanel);

        String productType = "Unknown";

        ArrayList<String> trainSetProductsArrayList = new ArrayList<>();
        ArrayList<String> trackPackProductsArrayList = new ArrayList<>();

        Map<String, String> attributes = new LinkedHashMap<>();

        if (product instanceof Locomotive locomotive) {
            productType = "Locomotive";
            attributes.put("DCC Code: ", String.valueOf(locomotive.getLocomotiveModel()));
        }
        if (product instanceof RollingStock rollingStock) {
            productType = "Rolling Stock";

            String era1 = "Era 1 (1804-1874)";
            String era2 = "Era 2 (1875-1922)";
            String era3 = "Era 3 (1923-1947)";
            String era4 = "Era 4 (1948-1956)";
            String era5 = "Era 5 (1957-1966)";
            String era6 = "Era 6 (1967-1971)";
            String era7 = "Era 7 (1972-1982)";
            String era8 = "Era 8 (1983-1994)";
            String era9 = "Era 9 (1995-2004)";
            String era10 = "Era 10 (2005-2015)";
            String era11 = "Era 11 (2016-2026)";

            String startEra = "";
            switch (rollingStock.getStartEra()) {
                case ERA_1 -> startEra = era1;
                case ERA_2 -> startEra = era2;
                case ERA_3 -> startEra = era3;
                case ERA_4 -> startEra = era4;
                case ERA_5 -> startEra = era5;
                case ERA_6 -> startEra = era6;
                case ERA_7 -> startEra = era7;
                case ERA_8 -> startEra = era8;
                case ERA_9 -> startEra = era9;
                case ERA_10 -> startEra = era10;
                case ERA_11 -> startEra = era11;
            }

            String endEra = "";
            switch (rollingStock.getEndEra()) {
                case ERA_1 -> endEra = era1;
                case ERA_2 -> endEra = era2;
                case ERA_3 -> endEra = era3;
                case ERA_4 -> endEra = era4;
                case ERA_5 -> endEra = era5;
                case ERA_6 -> endEra = era6;
                case ERA_7 -> endEra = era7;
                case ERA_8 -> endEra = era8;
                case ERA_9 -> endEra = era9;
                case ERA_10 -> endEra = era10;
                case ERA_11 -> endEra = era11;
            }

            attributes.put("Start Era: ", startEra);
            attributes.put("End Era: ", endEra);
        } else if (product instanceof TrainSet trainSet) {
            productType = "Train Set";
            Map<Product, Integer> products = trainSet.getProducts();

            for (Map.Entry<Product, Integer> productMap : products.entrySet()) {
                Product p = productMap.getKey();
                int quantity = productMap.getValue();

                trainSetProductsArrayList.add(p.getProductName() + " (" + quantity + ")");
            }
//            attributes.put("Included Products: ", trainSetProductsArrayList.toString().replaceAll("^.|.$", ""));

            // Eurostar Train Set
            // Class 373 Eurostar EMU Power Car - Locomotive, DCC Ready
            // Mark 2 Unpowered Trailer Car - Carriage -
            // Mark 2 Passenger Saloon - Carriage

            // convert products to string
        } else if (product instanceof TrackPiece trackPiece) {
            String radius = trackPiece.getRadius();
            if (radius != null) {
                attributes.put("Radius: ", radius);
            }
            productType = "Track Piece";
        } else if (product instanceof TrackPack trackPack) {
            Map<String, Integer> trackPackPieces = trackPack.getTrackPieces();

            for (Map.Entry<String, Integer> productMap : trackPackPieces.entrySet()) {
                String p = productMap.getKey();
                int quantity = productMap.getValue();

                trackPackProductsArrayList.add(p + " (" + quantity + ")");
            }
            productType = "Track Pack";
        } else if (product instanceof Controller controller) {
            productType = "Controller";
            attributes.put("Signal Type: ", String.valueOf(controller.getSignalType()));
        }


        JLabel label = new JLabel("Product Overview");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setFont(new Font("Dialog", Font.PLAIN, 15));
        this.customProductJPanel.add(label);

        customProductJPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        if (getMainFrame().getCurrentUser() != null) {
            if (getMainFrame().isReturnToInventoryManagement()) {
                label = new JLabel("Product ID: " + product.getProductID());
                label.setAlignmentX(Component.CENTER_ALIGNMENT);
                this.customProductJPanel.add(label);

                if (product.getStock() != -1) {
                    label = new JLabel("Quantity: " + product.getStock());
                    label.setAlignmentX(Component.CENTER_ALIGNMENT);
                    this.customProductJPanel.add(label);
                }
            }
        }
        this.customProductJPanel.add(label);
        label = new JLabel("Product Name: " + product.getProductName());
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.customProductJPanel.add(label);
        label = new JLabel("Product Type: " + productType);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.customProductJPanel.add(label);
        label = new JLabel("Product Code: " + product.getProductCode());
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.customProductJPanel.add(label);


        customProductJPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        label = new JLabel("Brand Name: " + product.getBrandName());
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.customProductJPanel.add(label);
        label = new JLabel("Manufacturer Name: " + product.getManufacturerCode());
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.customProductJPanel.add(label);

        customProductJPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        label = new JLabel("Retail Price: £" + String.format("%.2f", product.getRetailPrice()));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.customProductJPanel.add(label);
        label = new JLabel("Modelling Scale: " + product.getModellingScale());
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.customProductJPanel.add(label);

        customProductJPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        if (!attributes.isEmpty()) {
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                label = new JLabel(attribute.getKey() + attribute.getValue());
                label.setAlignmentX(Component.CENTER_ALIGNMENT);
                this.customProductJPanel.add(label);
            }
        }

        customProductJPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        if (product instanceof TrainSet) {
            JLabel includesLabel = new JLabel("Set Includes:");
            includesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            this.customProductJPanel.add(includesLabel);

            for (String product: trainSetProductsArrayList) {
                JLabel productLabel = new JLabel(product);
                productLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                this.customProductJPanel.add(productLabel);
            }
        } else if (product instanceof TrackPack) {
            JLabel includesLabel = new JLabel("Pack Includes:");
            includesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            this.customProductJPanel.add(includesLabel);
            for (String product: trackPackProductsArrayList) {
                JLabel productLabel = new JLabel(product);
                productLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                this.customProductJPanel.add(productLabel);
            }
        }
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public Product getProduct() {
        return product;
    }



    public JPanel getProductDetailsPanel() {
        return productDetailsPanel;
    }
}
