package views;

import database.DatabaseConnectionHandler;
import database.DatabaseOperations;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProduct extends JPanel {
    private MainFrame mainFrame;
    private JPanel updateProductPanel;
    private JPanel customProductAttributesPanel;
    private Product product;
    private Map<String, JTextField> textFieldMap;
    private Map<String, JComboBox<String>> comboBoxMap;


    public UpdateProduct(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        this.textFieldMap = new LinkedHashMap<>();
        this.comboBoxMap = new LinkedHashMap<>();
        this.customProductAttributesPanel = new JPanel();
        this.customProductAttributesPanel.setLayout(new BoxLayout(customProductAttributesPanel, BoxLayout.Y_AXIS));
        JPanel productDetailsPanel = new JPanel();
        productDetailsPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        Product product = getMainFrame().getProductToUpdate();

        int gridy = 0;
        String componentText = "Product ID";
        productDetailsPanel.add(createLabel(componentText),
                createConstraints(GridBagConstraints.EAST, 0, gridy, 1));
        productDetailsPanel.add(createTextField(componentText,
                String.valueOf(product.getProductID()), false),
                createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        gridy++;

        componentText = "Product Code";
        productDetailsPanel.add(createLabel(componentText),
                createConstraints(GridBagConstraints.EAST, 0, gridy, 1));
        productDetailsPanel.add(createTextField(componentText,
                product.getProductCode(), false),
                createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        gridy++;

        componentText = "Product Name";
        productDetailsPanel.add(createLabel(componentText),
                createConstraints(GridBagConstraints.EAST, 0, gridy, 1));
        productDetailsPanel.add(createTextField(componentText,
                product.getProductName(), true),
                createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        gridy++;

        componentText = "Manufacturer Name";
        productDetailsPanel.add(createLabel(componentText),
                createConstraints(GridBagConstraints.EAST, 0, gridy, 1));
        productDetailsPanel.add(createTextField(componentText,
                product.getManufacturerCode(), true),
                createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        gridy++;

        componentText = "Brand Name";
        productDetailsPanel.add(createLabel(componentText),
                createConstraints(GridBagConstraints.EAST, 0, gridy, 1));
        productDetailsPanel.add(createTextField(componentText,
                product.getBrandName(), true),
                createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        gridy++;

        componentText = "Modelling Scale";
        productDetailsPanel.add(createLabel(componentText),
                createConstraints(GridBagConstraints.EAST, 0, gridy, 1));
        productDetailsPanel.add(createComboBox(componentText,
                new String[]{"OO_GAUGE", "TT_GAUGE", "N_GAUGE"},
                product.getModellingScale() == null ? "None" : product.getModellingScale().name()),
            createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        gridy++;

        componentText = "Retail Price (£)";
        productDetailsPanel.add(createLabel(componentText),
                createConstraints(GridBagConstraints.EAST, 0, gridy, 1));
        productDetailsPanel.add(createTextField(componentText,
                String.format("%.2f", product.getRetailPrice()), true),
                createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        gridy++;

        componentText = "Stock";
        productDetailsPanel.add(createLabel(componentText),
                createConstraints(GridBagConstraints.EAST, 0, gridy, 1));
        productDetailsPanel.add(createTextField(componentText,
                String.valueOf(product.getStock()), true),
                createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        gridy++;

        if (product instanceof Locomotive locomotive) {

            componentText = "Locomotive Model";
            productDetailsPanel.add(createLabel(componentText),
                    createConstraints(GridBagConstraints.EAST, 0, gridy, 1));
            productDetailsPanel.add(createComboBox(componentText,
                    new String[] {"ANALOGUE", "DCC_READY", "DCC_FITTED", "DCC_SOUND"},
                    String.valueOf(locomotive.getLocomotiveModel())),
                    createConstraints(GridBagConstraints.WEST, 1, gridy, 1));

            // Class A3 "Flying Scotsman"
            // L100

            gridy++;
        }
        if (product instanceof RollingStock rollingStock) {
            componentText = "Start Era";
            productDetailsPanel.add(createLabel(componentText),
                    createConstraints(GridBagConstraints.EAST, 0, gridy, 1));

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

            productDetailsPanel.add(createComboBox(componentText,
                            new String[] {era1, era2, era3, era4, era5, era6, era7, era8, era9, era10, era11},
                            startEra),
                    createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
            gridy++;

            componentText = "End Era";
            productDetailsPanel.add(createLabel(componentText),
                    createConstraints(GridBagConstraints.EAST, 0, gridy, 1));

            productDetailsPanel.add(createComboBox(componentText,
                            new String[] {era1, era2, era3, era4, era5, era6, era7, era8, era9, era10, era11},
                            endEra),
                    createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
            gridy++;

            // [LMS/LNER/GWR/SR]/[MARK 1/MARK 2/MARK 3/MARK 4] [Carriage Description]
            // If matches regex => Carriage, if not => Wagon
            String carriageRegex = "^(LMS|LNER|GWR|SR|MARK 1|MARK 2|MARK 3|MARK 4) (.+)";
            Pattern pattern = Pattern.compile(carriageRegex);
            String rollingStockName = rollingStock.getProductName();
            Matcher matcher = pattern.matcher(rollingStockName);

            if (matcher.find()) {
                getTextFieldMap().get("Product Name").setEnabled(false);
                // Matched carriage regex -> rolling stock is a carriage

                String carriageType = matcher.group(1);
                String carriageDescription = matcher.group(2);

                componentText = "Company Name/BR Standard Mark";
                productDetailsPanel.add(createLabel(componentText),
                        createConstraints(GridBagConstraints.EAST, 0, gridy, 1));

                productDetailsPanel.add(createComboBox(componentText,
                                new String[] {"LMS", "LNER", "GWR", "SR", "MARK 1", "MARK 2", "MARK 3", "MARK 4"},
                                carriageType),
                        createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
                gridy++;

                componentText = "Carriage Description";
                productDetailsPanel.add(createLabel(componentText),
                        createConstraints(GridBagConstraints.EAST, 0, gridy, 1));

                productDetailsPanel.add(createTextField(componentText,
                                carriageDescription, true),
                        createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
            }
        } else if (product instanceof TrainSet trainSet) {
            LinkedHashMap<Product, Integer> trainSetMap = trainSet.getProducts();

            componentText = "This Train Set Includes";
            productDetailsPanel.add(createLabel(componentText),
                    createConstraints(GridBagConstraints.CENTER, 0, gridy, 2));

            for (Map.Entry<Product, Integer> entry : trainSetMap.entrySet()) {
                String containedProductCode = entry.getKey().getProductName();
                int containedQuantity = entry.getValue();

                gridy++;

                componentText = String.format("%s (x%d)", containedProductCode, containedQuantity);
                productDetailsPanel.add(createLabel(componentText),
                        createConstraints(GridBagConstraints.CENTER, 0, gridy, 2));
            }

            // contains products
            // product code , quantity
        } else if (product instanceof TrackPiece trackPiece) {
            String trackName = trackPiece.getProductName()
                    .replaceAll("1st Radius", "")
                    .replaceAll("2nd Radius", "")
                    .replaceAll("3rd Radius", "").strip();

            String regEx = "^((1st|2nd|3rd) Radius )?(.+)$";
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(trackPiece.getProductName());

            getTextFieldMap().get("Product Name").setText(trackName);

            componentText = "Radius";
            productDetailsPanel.add(createLabel(componentText),
                    createConstraints(GridBagConstraints.EAST, 0, gridy, 1));

            String productRadius = (trackPiece.getRadius() == null) ? "None" : String.valueOf(trackPiece.getRadius());

            productDetailsPanel.add(createComboBox(componentText,
                            new String[] {"None", "1st Radius", "2nd Radius", "3rd Radius"},
                            productRadius),
                    createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        } else if (product instanceof TrackPack trackPack) {
            LinkedHashMap<String, Integer> trackPackMap = trackPack.getTrackPieces();

            componentText = "This Track Pack includes";
            productDetailsPanel.add(createLabel(componentText),
                    createConstraints(GridBagConstraints.CENTER, 0, gridy, 2));
            gridy++;

            getTextFieldMap().get("Product Name").setEnabled(false);

            for (Map.Entry<String, Integer> entry : trackPackMap.entrySet()) {
                String containedProductName = entry.getKey();
                int containedQuantity = entry.getValue();

                gridy++;
                componentText = String.format("%s (x%d)", containedProductName, containedQuantity);
                productDetailsPanel.add(createLabel(componentText),
                        createConstraints(GridBagConstraints.CENTER, 0, gridy, 2));
            }
        } else if (product instanceof Controller controller) {

            componentText = "Signal Type";
            productDetailsPanel.add(createLabel(componentText),
                    createConstraints(GridBagConstraints.EAST, 0, gridy, 1));

            productDetailsPanel.add(createComboBox(componentText,
                            new String[] {"ANALOGUE", "DIGITAL"},
                            controller.getSignalType().name()),
                    createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        }

        customProductAttributesPanel.add(productDetailsPanel);

        JButton updateProductButton = new JButton("Confirm");
        JButton cancelButton = new JButton("Cancel");
        JButton deleteButton = new JButton("Delete Product");

        DatabaseOperations operations = new DatabaseOperations();
        updateProductButton.addActionListener(e -> {
            DatabaseConnectionHandler handler = new DatabaseConnectionHandler();

            try {
                handler.openConnection();
                Product productToUpdate = getMainFrame().getProductToUpdate();
                // int productID,
                // String productCode, String brandName, String manufacturerCode, String productName, double retailPrice, SizeRatio modellingScale, int stock
                int productID = productToUpdate.getProductID(); // Cannot be updated; field disabled
                String productCode = productToUpdate.getProductCode(); // Cannot be updated; field disabled
                String brandNameText = getTextFieldMap().get("Brand Name").getText().strip();
                String manufacturerCodeText = getTextFieldMap().get("Manufacturer Name").getText().strip();
                String productNameText = getTextFieldMap().get("Product Name").getText().strip();
                String priceText = getTextFieldMap().get("Retail Price (£)").getText().strip();
                Product.SizeRatio modellingScaleText = Product.SizeRatio.valueOf((String) getComboBoxMap().get("Modelling Scale").getSelectedItem());
                String stockText = getTextFieldMap().get("Stock").getText().strip();

                StringBuilder error = new StringBuilder();

                if (productNameText.isEmpty()) {
                    error.append("Product Code cannot be empty\n");
                }
                if (brandNameText.isEmpty()) {
                    error.append("Brand Name cannot be empty\n");
                }
                if (manufacturerCodeText.isEmpty()) {
                    error.append("Manufacturer Name cannot be empty\n");
                }
                if (priceText.isEmpty()) {
                    error.append("Product Name cannot be empty\n");
                }
                if (stockText.isEmpty()) {
                    error.append("Stock cannot be empty\n");
                }

                double price = -1;
                try {
                    price = Double.parseDouble(priceText);
                    new DecimalFormat("#.##").format(price);
                    if (price < 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    error.append("Price must be an integer greater than or equal to 0\n");
                }

                int stock = -1;
                try {
                    stock = Integer.parseInt(stockText);
                    if (stock < 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    error.append("Stock must be an integer greater than or equal to 0");
                }

                if (error.length() > 0) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), error);
                    return;
                }

                Product updatedProduct = new Product(productID, productCode, brandNameText, manufacturerCodeText, productNameText, price, modellingScaleText, stock);

                if (getComboBoxMap().containsKey("Start Era") && getComboBoxMap().containsKey("End Era")) {
                    String startEraText = (String) getComboBoxMap().get("Start Era").getSelectedItem();
                    String endEraText = (String) getComboBoxMap().get("End Era").getSelectedItem();
                    assert startEraText != null;
                    assert endEraText != null;

                    RollingStock.Era startEra = null;
                    switch (startEraText) {
                        case "Era 1 (1804-1874)" -> startEra = RollingStock.Era.ERA_1;
                        case "Era 2 (1875-1922)" -> startEra = RollingStock.Era.ERA_2;
                        case "Era 3 (1923-1947)" -> startEra = RollingStock.Era.ERA_3;
                        case "Era 4 (1948-1956)" -> startEra = RollingStock.Era.ERA_4;
                        case "Era 5 (1957-1966)" -> startEra = RollingStock.Era.ERA_5;
                        case "Era 6 (1967-1971)" -> startEra = RollingStock.Era.ERA_6;
                        case "Era 7 (1972-1982)" -> startEra = RollingStock.Era.ERA_7;
                        case "Era 8 (1983-1994)" -> startEra = RollingStock.Era.ERA_8;
                        case "Era 9 (1995-2004)" -> startEra = RollingStock.Era.ERA_9;
                        case "Era 10 (2005-2015)" -> startEra = RollingStock.Era.ERA_10;
                        case "Era 11 (2016-2026)" -> startEra = RollingStock.Era.ERA_11;
                    }

                    RollingStock.Era endEra = null;
                    switch (endEraText) {
                        case "Era 1 (1804-1874)" -> endEra = RollingStock.Era.ERA_1;
                        case "Era 2 (1875-1922)" -> endEra = RollingStock.Era.ERA_2;
                        case "Era 3 (1923-1947)" -> endEra = RollingStock.Era.ERA_3;
                        case "Era 4 (1948-1956)" -> endEra = RollingStock.Era.ERA_4;
                        case "Era 5 (1957-1966)" -> endEra = RollingStock.Era.ERA_5;
                        case "Era 6 (1967-1971)" -> endEra = RollingStock.Era.ERA_6;
                        case "Era 7 (1972-1982)" -> endEra = RollingStock.Era.ERA_7;
                        case "Era 8 (1983-1994)" -> endEra = RollingStock.Era.ERA_8;
                        case "Era 9 (1995-2004)" -> endEra = RollingStock.Era.ERA_9;
                        case "Era 10 (2005-2015)" -> endEra = RollingStock.Era.ERA_10;
                        case "Era 11 (2016-2026)" -> endEra = RollingStock.Era.ERA_11;
                    }

                    if (getComboBoxMap().containsKey("Locomotive Model")) {
                        // Locomotive
                        String locomotiveModelText = (String) getComboBoxMap().get("Locomotive Model").getSelectedItem();
                        updatedProduct = new Locomotive(
                                productID,
                                productCode,
                                brandNameText,
                                manufacturerCodeText,
                                productNameText,
                                price,
                                modellingScaleText,
                                stock,
                                startEra,
                                endEra,
                                Locomotive.LocomotiveModel.valueOf(locomotiveModelText)
                        );
                    } else {
                        if (getComboBoxMap().containsKey("Company Name/BR Standard Mark")) {
                            // Carriage
                            String carriageType = (String) getComboBoxMap().get("Company Name/BR Standard Mark").getSelectedItem();
                            String carriageDescription = getTextFieldMap().get("Carriage Description").getText();

                            if (carriageDescription.isEmpty()) {
                                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Carriage Description cannot be empty");
                                return;
                            }

                            productNameText = String.format("%s %s",
                                    carriageType,
                                    carriageDescription
                            );
                        }
                        // If not Carriage, then Wagon
                        updatedProduct = new RollingStock(
                                productID,
                                productCode,
                                brandNameText,
                                manufacturerCodeText,
                                productNameText,
                                price,
                                modellingScaleText,
                                stock,
                                startEra,
                                endEra
                        );
                    }
                }
                if (getComboBoxMap().containsKey("Radius")) {
                    String radius = (String) getComboBoxMap().get("Radius").getSelectedItem();
                    if (radius.equals("None")) {
                        radius = null;
                    }

                    updatedProduct = new TrackPiece(
                            productID,
                            productCode,
                            brandNameText,
                            manufacturerCodeText,
                            radius == null ? productNameText : radius + " " + productNameText,
                            price,
                            modellingScaleText,
                            stock,
                            radius
                    );
                }
                if (getComboBoxMap().containsKey("Signal Type")) {
                    Controller.SignalType signalType = Controller.SignalType.valueOf((String) getComboBoxMap().get("Signal Type").getSelectedItem());
                    updatedProduct = new Controller(
                            productID,
                            productCode,
                            brandNameText,
                            manufacturerCodeText,
                            productNameText,
                            price,
                            modellingScaleText,
                            stock,
                            signalType
                    );
                }
                try {
                    TrainSet oldTrainSet = (TrainSet) getMainFrame().getProductToUpdate();
                    updatedProduct = new TrainSet(
                            productID,
                            productCode,
                            brandNameText,
                            manufacturerCodeText,
                            productNameText,
                            price,
                            modellingScaleText,
                            stock,
                            oldTrainSet.getTrainSetID(),
                            oldTrainSet.getProducts()
                    );
                    // Throws ClassCastException if the product to update is not a TrainSet
                } catch (ClassCastException ignored) {}
                try {
                    TrackPack oldTrackPack = (TrackPack) getMainFrame().getProductToUpdate();
                    TrackPack.TrackType oldTrackPackType = getTrackTypeFromPieces(oldTrackPack.getTrackPieces());

                    updatedProduct = new TrackPack(
                            productID,
                            productCode,
                            brandNameText,
                            manufacturerCodeText,
                            productNameText,
                            price,
                            modellingScaleText,
                            stock,
                            oldTrackPackType
                    );

                    // Throws ClassCastException if the product to update is not a TrackPack
                } catch (ClassCastException ignored) {}
                // Else - if updatedProduct is not updated -> unknown product


                if (operations.editExistingProduct(product, updatedProduct, handler.getConnection())) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Product successfully updated");
                    getMainFrame().setProducts(handler.getConnection());

                    getMainFrame().gotoPage("InventoryManagement", new InventoryManagement(getMainFrame()).getInventoryManagementPanel());
                } else {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                            "<html>A product already exists with the given Product Name and Modelling Scale." +
                                    "<br><br>Note: " +
                                    "<br>For Carriages, the Product Name is derived by concatenating the Company Name or BR Standard Mark with the Carriage Description." +
                                    "<br>For Track Pieces, if the Radius is not null, the Product Name includes the Radius followed by the standard Product Name.</html>");

                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                ex.printStackTrace();
            } finally {
                handler.closeConnection();
            }
        });

        cancelButton.addActionListener(e -> {
            // return to inventory management
            getMainFrame().gotoPage("InventoryManagement", new InventoryManagement(getMainFrame()).getInventoryManagementPanel());
        });

        deleteButton.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(
                    JOptionPane.getRootFrame(),
                    "Do you really wish to delete the product?",
                    "Confirm Product Deletion",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (response == JOptionPane.OK_OPTION) {
                DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
                try {
                    handler.openConnection();
                       if (operations.deleteProduct(product, handler.getConnection())) {
                           JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Product successfully deleted");
                           getMainFrame().gotoPage("InventoryManagement", new InventoryManagement(getMainFrame()).getInventoryManagementPanel());
                       } else {
                           JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "An error occurred when deleting the product");
                       }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                } finally {
                    handler.closeConnection();
                }
            }
        });


        customProductAttributesPanel.add(updateProductButton);
        customProductAttributesPanel.add(cancelButton);
        customProductAttributesPanel.add(deleteButton);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        return label;
    }

    private JTextField createTextField(String key, String text, boolean isEnabled) {
        JTextField textField = new JTextField(text);
        textField.setColumns(20);
        textField.setEnabled(isEnabled);
        getTextFieldMap().put(key, textField);
        return textField;
    }

    private JComboBox<String> createComboBox(String key, String[] items, String selectedItem) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setSelectedItem(selectedItem);
        getComboBoxMap().put(key, comboBox);
        return comboBox;
    }

    private GridBagConstraints createConstraints(int anchor, int gridX, int gridY, int gridWidth) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = anchor;
        constraints.gridx = gridX;
        constraints.gridy = gridY;
        constraints.gridwidth = gridWidth;
        return constraints;
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public JPanel getUpdateProductPanel() {
        return updateProductPanel;
    }

    public Map<String, JTextField> getTextFieldMap() {
        return textFieldMap;
    }

    public Map<String, JComboBox<String>> getComboBoxMap() {
        return comboBoxMap;
    }

    private TrackPack.TrackType getTrackTypeFromPieces(Map<String, Integer> trackPieces) {
        // Create a map for each type of Track Pack, then compare the trackPieces map against those to
        // determine the type of TrackPack

        Map<String, Integer> secondRadiusStarterOval = new HashMap<>();
        secondRadiusStarterOval.put("2nd Radius Double Curve", 8);
        secondRadiusStarterOval.put("Single Straight", 2);

        Map<String, Integer> thirdRadiusStarterOval = new HashMap<>();
        thirdRadiusStarterOval.put("3rd Radius Double Curve", 8);
        thirdRadiusStarterOval.put("Single Straight", 2);

        Map<String, Integer> trackPackA = new HashMap<>();
        trackPackA.put("Single Straight", 1);
        trackPackA.put("Double Straight", 1);
        trackPackA.put("2nd Radius Single Curve", 2);
        trackPackA.put("Left-Hand Point", 1);
        trackPackA.put("Buffer Stop", 1);

        Map<String, Integer> trackPackB = new HashMap<>();
        trackPackB.put("Single Straight", 2);
        trackPackB.put("2nd Radius Single Curve", 4);
        trackPackB.put("2nd Radius Double Curve", 1);
        trackPackB.put("Right-Hand Point", 1);
        trackPackB.put("Buffer Stop", 1);

        if (trackPieces.equals(secondRadiusStarterOval)) {
            return TrackPack.TrackType.SECOND_RADIUS_STARTER_OVAL;
        } else if (trackPieces.equals(thirdRadiusStarterOval)) {
            return TrackPack.TrackType.THIRD_RADIUS_STARTER_OVAL;
        } else if (trackPieces.equals(trackPackA)) {
            return TrackPack.TrackType.TRACK_PACK_A;
        } else if (trackPieces.equals(trackPackB)) {
            return TrackPack.TrackType.TRACK_PACK_B;
        } else {
            throw new IllegalArgumentException("The track pieces do not match any known TrackType.");
        }
    }
}
