package views;

import model.Role;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;

public class ViewUserDetails extends JPanel {
    private JPanel userDetailsPanel;
    private JButton returnToOrderManagementButton;
    private final MainFrame mainFrame;
    private JTable userTable;
    private JTable userAddressTable;
    private User user;

    public ViewUserDetails(MainFrame mainFrame, User user) {
        this.mainFrame = mainFrame;
        this.user = user;

        if (getMainFrame().isViewUserDetailsOrderQueue()) {
            returnToOrderManagementButton.addActionListener(e -> {
                getMainFrame().gotoPage("OrderQueueManagement", new OrderQueueManagement(getMainFrame()).getOrderQueueManagementPanel());
            });
        } else {
            returnToOrderManagementButton.addActionListener(e -> {
                getMainFrame().gotoPage("OrderManagement", new OrderManagement(getMainFrame()).getOrderManagementPanel());
            });
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here

        // int userID,
        // String forename,
        // String surname,
        // String email,
        // String password,
        // boolean isCustomer,
        // boolean isStaff,
        // boolean isManager,
        // Address address,
        // ArrayList<Role> userRoles,
        // PaymentMethod paymentMethod

        String[] userColumnNames = {"User ID", "Email", "First Name", "Last Name", "Roles", "Has Payment Method"};
        String[] userAddressColumnNames = {"Postcode", "House Number", "Street", "City", "County"};
        DefaultTableModel userTableModel = new DefaultTableModel(userColumnNames, 0);
        DefaultTableModel userAddressTableModel = new DefaultTableModel(userAddressColumnNames, 0);

        userTableModel.addRow(new Object[]{
                "User ID", "Email", "First Name", "Lat Name", "Roles", "Has Payment Method"
        });
        userAddressTableModel.addRow(new Object[] {
                "Postcode", "House Number", "Street", "City", "County"
        });

        StringBuilder roles = new StringBuilder();
        ArrayList<Role> userRoles = user.getUserRoles();
        for (Role role : userRoles) {
            roles.append(role.getRoleName());
            if (userRoles.indexOf(role) != (userRoles.size() - 1)) {
                roles.append(", ");
            }
        }



        Object[] personalInformation = new Object[] {
                user.getUserID(),
                user.getEmail(),
                user.getForename(),
                user.getSurname(),
                roles,
                user.getPaymentMethod() == null ? "No" : "Yes"
        };

        Object[] addressInformation = new Object[] {
                user.getAddress().getPostcode(),
                user.getAddress().getHouseNumber(),
                user.getAddress().getRoadName(),
                user.getAddress().getCityName(),
                user.getAddress().getCounty()
        };

        userTableModel.addRow(personalInformation);
        userAddressTableModel.addRow(addressInformation);

        this.userTable = new JTable(userTableModel);
        this.userAddressTable = new JTable(userAddressTableModel);
    }

    public JPanel getUserDetailsPanel() {
        return userDetailsPanel;
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public User getUser() {
        return user;
    }
}
