package views;

import model.Order;
import model.OrderLine;
import model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ViewOrderDetails extends JPanel {
    private JPanel orderDetailsPanel;
    private JPanel mainOrderDetailsPanel;
    private JTable orderTable;
    private JButton returnToPreviousWindowButton;
    private JTable orderLineTable;
    private Order order;
    private final MainFrame mainFrame;
    public ViewOrderDetails(MainFrame mainFrame, Order order) {
        this.mainFrame = mainFrame;
        this.order = order;
    }

    public JPanel getOrderDetailsPanel() {
        return orderDetailsPanel;
    }

    public JPanel getMainOrderDetailsPanel() {
        return mainOrderDetailsPanel;
    }

    public JButton getReturnToOrderHistoryButton() {
        return returnToPreviousWindowButton;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here

        if (getMainFrame().isViewOrderQueueDetailsManagement()) {
            returnToPreviousWindow(new OrderQueueManagement(getMainFrame()).getOrderQueueManagementPanel(), "OrderQueueManagement");
        } else if (getMainFrame().isViewOrderDetailsManagement()) {
            returnToPreviousWindow(new OrderManagement(getMainFrame()).getOrderManagementPanel(), "OrderManagement");
        } else {
            returnToPreviousWindow(new OrderHistory(getMainFrame()).getOrderHistoryPanel(), "OrderHistory");
        }

        String[] orderColumnNames = {"Order #", "Date", "Total Cost", "Order Status"};
        String[] orderLineColumnNames = {"Item Name", "Brand", "Product Code", "Manufacturer", "Quantity", "Single Item Cost", "Total Cost"};
        DefaultTableModel orderTableModel = new DefaultTableModel(orderColumnNames, 0);
        DefaultTableModel orderLineTableModel = new DefaultTableModel(orderLineColumnNames, 0);

        orderTableModel.addRow(new Object[]{
                "Order #", "Date", "Total Cost", "Order Status"
        });

        orderTableModel.addRow(new Object[] {
                order.getOrderNumber(),
                order.getDate(),
                "£" + String.format("%.2f", order.getTotalCost()),
                order.getOrderStatus()
        });

        orderLineTableModel.addRow(new Object[]{
                "Item Name", "Brand", "Code", "Manufacturer", "Quantity", "Single Item Cost", "Total Cost"});

        for (OrderLine orderLine : order.getOrderLineArrayList()) {
            Product product = orderLine.getProduct();
            Object[] row = new Object[]{};

            row = new Object[] {
                    product.getProductName(),
                    product.getBrandName(),
                    product.getProductCode(),
                    product.getManufacturerCode(),
                    orderLine.getQuantity(),
                    "£" + String.format("%.2f", product.getRetailPrice()),
                    "£" + String.format("%.2f", orderLine.getQuantity() * product.getRetailPrice())
            };

            orderLineTableModel.addRow(row);
        }

        this.orderTable = new JTable(orderTableModel);
        this.orderLineTable = new JTable(orderLineTableModel);
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    private void returnToPreviousWindow(JPanel panel, String panelName) {
        returnToPreviousWindowButton = new JButton();
        returnToPreviousWindowButton.addActionListener(e -> {
            try {
                Map<JPanel, String> jPanelStringHashMap = new HashMap<>();
                jPanelStringHashMap.put(panel, panelName);
                getMainFrame().replaceJPanels(jPanelStringHashMap);
                getMainFrame().showCard(panelName);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }
}
