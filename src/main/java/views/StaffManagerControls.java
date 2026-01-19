package views;

import model.User;
import javax.swing.*;

public class StaffManagerControls extends JPanel {
    private JPanel staffManagerControlsPanel;
    private JButton returnToShopButton;
    private JButton orderManagementButton;
    private JButton userManagementButton;
    private JButton inventoryManagementButton;
    private final MainFrame mainFrame;

    public StaffManagerControls(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        returnToShopButton.addActionListener(e -> {
            getMainFrame().gotoPage("Shop", new Shop(getMainFrame()).getShopPanel());
        });
        orderManagementButton.addActionListener(e -> {
            User user = getMainFrame().getCurrentUser();
            if (!user.isStaff() && !user.isManager()) {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), String.format("User %s does not have permission to access this page", user.getEmail()));
                return;
            }

            getMainFrame().gotoPage("OrderManagement", new OrderManagement(getMainFrame()).getOrderManagementPanel());
        });
        inventoryManagementButton.addActionListener(e -> {
            User user = getMainFrame().getCurrentUser();
            if (!user.isStaff() && !user.isManager()) {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), String.format("User %s does not have permission to access this page", user.getEmail()));
                return;
            }

            getMainFrame().gotoPage("InventoryManagement", new InventoryManagement(getMainFrame()).getInventoryManagementPanel());
        });
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public JPanel getStaffManagerControlsPanel() {
        return staffManagerControlsPanel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        this.userManagementButton = new JButton("User Management");
        this.userManagementButton.addActionListener(e -> {
            User user = getMainFrame().getCurrentUser();
            if (!user.isStaff() && !user.isManager()) {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), String.format("User %s does not have permission to access this page", user.getEmail()));
                return;
            }

            getMainFrame().gotoPage("UserManagement", new UserManagement(getMainFrame()).getUserManagementPanel());
        });

        if (!getMainFrame().getCurrentUser().isManager()) {
            this.userManagementButton.setVisible(false);
        }
    }
}
