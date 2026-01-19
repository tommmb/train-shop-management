package views;

import database.DatabaseConnectionHandler;
import database.DatabaseOperations;
import model.NoOrderExistsException;
import model.Order;
import model.User;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class OrderQueueManagement extends JPanel {
    private JPanel orderQueueManagementPanel;
    private JButton returnToOrderManagementButton;
    private JButton viewUserDetailsForButton;
    private JButton fulfillFirstOrderInButton;
    private JButton cancelFirstOrderInButton;
    private JPanel mainOrderQueueManagementPanel;
    private JPanel customOrderQueueManagementJPanel;
    private final MainFrame mainFrame;
    public OrderQueueManagement(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        getMainFrame().setViewOrderQueueDetailsManagement(true);
        getMainFrame().setViewOrderDetailsManagement(false);
        getMainFrame().setViewUserDetailsOrderQueue(true);


        fulfillFirstOrderInButton.addActionListener(e -> {
            // Fulfill Order
            DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
            DatabaseOperations operations = new DatabaseOperations();

            try {
                handler.openConnection();
                ArrayList<Order> orders = operations.getAllOrdersInQueue(handler.getConnection());
                int orderID = orders.get(0).getOrderNumber();

                Order order = operations.getOrderByOrderID(orderID, operations.getAllProducts(handler.getConnection()), handler.getConnection());
                if (operations.getUserByUserID(order.getUserID(), handler.getConnection()).getPaymentMethod() == null) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Order #" + orderID + " cannot be fulfilled. The user has no payment details." );
                    return;
                }

                boolean orderConfirmed = operations.fulfillOrder(orderID, handler.getConnection());

                if (orderConfirmed) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Order #" + orderID + " has been fulfilled successfully.");

                    getMainFrame().setProducts(handler.getConnection());

                    getMainFrame().gotoPage("OrderQueueManagement", new OrderQueueManagement(getMainFrame()).getOrderQueueManagementPanel());
                } else {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Order #" + orderID + " cannot be fulfilled. It may be pending, cancelled, or an order may not exist with the specified Order Number.");
                }

            } catch (SQLException | NoOrderExistsException ex) {
                ex.printStackTrace();
            }
            finally {
                handler.closeConnection();
            }
        });
        cancelFirstOrderInButton.addActionListener(e -> {
            DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
            DatabaseOperations operations = new DatabaseOperations();

            try {
                handler.openConnection();
                ArrayList<Order> orders = operations.getAllOrdersInQueue(handler.getConnection());
                int orderID = orders.get(0).getOrderNumber();

                String cancellationReason = JOptionPane.showInputDialog(JOptionPane.getRootFrame(),
                        "<html>Please enter the reason for cancellation. <br>Press OK to confirm cancellation.</html>"
                );
                if (cancellationReason == null) {
                    return;
                } else {
                    cancellationReason = "No reason given.";
                }

                boolean orderConfirmed = operations.cancelOrder(orderID, cancellationReason, handler.getConnection());

                if (orderConfirmed) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Order #" + orderID + " has been cancelled successfully.");

                    getMainFrame().gotoPage("OrderQueueManagement", new OrderQueueManagement(getMainFrame()).getOrderQueueManagementPanel());
                } else {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Order #" + orderID + " cannot be cancelled. It may already have been cancelled, or an order may not exist with the specified Order Number.");
                }

            } catch (SQLException | NoOrderExistsException ex) {
                ex.printStackTrace();
            }
        });
        returnToOrderManagementButton.addActionListener(e -> {
            getMainFrame().gotoPage("OrderManagement", new OrderManagement(getMainFrame()).getOrderManagementPanel());
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
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public JPanel getOrderQueueManagementPanel() {
        return orderQueueManagementPanel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here

        customOrderQueueManagementJPanel = new JPanel();
        customOrderQueueManagementJPanel.setLayout(new BoxLayout(customOrderQueueManagementJPanel, BoxLayout.Y_AXIS));

        DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
        try {
            handler.openConnection();
            DatabaseOperations operations = new DatabaseOperations();

            if (getMainFrame().getCurrentUser() == null) {
                return;
            }

            ArrayList<Order> orders = operations.getAllOrdersInQueue(handler.getConnection());

            if (orders.size() == 0) {
                this.customOrderQueueManagementJPanel.add(new JLabel("There are no orders to display."));
                return;
            }

            for (Order order : orders) {
                JPanel singleOrderPanel = new JPanel();
                singleOrderPanel.setLayout(new GridBagLayout());


                JLabel orderSummary = new JLabel(
                        "<html>Order <b>#" + order.getOrderNumber() +
                                "</b> | Date: <b>" + order.getDate() +
                                "</b> | Total Cost: <b>£" + order.getTotalCost() +
                                "</b> | Has Payment Method: <b>" + (operations.getUserByUserID(order.getUserID(), handler.getConnection()).getPaymentMethod() == null ? "No" : "Yes") + "</b></html>");

                JButton orderDetailsButton = new JButton("View Detailed Order Information");

                orderDetailsButton.addActionListener((e) -> {
                    getMainFrame().gotoPage("ViewOrderDetails", new ViewOrderDetails(getMainFrame(), order).getOrderDetailsPanel());
                });

                OrderHistory.addJPanelWithConstraints(singleOrderPanel, orderSummary, orderDetailsButton);

                this.customOrderQueueManagementJPanel.add(singleOrderPanel);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            handler.closeConnection();
        }
    }
}
