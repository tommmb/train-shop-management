package views;

import database.DatabaseConnectionHandler;
import database.DatabaseOperations;
import model.Role;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserManagement extends JPanel {
    private final MainFrame mainFrame;
    private JPanel userManagementPanel;
    private JTable usersTable;
    private JButton appointNewStaffButton;
    private JButton dismissStaffButton;
    private JButton returnToShopButton;

    public UserManagement(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        //TODO use Manager setStaff method and check user is manager
        appointNewStaffButton.addActionListener(e -> {
            DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
            try {
                handler.openConnection();
                DatabaseOperations operations = new DatabaseOperations();
                String userEmail = JOptionPane.showInputDialog(JOptionPane.getRootFrame(), "Enter the email address of the account you wish to assign as staff");

                if (userEmail == null) {
                    return;
                }

                if (!Register.isValidEmail(userEmail)) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "The entered email is invalid");
                    return;
                }
                User user = operations.getUserByEmail(userEmail, handler.getConnection());

                if (user == null) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "No user exists with the given email");
                    return;
                }

                boolean success = operations.appointStaff(user.getUserID(), handler.getConnection());
                if (success) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Successfully appointed the specified user as staff");


                    getMainFrame().gotoPage("UserManagement", new UserManagement(getMainFrame()).getUserManagementPanel());
                } else {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "The specified user is already staff");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                handler.closeConnection();
            }
        });
        //TODO use Manager removeStaff method and check user is manager
        dismissStaffButton.addActionListener(e -> {
            DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
            try {
                handler.openConnection();
                DatabaseOperations operations = new DatabaseOperations();
                String userEmail = JOptionPane.showInputDialog(JOptionPane.getRootFrame(), "Enter the email address of the account from which you wish to remove staff privileges");

                if (userEmail == null) {
                    return;
                }

                if (!Register.isValidEmail(userEmail)) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "The entered email is invalid");
                    return;
                }
                User user = operations.getUserByEmail(userEmail, handler.getConnection());

                if (user == null) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), String.format("No user exists with the email '%s'", userEmail));
                    return;
                }
                boolean success = operations.dismissStaff(user.getUserID(), handler.getConnection());
                if (success) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Successfully removed the specified user's staff privileges");

                    getMainFrame().gotoPage("UserManagement", new UserManagement(getMainFrame()).getUserManagementPanel());
                } else {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "The specified user is not staff");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                handler.closeConnection();
            }
        });
        returnToShopButton.addActionListener(e -> {
            getMainFrame().gotoPage("StaffManagerControls", new StaffManagerControls(getMainFrame()).getStaffManagerControlsPanel());
        });
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public JPanel getUserManagementPanel() {
        return userManagementPanel;
    }

    private void createUIComponents()  {
        // TODO: place custom component creation code here
        DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
        try {
            handler.openConnection();
            DatabaseOperations operations = new DatabaseOperations();

            ArrayList<User> users = operations.getAllUsers(handler.getConnection());

            String[] columnNames = {"User ID", "First Name", "Last Name", "Postcode", "House Number", "Email", "Roles"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
            tableModel.addRow(new Object[]{
                    "User ID", "First Name", "Last Name", "Postcode", "House Number", "Email", "Roles"
            });

            for (User user : users) {
                StringBuilder roles = new StringBuilder();
                ArrayList<Role> userRoles = user.getUserRoles();
                for (Role role : userRoles) {
                    roles.append(role.getRoleName());
                    if (userRoles.indexOf(role) != (userRoles.size() - 1)) {
                        roles.append(", ");
                    }
                }

                Object[] row = new Object[] {
                        user.getUserID(),
                        user.getForename(),
                        user.getSurname(),
                        user.getAddress().getPostcode(),
                        user.getAddress().getHouseNumber(),
                        user.getEmail(),
                        roles
                };
                tableModel.addRow(row);
            }

            usersTable = new JTable(tableModel);
            usersTable.setEnabled(false);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            handler.closeConnection();
        }
    }
}
