package views;

import database.DatabaseConnectionHandler;
import database.DatabaseOperations;
import model.Order;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class OrderHistory extends JPanel {
    private final MainFrame mainFrame;
    private JPanel orderHistoryPanel;
    private JPanel mainOrderPanel;
    private JButton returnToShopButton;
    private JPanel customOrderHistoryJPanel;

    public OrderHistory(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        getMainFrame().setViewOrderDetailsManagement(false);
        getMainFrame().setViewUserDetailsOrderQueue(false);
        getMainFrame().setViewOrderQueueDetailsManagement(false);

        returnToShopButton.addActionListener(e -> {
            getMainFrame().gotoPage("Shop", new Shop(getMainFrame()).getShopPanel());
        });
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public JPanel getOrderHistoryPanel() {
        return orderHistoryPanel;
    }

    private void createUIComponents() {
        this.customOrderHistoryJPanel = new JPanel();
        this.customOrderHistoryJPanel.setLayout(new BoxLayout(customOrderHistoryJPanel, BoxLayout.Y_AXIS));
        // TODO: place custom component creation code here
        DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
        try {
            handler.openConnection();
            DatabaseOperations operations = new DatabaseOperations();

            if (getMainFrame().getCurrentUser() == null) {
                return;
            }

            ArrayList<Order> orders = operations.getOrdersByUserID(getMainFrame().getCurrentUser().getUserID(), handler.getConnection());

            if (orders.size() == 0) {
                this.customOrderHistoryJPanel.add(new JLabel("There are no orders to display."));
                return;
            }

            for (Order order : orders) {
                JPanel singleOrderPanel = new JPanel();
                singleOrderPanel.setLayout(new GridBagLayout());

                JLabel orderSummary = new JLabel("Order #" + order.getOrderNumber() + " | Date: " + order.getDate() + " | Total Cost: £" + order.getTotalCost() + " | Order Status: " + order.getOrderStatus().name());

                JButton orderDetailsButton = new JButton("View Details for Order #" + order.getOrderNumber());
                orderDetailsButton.addActionListener((e) -> {
                    getMainFrame().gotoPage("ViewOrderDetails", new ViewOrderDetails(getMainFrame(), order).getOrderDetailsPanel());
                });

                addJPanelWithConstraints(singleOrderPanel, orderSummary, orderDetailsButton);

                this.customOrderHistoryJPanel.add(singleOrderPanel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            handler.closeConnection();
        }
    }

    static void addJPanelWithConstraints(JPanel singleOrderPanel, JLabel orderSummary, JButton orderDetailsButton) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridx = 0;
        constraints.gridy = 0;
        singleOrderPanel.add(orderSummary, constraints);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 1;
        constraints.gridy = 0;
        singleOrderPanel.add(orderDetailsButton, constraints);
    }
}
