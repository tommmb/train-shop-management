package views;
import database.DatabaseConnectionHandler;
import database.DatabaseOperations;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class OrderManagement extends JPanel {
    private final MainFrame mainFrame;
    private JPanel orderManagementPanel;
    private JButton returnToShopButton;
    private JPanel mainOrderManagementPanel;
    private JButton viewUserDetailsForButton;
    private JPanel customOrderManagementJPanel;
    private JLabel ordersOrQueueLabel;
    private JButton viewOrdersInQueueButton;

    public OrderManagement(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        getMainFrame().setViewOrderDetailsManagement(true);
        getMainFrame().setViewUserDetailsOrderQueue(false);
        getMainFrame().setViewOrderQueueDetailsManagement(false);

        returnToShopButton.addActionListener(e -> {
            getMainFrame().gotoPage("StaffManagerControls", new StaffManagerControls(getMainFrame()).getStaffManagerControlsPanel());
        });

        viewUserDetailsForButton.addActionListener(e -> {
            String orderIDText = JOptionPane.showInputDialog(JOptionPane.getRootFrame(),
                "To view the details of the user who created an order, please enter the corresponding Order ID."
            );

            if (orderIDText == null) {
                return;
            }

            try {
                int orderID = Integer.parseInt(orderIDText);

                DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
                DatabaseOperations operations = new DatabaseOperations();
                handler.openConnection();

                Order order = operations.getOrderByOrderID(orderID, mainFrame.getProducts(), handler.getConnection());
                if (order == null) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "No Order exists with the given Order ID.");
                    return;
                }

                try {
                    int userID = order.getUserID();
                } catch (NullPointerException ex) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "An error occurred when fetching the user's details.");
                    return;
                }

                User user = operations.getUserByUserID(order.getUserID(), handler.getConnection());

                getMainFrame().gotoPage("ViewUserDetails", new ViewUserDetails(getMainFrame(), user).getUserDetailsPanel());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "The entered Order Number is invalid.");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        viewOrdersInQueueButton.addActionListener(e -> {
            getMainFrame().gotoPage("OrderQueueManagement", new OrderQueueManagement(getMainFrame()).getOrderQueueManagementPanel());
        });
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public JPanel getOrderManagementPanel() {
        return orderManagementPanel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here

            customOrderManagementJPanel = new JPanel();
            customOrderManagementJPanel.setLayout(new BoxLayout(customOrderManagementJPanel, BoxLayout.Y_AXIS));

            DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
            try {
                handler.openConnection();
                DatabaseOperations operations = new DatabaseOperations();

                if (getMainFrame().getCurrentUser() == null) {
                    return;
                }

                ArrayList<Order> orders = operations.getAllOrders(handler.getConnection());

                if (orders.size() == 0) {
                    this.customOrderManagementJPanel.add(new JLabel("There are no orders to display."));
                    return;
                }

                for (Order order : orders) {
                    if (!(order.getOrderStatus().equals(Order.OrderStatus.FULFILLED) || order.getOrderStatus().equals(Order.OrderStatus.CANCELLED))) {
                        return;
                    }

                    JPanel singleOrderPanel = new JPanel();
                    singleOrderPanel.setLayout(new GridBagLayout());

                    JLabel orderSummary = new JLabel(
                            "<html>Order <b>#" + order.getOrderNumber() +
                                    "</b> | Date: <b>" + order.getDate() +
                                    "</b> | Total Cost: <b>£" + order.getTotalCost() +
                                    "</b> | Order Status: <b>" + order.getOrderStatus().name());

                    JButton orderDetailsButton = new JButton("View Detailed Order Information");

                    orderDetailsButton.addActionListener((e) -> {
                        getMainFrame().gotoPage("ViewOrderDetails", new ViewOrderDetails(getMainFrame(), order).getOrderDetailsPanel());
                    });

                    OrderHistory.addJPanelWithConstraints(singleOrderPanel, orderSummary, orderDetailsButton);

                    int gridy = 0;
                    GridBagConstraints constraints = new GridBagConstraints();
                    constraints.anchor = GridBagConstraints.CENTER;
                    constraints.gridx = 0;
                    constraints.gridy = gridy;

                    this.customOrderManagementJPanel.add(singleOrderPanel);
                    gridy++;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                handler.closeConnection();
            }
    }
}
