package views;

import database.DatabaseConnectionHandler;
import database.DatabaseOperations;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public class AddNewProduct extends JPanel {
    private final MainFrame mainFrame;
    private JPanel addNewProductPanel;
    private JPanel customProductFieldsPanel;
    private Map<String, JTextField> textFieldMap;
    private Map<String, JComboBox<String>> comboBoxMap;

    public AddNewProduct(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        createUIComponents();
        initComponents();
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public JPanel getAddNewProductPanel() {
        return addNewProductPanel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        this.textFieldMap = new LinkedHashMap<>();
        this.comboBoxMap = new LinkedHashMap<>();
        customProductFieldsPanel = new JPanel();
        customProductFieldsPanel.setLayout(new GridBagLayout());

        int productType = getMainFrame().getNewProductTypeNumber();

        int gridy = 0;
        String addComponentText = null;
        switch (productType) {
            case 1 -> addComponentText = "Add New Locomotive";
            case 2 -> addComponentText = "Add New Carriage";
            case 3 -> addComponentText = "Add New Wagon";
            case 4 -> addComponentText = "Add New Track Piece";
            case 5 -> addComponentText = "Add New Controller";
        }

        assert addComponentText != null;
        customProductFieldsPanel.add(createLabel(addComponentText),
        createConstraints(GridBagConstraints.CENTER, 0, gridy, 2));
        gridy++;

        String componentText = "Product Code";
        customProductFieldsPanel.add(createLabel(componentText),
                createConstraints(GridBagConstraints.EAST, 0, gridy, 1));
        customProductFieldsPanel.add(createTextField(componentText, true),
                createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        gridy++;

        componentText = "Product Name";
        customProductFieldsPanel.add(createLabel(componentText),
                createConstraints(GridBagConstraints.EAST, 0, gridy, 1));
        customProductFieldsPanel.add(createTextField(componentText, true),
                createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        gridy++;

        componentText = "Manufacturer Name";
        customProductFieldsPanel.add(createLabel(componentText),
                createConstraints(GridBagConstraints.EAST, 0, gridy, 1));
        customProductFieldsPanel.add(createTextField(componentText, true),
                createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        gridy++;

        componentText = "Brand Name";
        customProductFieldsPanel.add(createLabel(componentText),
                createConstraints(GridBagConstraints.EAST, 0, gridy, 1));
        customProductFieldsPanel.add(createTextField(componentText, true),
                createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        gridy++;

        componentText = "Modelling Scale";
        customProductFieldsPanel.add(createLabel(componentText),
                createConstraints(GridBagConstraints.EAST, 0, gridy, 1));
        customProductFieldsPanel.add(createComboBox(componentText,
                        new String[]{"Select Scale", "OO_GAUGE", "TT_GAUGE", "N_GAUGE"},
                        "Select Scale"),
                createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        gridy++;

        componentText = "Retail Price (£)";
        customProductFieldsPanel.add(createLabel(componentText),
                createConstraints(GridBagConstraints.EAST, 0, gridy, 1));
        customProductFieldsPanel.add(createTextField(componentText,
                        true),
                createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        gridy++;

        componentText = "Stock";
        customProductFieldsPanel.add(createLabel(componentText),
                createConstraints(GridBagConstraints.EAST, 0, gridy, 1));
        customProductFieldsPanel.add(createTextField(componentText,
                        true),
                createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        gridy++;

        if (productType == 1 || productType == 2 || productType == 3) {
            // groups all Rolling Stock - add Start/End Era Combo Boxes

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

            componentText = "Start Era";
            customProductFieldsPanel.add(createLabel(componentText),
                    createConstraints(GridBagConstraints.EAST, 0, gridy, 1));

            customProductFieldsPanel.add(createComboBox(componentText,
                            new String[]{"Select Era", era1, era2, era3, era4, era5, era6, era7, era8, era9, era10, era11},
                            "Select Era"),
                    createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
            gridy++;

            componentText = "End Era";
            customProductFieldsPanel.add(createLabel(componentText),
                    createConstraints(GridBagConstraints.EAST, 0, gridy, 1));

            customProductFieldsPanel.add(createComboBox(componentText,
                            new String[]{"Select Era", era1, era2, era3, era4, era5, era6, era7, era8, era9, era10, era11},
                            "Select Era"),
                    createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
            gridy++;

        }

        if (productType == 1) {
            // Locomotive
            componentText = "Locomotive Model";
            customProductFieldsPanel.add(createLabel(componentText),
                    createConstraints(GridBagConstraints.EAST, 0, gridy, 1));
            customProductFieldsPanel.add(createComboBox(componentText,
                            new String[] {"Select Type", "ANALOGUE", "DCC_READY", "DCC_FITTED", "DCC_SOUND"},
                            "Select Type"),
                    createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        } else if (productType == 2) {
            // Carriage
            getTextFieldMap().get("Product Name").setText("N/A");
            getTextFieldMap().get("Product Name").setEnabled(false);

            componentText = "Company Name/BR Standard Mark";
            customProductFieldsPanel.add(createLabel(componentText),
                    createConstraints(GridBagConstraints.EAST, 0, gridy, 1));

            customProductFieldsPanel.add(createComboBox(componentText,
                            new String[] {"Select Company/Standard Mark", "LMS", "LNER", "GWR", "SR", "MARK 1", "MARK 2", "MARK 3", "MARK 4"},
                            "Select Company/Standard Mark"),
                    createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
            gridy++;

            componentText = "Carriage Description";
            customProductFieldsPanel.add(createLabel(componentText),
                    createConstraints(GridBagConstraints.EAST, 0, gridy, 1));

            customProductFieldsPanel.add(createTextField(componentText, true),
                    createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        //} else if (productType == 3) {
            // Wagno - No other fields needed
        } else if (productType == 4) {
            // Track Piece

            componentText = "Radius";
            customProductFieldsPanel.add(createLabel(componentText),
                    createConstraints(GridBagConstraints.EAST, 0, gridy, 1));

            customProductFieldsPanel.add(createComboBox(componentText,
                            new String[] {"Select Radius", "None", "1st Radius", "2nd Radius", "3rd Radius"},
                            "Select Radius"),
                    createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        } else {
            componentText = "Signal Type";
            customProductFieldsPanel.add(createLabel(componentText),
                    createConstraints(GridBagConstraints.EAST, 0, gridy, 1));

            customProductFieldsPanel.add(createComboBox(componentText,
                            new String[] {"Select Signal Type", "ANALOGUE", "DIGITAL"},
                            "Select Signal Type"),
                    createConstraints(GridBagConstraints.WEST, 1, gridy, 1));
        }
        gridy++;

        // Add Cancel, Confirm buttons
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 0;
        constraints.gridy = gridy;
        constraints.gridwidth = 2;


        JButton updateProductButton = new JButton("Confirm");
        JButton cancelButton = new JButton("Cancel");

        updateProductButton.addActionListener(e -> {
            String productCode = getTextFieldMap().get("Product Code").getText().strip(); // Cannot be updated; field disabled
            String brandNameText = getTextFieldMap().get("Brand Name").getText().strip();
            String manufacturerCodeText = getTextFieldMap().get("Manufacturer Name").getText().strip();
            String productNameText = getTextFieldMap().get("Product Name").getText().strip();
            String priceText = getTextFieldMap().get("Retail Price (£)").getText().strip();
            String modellingScaleText = (String) getComboBoxMap().get("Modelling Scale").getSelectedItem();
            String stockText = getTextFieldMap().get("Stock").getText().strip();

            StringBuilder error = new StringBuilder();

            if (productCode.isEmpty()) {
                error.append("Product Code cannot be empty\n");
            }
            if (brandNameText.isEmpty()) {
                error.append("Brand Name cannot be empty\n");
            }
            if (manufacturerCodeText.isEmpty()) {
                error.append("Manufacturer Name cannot be empty\n");
            }
            if (productNameText.isEmpty()) {
                error.append("Product Code cannot be empty\n");
            }
            if (priceText.isEmpty()) {
                error.append("Product Name cannot be empty\n");
            }
            if (stockText.isEmpty()) {
                error.append("Stock cannot be empty\n");
            }

            if (modellingScaleText.equals("Select Scale")) {
                error.append("Please select a value for Modelling Scale\n");
            }

            String trackPieceRegex = "R\\d{3,5}";
            String controllerRegex = "C\\d{3,5}";
            String locomotiveRegex = "L\\d{3,5}";
            String rollingStockRegex = "S\\d{3,5}";

            if (productType == 1 || productType == 2 || productType == 3) {
                String startEraText = (String) getComboBoxMap().get("Start Era").getSelectedItem();
                String endEraText = (String) getComboBoxMap().get("End Era").getSelectedItem();
                assert startEraText != null;
                assert endEraText != null;

                if (startEraText.equals("Select Era")) {
                    error.append("Please select a value for Start Era\n");
                }
                if (endEraText.equals("Select Era")) {
                    error.append("Please select a value for End Era\n");
                }

                if (productType == 1) {
                    String locomotiveModelText = (String) getComboBoxMap().get("Locomotive Model").getSelectedItem();
                    if (locomotiveModelText.equals("Select Type")) {
                        error.append("Please select a value for Locomotive Model\n");
                    }

                    if (!productCode.matches(locomotiveRegex)) {
                        error.append("Locomotive Product Codes must start with an 'L' and be followed by three to five digits\n");
                    }
                } else if (productType == 2) {
                    String companyNameBRStandardMark = (String) getComboBoxMap().get("Company Name/BR Standard Mark").getSelectedItem();
                    if (companyNameBRStandardMark.equals("Select Company/Standard Mark")) {
                        error.append("Please select a value for Company Name/BR Standard Mark\n");
                    }

                    if (getTextFieldMap().get("Carriage Description").getText().strip().isEmpty()) {
                        error.append("Carriage Description cannot be empty");
                    }

                    if (!productCode.matches(rollingStockRegex)) {
                        error.append("Carriage Product Codes must start with an 'S' and be followed by three to five digits\n");
                    }
                } else {
                    if (!productCode.matches(rollingStockRegex)) {
                        error.append("Wagon Product Codes must start with an 'S' and be followed by three to five digits\n");
                    }
                }
            } else if (productType == 4) {
                // Track Piece
                if (!productCode.matches(trackPieceRegex)) {
                    error.append("Track Piece Product Codes must start with an 'R' and be followed by three to five digits\n");
                }

                String radius = (String) getComboBoxMap().get("Radius").getSelectedItem();
                if (radius.equals("Select Radius")) {
                    error.append("Please select a value for Radius\n");
                }
            } else if (productType == 5) {
                // Controller

                if (!productCode.matches(controllerRegex)) {
                    error.append("Controller Product Codes must start with a 'C' and be followed by three to five digits\n");
                }

                String signalType = (String) getComboBoxMap().get("Signal Type").getSelectedItem();
                if (signalType.equals("Select Signal Type")) {
                    error.append("Please select a value for Signal Type\n");
                }
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
                error.append("Stock must be an integer greater than or equal to 0\n");
            }


            if (error.length() > 0) {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), error);
                return;
            }

            Product.SizeRatio modellingScale = Product.SizeRatio.valueOf(modellingScaleText);

            Product newProduct = null;

            if (productType == 1 || productType == 2 || productType == 3) {
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

                if (productType == 1) {
                    // Locomotive
                    Locomotive.LocomotiveModel locomotiveModel = Locomotive.LocomotiveModel.valueOf((String) getComboBoxMap().get("Locomotive Model").getSelectedItem());
                    newProduct = new Locomotive(
                            0,
                            productCode,
                            brandNameText,
                            manufacturerCodeText,
                            productNameText,
                            price,
                            modellingScale,
                            stock,
                            startEra,
                            endEra,
                            locomotiveModel
                    );
                } else if (productType == 2) {
                    // Carriage
                    String carriageType = (String) getComboBoxMap().get("Company Name/BR Standard Mark").getSelectedItem();
                    String carriageDescription = getTextFieldMap().get("Carriage Description").getText().strip();

                    String productName = carriageType + " " + carriageDescription;


                    newProduct = new RollingStock(
                            0,
                            productCode,
                            brandNameText,
                            manufacturerCodeText,
                            productName,
                            price,
                            modellingScale,
                            stock,
                            startEra,
                            endEra
                    );
                } else {
                    // Wagon
                    newProduct = new RollingStock(
                            0,
                            productCode,
                            brandNameText,
                            manufacturerCodeText,
                            productNameText,
                            price,
                            modellingScale,
                            stock,
                            startEra,
                            endEra
                    );
                }

            } else if (productType == 4) {
                // Track Piece
                String radius = (String) getComboBoxMap().get("Radius").getSelectedItem();
                radius = radius.equals("None") ? null : radius;

                String productName = (radius == null) ? productNameText : radius + " " + productNameText;
                newProduct = new TrackPiece(
                        0,
                        productCode,
                        brandNameText,
                        manufacturerCodeText,
                        productName,
                        price,
                        modellingScale,
                        stock,
                        radius
                );
            } else {
                // Controller
                Controller.SignalType signalType = Controller.SignalType.valueOf((String) getComboBoxMap().get("Signal Type").getSelectedItem());
                newProduct = new Controller(
                        0,
                        productCode,
                        brandNameText,
                        manufacturerCodeText,
                        productNameText,
                        price,
                        modellingScale,
                        stock,
                        signalType
                );
            }

            DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
            try {
                handler.openConnection();
                DatabaseOperations operations = new DatabaseOperations();

                boolean res = operations.addNewProduct(getMainFrame().getProducts(), newProduct, handler.getConnection());
                if (res) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Product successfully added");
                    getMainFrame().setProducts(handler.getConnection());
                    getMainFrame().gotoPage("InventoryManagement", new InventoryManagement(getMainFrame()).getInventoryManagementPanel());
                } else {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "An error occurred when adding the product. A product with the entered information already exists.");
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

        customProductFieldsPanel.add(updateProductButton, constraints);
        constraints.gridy++;
        customProductFieldsPanel.add(cancelButton, constraints);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        return label;
    }

    private JTextField createTextField(String key, boolean isEnabled) {
        JTextField textField = new JTextField();
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

    public Map<String, JTextField> getTextFieldMap() {
        return textFieldMap;
    }

    public void setTextFieldMap(Map<String, JTextField> textFieldMap) {
        this.textFieldMap = textFieldMap;
    }

    public Map<String, JComboBox<String>> getComboBoxMap() {
        return comboBoxMap;
    }

    public void setComboBoxMap(Map<String, JComboBox<String>> comboBoxMap) {
        this.comboBoxMap = comboBoxMap;
    }

    private void initComponents() {
        // Main panel with border layout
        addNewProductPanel = new JPanel(new BorderLayout());

        // Center panel with form
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(customProductFieldsPanel, BorderLayout.CENTER);

        addNewProductPanel.add(centerPanel, BorderLayout.CENTER);
    }
}
