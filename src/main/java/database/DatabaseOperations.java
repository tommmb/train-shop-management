package database;

import model.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseOperations {
    /**
     * This function fetches the stored data about a user from a given email and
     * returns
     * a populated User object
     *
     * @param givenEmail - the email of the user
     * @param connection - the database connection
     * @return - The User object containing the data of the User
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    public User getUserByEmail(String givenEmail, Connection connection) throws SQLException {
        String sql = "SELECT * FROM Users WHERE email = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, givenEmail);
        ResultSet res = preparedStatement.executeQuery();

        return getUserByUserIdPart(res, connection);
    }

    /**
     * This function fetches the stored data about a user from a given user ID
     * and returns a populated User object
     *
     * @param givenUserID - the ID of the user
     * @param connection  - the database connection
     * @return - The User object containing the data of the User
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    public User getUserByUserID(int givenUserID, Connection connection) throws SQLException {
        String sql = "SELECT * FROM Users WHERE user_id = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, givenUserID);
        ResultSet res = preparedStatement.executeQuery();

        return getUserByUserIdPart(res, connection);
    }

    /**
     * This function returns a User object given a ResultSet from another SQL Query
     *
     * @param res        - the ResultSet
     * @param connection - the database connection
     * @return - The User object containing the data of the User
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    private User getUserByUserIdPart(ResultSet res, Connection connection) throws SQLException {
        if (res.next()) {
            // retrieve the parameters from the ResultSet
            int userID = res.getInt("user_id");
            String forename = res.getString("forename");
            String surname = res.getString("surname");
            String email = res.getString("email");
            String password = res.getString("password");
            String salt = res.getString("salt");
            String postcode = res.getString("postcode");
            int houseNumber = res.getInt("house_number");
            String cardNumber = res.getString("card_number");

            // Check if the user has an address
            String sql = "SELECT * FROM UserAddresses WHERE postcode = ? AND house_number = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, postcode);
            preparedStatement.setInt(2, houseNumber);

            res = preparedStatement.executeQuery();

            if (res.next()) {
                postcode = res.getString("postcode");
                houseNumber = res.getInt("house_number");
                String county = res.getString("county");
                String city = res.getString("city");
                String roadName = res.getString("road_name");

                Address address = new Address(postcode, houseNumber, roadName, city, county);

                boolean isCustomer = false;
                boolean isStaff = false;
                boolean isManager = false;

                ArrayList<Role> userRoles = new ArrayList<>();

                // Check if the user has respective roles in the UserRoles table
                sql = "SELECT " +
                        "Roles.role_name, " +
                        "Roles.role_id " +
                        "FROM UserRoles " +
                        "JOIN Roles ON UserRoles.role_id = Roles.role_id " +
                        "WHERE UserRoles.user_id = ?;";

                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, userID);

                res = preparedStatement.executeQuery();

                while (res.next()) {
                    String roleName = res.getString("role_name");
                    int roleID = res.getInt("role_id");
                    userRoles.add(new Role(roleID, roleName));
                }

                // Check if the user has stored banking information
                sql = "SELECT * from BankDetails WHERE card_number = ?";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, cardNumber);

                ResultSet userPaymentDetailsQuery = preparedStatement.executeQuery();

                PaymentMethod paymentMethod = null;
                if (userPaymentDetailsQuery.next()) {
                    // Retrieve the parameters from the query and build the PaymentMethod object
                    String cvc = userPaymentDetailsQuery.getString("cvc");
                    String expiryDate = userPaymentDetailsQuery.getString("expiry_date");
                    String cardholderName = userPaymentDetailsQuery.getString("cardholder_name");
                    String bankCardName = userPaymentDetailsQuery.getString("bank_card_name");

                    paymentMethod = new PaymentMethod(cardNumber, cvc, expiryDate, cardholderName, bankCardName);

                }

                if (userRoles.size() >= 1) {
                    return new User(userID, forename, surname, email, password, salt, isCustomer, isStaff, isManager,
                            address, userRoles, paymentMethod);
                }
            }
        }
        return null;
    }

    /**
     * This function changes the stored email for a given user
     *
     * @param givenUserID - the ID of the user
     * @param newEmail    - the new email to change the user's email to
     * @param connection  - the connection to the database
     * @return - True if the chances was successful; false otherwise
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    public boolean changeUserEmail(int givenUserID, String newEmail, Connection connection) throws SQLException {
        String sql = "SELECT * FROM Users WHERE email = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, newEmail);

        ResultSet emailExistsQuery = preparedStatement.executeQuery();

        if (emailExistsQuery.next()) {
            return emailExistsQuery.getInt("email") == givenUserID;
        }

        try {
            // SQL command to update the user's email
            sql = "UPDATE Users SET email = ? WHERE user_id = ?;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newEmail);
            preparedStatement.setInt(2, givenUserID);
            preparedStatement.execute();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * This function updates the stored password for a user
     *
     * @param user        - the User object of which the password will be changed
     * @param oldPassword - the old password of the user
     * @param newPassword - the password to be changed to
     * @param connection  - the connection to the database
     * @return True if password change successful; false otherwise
     */
    public boolean changeUserPassword(User user, String oldPassword, String newPassword, Connection connection) {
        if (!user.passwordsMatch(oldPassword)) {
            return false;
        }

        byte[] hash;
        String hashString = "";
        try {
            // Hash the password using the SHA hashing algorithm
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(user.getSalt().getBytes());
            hash = md.digest(newPassword.getBytes());
            hashString = HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException er) {
            er.printStackTrace();
            return false;
        }

        try {
            // SQL command to update the user's password
            String sql = "UPDATE Users SET password = ? WHERE user_id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, hashString);
            preparedStatement.setInt(2, user.getUserID());
            preparedStatement.execute();
            preparedStatement.close();

            user.setPassword(hashString);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * This function checks the entered credentials against those stored for that
     * user and returns true if they match
     *
     * @param email      - the entered email at Login
     * @param password   - the entered password at Login
     * @param connection - the connection to the database
     * @return True if sign-in successful; false otherwise
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    public boolean signIn(String email, String password, Connection connection) throws SQLException {
        // Query to check if a user exists with the entered email
        String sql = "SELECT email, password, salt FROM Users WHERE email = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, email);

        ResultSet userQuery = preparedStatement.executeQuery();
        if (userQuery.next()) {

            // Retrieve the stored salt of the user in order to hash the entered password
            String existingPassword = userQuery.getString("password");
            String salt = userQuery.getString("salt");

            String hashString;

            try {
                // Hash the entered password
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(salt.getBytes());
                byte[] hash = md.digest(password.getBytes());
                hashString = HexFormat.of().formatHex(hash);

            } catch (NoSuchAlgorithmException er) {
                er.printStackTrace();
                return false;
            }

            userQuery.close();
            // If the hashes are equal, sign in successful
            return (hashString.equals(existingPassword));
        }

        userQuery.close();

        return false;
    }

    /**
     * This function creates a new Customer record and inserts it into the database
     *
     * @param forename   - the entered forename at Register
     * @param surname    - the entered surname at Regsiter
     * @param email      - the entered email at Register
     * @param password   - the entered passwrod at Register
     * @param salt       - the unique salt generated when a user registers
     * @param address    - the address object built from the entered address fields
     *                   at Regsiter
     * @param connection - the connection to the database
     * @return True if register successful; false otherwise
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    public boolean register(String forename, String surname, String email, String password, String salt,
            Address address, Connection connection) throws SQLException {
        // Check if an address exists for the entered postcode and house number
        String sql = "SELECT postcode, house_number FROM UserAddresses WHERE postcode = ? and house_number = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, address.getPostcode());
        preparedStatement.setInt(2, address.getHouseNumber());

        Statement statement = connection.createStatement();
        ResultSet addressQuery = preparedStatement.executeQuery();

        if (!addressQuery.next()) {
            // create the new address
            sql = "INSERT INTO UserAddresses (postcode, house_number, county, city, road_name) " +
                    "VALUES (?, ?, ?, ?, ?);";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, address.getPostcode());
            preparedStatement.setInt(2, address.getHouseNumber());
            preparedStatement.setString(3, address.getCounty());
            preparedStatement.setString(4, address.getCityName());
            preparedStatement.setString(5, address.getRoadName());
            preparedStatement.execute();

            addressQuery.close();
        }

        sql = "SELECT * FROM Users WHERE email = ?;";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, email);
        ResultSet emailQuery = preparedStatement.executeQuery();

        if (!emailQuery.next()) {
            emailQuery.close();

            // create the new user
            sql = "INSERT INTO Users (forename, surname, password, salt, postcode, house_number, email) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?);";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, forename);
            preparedStatement.setString(2, surname);
            preparedStatement.setString(3, password);
            preparedStatement.setString(4, salt);
            preparedStatement.setString(5, address.getPostcode());
            preparedStatement.setInt(6, address.getHouseNumber());
            preparedStatement.setString(7, email);
            preparedStatement.execute();

            // Hash the password with the salt generated at Register
            byte[] saltAES = new byte[16];
            new SecureRandom().nextBytes(saltAES);
            String saltAESEncoded = Base64.getEncoder().encodeToString(saltAES);
            sql = "UPDATE Users SET salt_aes = ? WHERE email = ?;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, saltAESEncoded);
            preparedStatement.setString(2, email);
            preparedStatement.execute();

            sql = "SELECT user_id FROM Users WHERE email = ?;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, email);

            ResultSet userIdQuery = preparedStatement.executeQuery();

            if (userIdQuery.next()) {
                int userID = userIdQuery.getInt("user_id");
                userIdQuery.close();

                // get the Role id of the CUSTOMER role if it exists
                ResultSet customerRoleIdQuery = statement.executeQuery(
                        "SELECT role_id FROM Roles WHERE role_name = 'CUSTOMER';");

                if (customerRoleIdQuery.next()) {
                    userIdQuery.close();
                    int roleID = customerRoleIdQuery.getInt("role_id");
                    customerRoleIdQuery.close();

                    sql = "INSERT INTO UserRoles (user_id, role_id) " +
                            "VALUES (?, ?);";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setInt(1, userID);
                    preparedStatement.setInt(2, roleID);
                    preparedStatement.execute();

                    return true;
                }
            }

        }

        statement.close();
        preparedStatement.close();

        return false;
    }

    /**
     * This function gives a specific user staff privileges
     *
     * @param userID     - the id of the user to appoint staff
     * @param connection - the connection to the database
     * @return True if appointed; false otherwise
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    public boolean appointStaff(int userID, Connection connection) throws SQLException {
        // TODO - Verify that the user that calls this is a MANAGER
        Statement statement = connection.createStatement();

        // Get STAFF role ID

        ResultSet staffRoleIDQuery = statement.executeQuery(
                "SELECT role_id FROM Roles WHERE role_name = 'STAFF';");

        if (!staffRoleIDQuery.next()) {
            return false;
        }

        int staffRoleID = staffRoleIDQuery.getInt("role_id");

        String sql = "SELECT role_id FROM UserRoles WHERE user_id = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, userID);

        ResultSet userRolesQuery = preparedStatement.executeQuery();

        boolean isStaff = false;
        // Check if the user is already STAFF with the existing records in the UserRoles
        // table
        while (userRolesQuery.next()) {
            if (userRolesQuery.getInt("role_id") == staffRoleID) {
                isStaff = true;
                break;
            }
        }
        userRolesQuery.close();

        if (isStaff) {
            return false;
        }

        // The user is not staff therefore add a record to UserRoels indicating that the
        // specified user is now staff
        sql = "INSERT INTO UserRoles (user_id, role_id) VALUES (? ,?);";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, userID);
        preparedStatement.setInt(2, staffRoleID);
        preparedStatement.execute();

        return preparedStatement.getUpdateCount() > 0;
    }

    /**
     * This function removes a user's staff permissions given they are a staff
     *
     * @param userID     - the id of the user to remove staff permissions
     * @param connection - the connection to the database
     * @return True if successfully dismissed; false otherwise
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    public boolean dismissStaff(int userID, Connection connection) throws SQLException {
        Statement statement = connection.createStatement();

        // Get STAFF role ID
        ResultSet staffRoleIDQuery = statement.executeQuery(
                "SELECT role_id FROM Roles WHERE role_name = 'STAFF';");

        if (!staffRoleIDQuery.next()) {
            return false;
        }

        int staffRoleID = staffRoleIDQuery.getInt("role_id");

        // Check if the user has the staff role
        String sql = "SELECT role_id FROM UserRoles WHERE user_id = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, userID);

        ResultSet userRolesQuery = preparedStatement.executeQuery();

        boolean isStaff = false;

        // If the user is staff, update the isStaff variable
        while (userRolesQuery.next()) {
            if (userRolesQuery.getInt("role_id") == staffRoleID) {
                isStaff = true;
                break;
            }
        }
        userRolesQuery.close();

        if (!isStaff) {
            return false;
        }

        // Delete the staff entry for the specified user in the UserRoles table
        sql = "DELETE FROM UserRoles WHERE user_id = ? AND role_id = ?;";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, userID);
        preparedStatement.setInt(2, staffRoleID);
        preparedStatement.execute();

        // return true if more than 0 rows were updated; false otherwise
        return preparedStatement.getUpdateCount() > 0;
    }

    /**
     * This function updates a user's stored payment method in the database
     *
     * @param userID           - the id of the user for the bank details to be
     *                         updated
     * @param newPaymentMethod - the PaymentMethod object containing bank details
     * @param connection       the connection to the database
     * @return True if successfully updated; false otherwise
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    public boolean updateCardDetails(int userID, PaymentMethod newPaymentMethod, Connection connection)
            throws SQLException {
        // Check if the user's bank details are already in the system.
        // if they are equal to the user's current bank details, return true
        // if they are DIFFERENT to the user's current details, return true

        // Check that the user exists
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, userID);
        ResultSet userQuery = preparedStatement.executeQuery();

        if (!userQuery.next()) {
            userQuery.close();
            return false;
        }

        // Check if the information for that card number is already stored
        boolean newPaymentMethodExists = false;
        sql = "SELECT * FROM BankDetails WHERE card_number = ?";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, newPaymentMethod.getCardNumber());
        ResultSet newBankDetailsQuery = preparedStatement.executeQuery();
        if (newBankDetailsQuery.next()) {
            newPaymentMethodExists = true;
        }

        if (!newPaymentMethodExists) {
            // if not stored, insert a new record with the bank details
            sql = "INSERT INTO BankDetails (card_number, CVC, expiry_date, cardholder_name, bank_card_name) VALUES (?, ?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newPaymentMethod.getCardNumber());
            preparedStatement.setString(2, newPaymentMethod.getCVC());
            preparedStatement.setString(3, newPaymentMethod.getExpiryDate());
            preparedStatement.setString(4, newPaymentMethod.getCardHolderName());
            preparedStatement.setString(5, newPaymentMethod.getBankCardName());
            preparedStatement.execute();
        } else {
            // if they are stored, update the existing record with the new bank details
            sql = "UPDATE BankDetails SET cvc = ?, expiry_date = ?, cardholder_name = ?, bank_card_name = ? WHERE card_number = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newPaymentMethod.getCVC());
            preparedStatement.setString(2, newPaymentMethod.getExpiryDate());
            preparedStatement.setString(3, newPaymentMethod.getCardHolderName());
            preparedStatement.setString(4, newPaymentMethod.getCardNumber());
            preparedStatement.setString(5, newPaymentMethod.getBankCardName());
            preparedStatement.execute();
        }

        // finally, update the foreign key in the Users table for their stored card
        // number
        sql = "UPDATE Users SET card_number = ? WHERE user_id = ?";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, newPaymentMethod.getCardNumber());
        preparedStatement.setInt(2, userID);
        preparedStatement.execute();

        return true;
    }

    /**
     * This function updates a user's stored address details in the database
     *
     * @param userID     - the ID of the user for the address to be updated
     * @param oldAddress - the user's old address
     * @param newAddress - the user's new address
     * @param connection - the database connection
     * @return True if successfully updated; false otherwise
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    public boolean updateAddressDetails(int userID, Address oldAddress, Address newAddress, Connection connection)
            throws SQLException {
        String sql = "SELECT * FROM Users WHERE user_id = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, userID);

        // Check that the user exists
        ResultSet userQuery = preparedStatement.executeQuery();

        if (!userQuery.next()) {
            userQuery.close();
            return false;
        }

        boolean newAddressExists = false;

        // check if the address already exists
        sql = "SELECT * FROM UserAddresses WHERE postcode = ? AND house_number = ?;";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, newAddress.getPostcode());
        preparedStatement.setInt(2, newAddress.getHouseNumber());

        ResultSet addressQuery = preparedStatement.executeQuery();

        if (addressQuery.next()) {
            newAddressExists = true;
        }

        if (newAddressExists) {
            // address exists, update the existing stored address

            sql = "UPDATE UserAddresses " +
                    "SET county = ?, " +
                    "city = ?, " +
                    "road_name = ? " +
                    "WHERE postcode = ? " +
                    "and house_number = ?;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newAddress.getCounty());
            preparedStatement.setString(2, newAddress.getCityName());
            preparedStatement.setString(3, newAddress.getRoadName());
            preparedStatement.setString(4, newAddress.getPostcode());
            preparedStatement.setInt(5, newAddress.getHouseNumber());
            preparedStatement.execute();

        } else {
            // new address; Insert new address into the database

            sql = "INSERT INTO UserAddresses (postcode, house_number, county, city, road_name) VALUES (?, ?, ?, ?, ?);";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newAddress.getPostcode());
            preparedStatement.setInt(2, newAddress.getHouseNumber());
            preparedStatement.setString(3, newAddress.getCounty());
            preparedStatement.setString(4, newAddress.getCityName());
            preparedStatement.setString(5, newAddress.getRoadName());
            preparedStatement.execute();
        }

        // update the foreign keys in the Users table to represent their new address
        sql = "UPDATE Users SET postcode = ?, house_number = ? WHERE user_id = ?;";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, newAddress.getPostcode());
        preparedStatement.setInt(2, newAddress.getHouseNumber());
        preparedStatement.setInt(3, userID);
        preparedStatement.execute();

        return true;
    }

    /**
     * This function deletes a user and their relevant data from the database
     * excluding orders
     *
     * @param userID     - the id of the user to delete
     * @param connection - database connection
     * @return True if succesfully deleted; false otherwise
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    public boolean deleteAccount(int userID, Connection connection) throws SQLException {
        // Check if the user exists
        // postcode , house_number - UserAddress (postcode , house_number)
        // card_number - BankDetails (card_number)
        // SQL join to get the number of users with FK UserAddress (postcode,
        // house_number)
        // SQL join to get the number of users with FK BankDetails (card_number)
        // Delete from UserRoles first
        // Delete from Users table first
        // THEN, delete from UserAddresses and BankDetails

        String sql = "SELECT postcode, house_number, card_number FROM Users WHERE user_id = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, userID);

        ResultSet userInfoQuery = preparedStatement.executeQuery();

        if (!userInfoQuery.next()) {
            return false;
        }

        String userPostcode = userInfoQuery.getString("postcode");
        int userHouseNumber = userInfoQuery.getInt("house_number");
        String userCardNumber = userInfoQuery.getString("card_number");
        userInfoQuery.close();

        // Delete the User from the UserRoles table
        sql = "DELETE FROM UserRoles WHERE user_id = ?;";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, userID);
        preparedStatement.execute();

        // Delete the User from the Users table
        sql = "DELETE FROM Users WHERE user_id = ?;";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, userID);
        preparedStatement.execute();

        // Delete the address from UserAddresses, if no other users have that address
        sql = "DELETE FROM UserAddresses " +
                "WHERE postcode = ? AND house_number = ? " +
                "AND NOT EXISTS (" +
                "SELECT * FROM Users WHERE postcode = ? AND house_number = ?);";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, userPostcode);
        preparedStatement.setInt(2, userHouseNumber);
        preparedStatement.setString(3, userPostcode);
        preparedStatement.setInt(4, userHouseNumber);
        preparedStatement.execute();

        // Delete the bank details from BankDetails, if no other users have those bank
        // details
        sql = "DELETE FROM BankDetails " +
                "WHERE card_number = ? " +
                "AND NOT EXISTS (" +
                "SELECT * FROM Users WHERE card_number = ?);";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, userCardNumber);
        preparedStatement.setString(2, userCardNumber);
        preparedStatement.execute();

        return true;
    }

    /**
     * This function returns a list of User objects, of all stored Users in the
     * database
     *
     * @param connection
     * @return
     * @throws SQLException
     */
    public ArrayList<User> getAllUsers(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet usersQuery = statement.executeQuery("SELECT * FROM Users");
        ArrayList<User> users = new ArrayList<>();
        ArrayList<String> userEmails = new ArrayList<>();

        while (usersQuery.next()) {
            String email = usersQuery.getString("email");
            userEmails.add(email);
        }
        usersQuery.close();

        for (String email : userEmails) {
            users.add(getUserByEmail(email, connection));
        }

        return users;
    }

    /**
     * This function returns the stored Pending order for a specific user
     *
     * @param userID
     * @param connection
     * @return an Order object
     * @throws SQLException
     */
    public Order getPendingOrderByUserID(int userID, Connection connection) throws SQLException {
        Statement statement = connection.createStatement();

        // query the Orders table for the PENDING order for that user
        String sql = "SELECT * FROM Orders WHERE user_id = ? AND status = 'PENDING';";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, userID);

        // int orderNumber, int userID, String date, double totalCost, OrderStatus
        // orderStatus
        ResultSet pendingOrderQuery = preparedStatement.executeQuery();
        if (pendingOrderQuery.next()) {
            // Create new Order object and return it
            return new Order(
                    pendingOrderQuery.getInt("order_id"),
                    userID,
                    pendingOrderQuery.getString("order_date"),
                    pendingOrderQuery.getDouble("total_cost"),
                    Order.OrderStatus.valueOf(pendingOrderQuery.getString("status")));
        }
        return null;
    }

    /**
     * This function returns a list of all Orders for a given userID
     *
     * @param userID     - the ID of the user
     * @param connection - the database connection
     * @return an ArrayList<Order>
     * @throws SQLException
     */
    public ArrayList<Order> getOrdersByUserID(int userID, Connection connection) throws SQLException {
        // Query to fetch all relevant data from multiple tables to create the Order
        // object
        String sql = "SELECT " +
                "Orders.order_id, " +
                "Orders.order_date, " +
                "Orders.status, " +
                "Products.product_id, " +
                "Products.product_name, " +
                "Products.brand_name, " +
                "Products.product_code, " +
                "Products.price, " +
                "Products.stock, " +
                "Products.size_ratio, " +
                "Products.manufacturer_code, " +
                "OrderLine.quantity, " +
                "Orders.total_cost " +
                "FROM Orders " +
                "JOIN OrderLine ON Orders.order_id = OrderLine.order_id " +
                "JOIN Products ON OrderLine.product_id = Products.product_id " +
                "WHERE Orders.user_id = ? " +
                "ORDER BY Orders.order_id;";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, userID);
        ResultSet orders = preparedStatement.executeQuery();

        Map<Integer, Order> orderMap = new LinkedHashMap<>();

        // Iterate over the ResultSet, create the relevant Order object and add it to
        // the orderMap
        while (orders.next()) {
            int orderID = orders.getInt("order_id");
            Order order = orderMap.get(orderID);

            if (order == null) {
                Date orderDate = orders.getDate("order_date");
                String status = orders.getString("status");
                double totalCost = orders.getDouble("total_cost");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String dateString = dateFormat.format(orderDate);

                order = new Order(orderID, userID, dateString, totalCost, Order.OrderStatus.valueOf(status));
                order.setTotalCost();
                orderMap.put(orderID, order);
            }

            addOrderLine(orders, order);
        }

        orders.close();

        return new ArrayList<>(orderMap.values());
    }

    /**
     * This function inserts a record into the OrderLine table in the database
     *
     * @param orders - ResultSet containing the queried OrderLine information
     * @param order  - the order which the OrderLine will be added to
     * @throws SQLException
     */
    private void addOrderLine(ResultSet orders, Order order) throws SQLException {
        // Parse the query and build the Product object
        int productID = orders.getInt("product_id");
        String productName = orders.getString("product_name");
        String brandName = orders.getString("brand_name");
        String productCode = orders.getString("product_code");
        double price = orders.getDouble("price");
        String manufacturerCode = orders.getString("manufacturer_code");
        int quantity = orders.getInt("quantity");
        int stock = orders.getInt("stock");
        double lineTotal = orders.getDouble("total_cost");
        Product.SizeRatio sizeRatio = Product.SizeRatio.valueOf(orders.getString("size_ratio"));

        Product product = new Product(
                productID,
                productCode,
                brandName,
                manufacturerCode,
                productName,
                price,
                sizeRatio,
                stock);

        OrderLine orderLine = new OrderLine(product, quantity, brandName);

        // Add the OrderLine object to the Order Object's ArrayList
        order.addOrderLine(orderLine);
    }

    /**
     * This function queries the database to retrieve stored information about and
     * Order and build an Order object
     *
     * @param orderID     - the order of the ID to be queried
     * @param allProducts - an ArrayList of all stored Products in the database
     * @param connection  - the database connection
     * @return an Order object
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    public Order getOrderByOrderID(int orderID, ArrayList<Product> allProducts, Connection connection)
            throws SQLException {
        // Query relevant information about an Order and it's relevant Orderline and
        // Product information
        String query = "SELECT " +
                "Orders.order_id, " +
                "Orders.order_date, " +
                "Orders.status, " +
                "Orders.user_id, " +
                "Orders.total_cost, " +
                "Products.product_id, " +
                "Products.product_name, " +
                "Products.brand_name, " +
                "Products.product_code, " +
                "Products.price, " +
                "Products.size_ratio, " +
                "Products.manufacturer_code, " +
                "OrderLine.quantity " +
                "FROM Orders " +
                "JOIN OrderLine ON Orders.order_id = OrderLine.order_id " +
                "JOIN Products ON OrderLine.product_id = Products.product_id " +
                "WHERE Orders.order_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, orderID);

        ResultSet orders = preparedStatement.executeQuery();

        // Iterate over the ResultSet, create the Order object
        Order order = null;
        while (orders.next()) {
            if (order == null) {
                int userID = orders.getInt("user_id");
                Date orderDate = orders.getDate("order_date");
                String status = orders.getString("status");
                double totalCost = orders.getDouble("total_cost");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String dateString = dateFormat.format(orderDate);

                order = new Order(orderID, userID, dateString, totalCost, Order.OrderStatus.valueOf(status));
            }

            int productID = orders.getInt("product_id");

            // Iterate over the ArrayList of products to find the Product object
            Product product = null;
            for (Product p : allProducts) {
                if (p.getProductID() == productID) {
                    product = p;
                }
            }

            // Add the Product object to the OrderLine
            if (product != null) {
                OrderLine orderLine = new OrderLine(
                        product,
                        orders.getInt("quantity"),
                        orders.getString("brand_name"));
                // Calculate the Line Cost for that order
                orderLine.setLineCost();
                order.addOrderLine(orderLine);
            }
        }

        // Recalculate the total cost of the Order
        if (order != null) {
            order.setTotalCost();
        }

        orders.close();
        preparedStatement.close();

        return order;
    }

    /**
     * Query the database to return an ArrayList of all stored Orders, via populated
     * Order objects
     *
     * @param connection - the database connection
     * @return - ArrayList<Order>
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    public ArrayList<Order> getAllOrders(Connection connection) throws SQLException {
        // Query the database to retrieve relevant Order, OrderLine and Product
        // information for
        // that specific order
        String sql = "SELECT " +
                "Orders.order_id, " +
                "Orders.order_date, " +
                "Orders.status, " +
                "Orders.user_id, " +
                "Products.product_id, " +
                "Products.product_name, " +
                "Products.brand_name, " +
                "Products.product_code, " +
                "Products.price, " +
                "Products.stock, " +
                "Products.size_ratio, " +
                "Products.manufacturer_code, " +
                "OrderLine.quantity, " +
                "Orders.total_cost " +
                "FROM Orders " +
                "JOIN OrderLine ON Orders.order_id = OrderLine.order_id " +
                "JOIN Products ON OrderLine.product_id = Products.product_id " +
                "ORDER BY Orders.order_id;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet orders = preparedStatement.executeQuery();

        Map<Integer, Order> orderMap = new HashMap<>();

        // Iterate over the ResultSet, build the OrderLine objects and add them to the
        // Order
        while (orders.next()) {
            int orderID = orders.getInt("order_id");
            Order order = orderMap.get(orderID);

            if (order == null) {
                Date orderDate = orders.getDate("order_date");
                String status = orders.getString("status");
                int userID = orders.getInt("user_id");
                double totalCost = orders.getDouble("total_cost");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String dateString = dateFormat.format(orderDate);

                order = new Order(orderID, userID, dateString, totalCost, Order.OrderStatus.valueOf(status));
                orderMap.put(orderID, order);
            }

            addOrderLine(orders, order);
        }

        orders.close();

        return new ArrayList<>(orderMap.values());
    }

    /**
     * This queries the database to return an ArrayList of all PENDING orders which
     * are waiting to be
     * fulfilled or cancelled
     *
     * @param connection - the database connection
     * @return - ArrayList<Order>
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    public ArrayList<Order> getAllOrdersInQueue(Connection connection) throws SQLException {
        // Query the database to retrieve all Order, OrderLine and Product information
        String query = "SELECT " +
                "o.order_id, " +
                "o.order_date, " +
                "o.status, " +
                "o.user_id, " +
                "o.total_cost, " +
                "p.product_id, " +
                "p.product_name, " +
                "p.brand_name, " +
                "p.product_code, " +
                "p.price, " +
                "p.stock, " +
                "p.size_ratio, " +
                "p.manufacturer_code, " +
                "ol.quantity " +
                "FROM Orders o " +
                "JOIN OrderLine ol ON o.order_id = ol.order_id " +
                "JOIN Products p ON ol.product_id = p.product_id " +
                "WHERE o.status = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, "CONFIRMED");
        ResultSet ordersResult = preparedStatement.executeQuery();

        HashMap<Integer, Order> orders = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Iterate over the ResultSet
        while (ordersResult.next()) {
            int orderID = ordersResult.getInt("order_id");
            Order order = orders.get(orderID);
            if (order == null) {
                int userID = ordersResult.getInt("user_id");
                Date orderDate = ordersResult.getDate("order_date");
                String status = ordersResult.getString("status");
                double totalCost = ordersResult.getDouble("total_cost");
                String dateString = dateFormat.format(orderDate);

                // Create the Order object, without any OrderLines
                order = new Order(orderID, userID, dateString, totalCost, Order.OrderStatus.valueOf(status));
                orders.put(orderID, order);
            }

            // Creat the Product object, which is then used to create an OrderLine which is
            // added to an Order
            Product product = new Product(
                    ordersResult.getInt("product_id"),
                    ordersResult.getString("product_code"),
                    ordersResult.getString("brand_name"),
                    ordersResult.getString("manufacturer_code"),
                    ordersResult.getString("product_name"),
                    ordersResult.getDouble("price"),
                    Product.SizeRatio.valueOf(ordersResult.getString("size_ratio")),
                    ordersResult.getInt("stock"));

            OrderLine orderLine = new OrderLine(
                    product,
                    ordersResult.getInt("quantity"),
                    ordersResult.getString("brand_name"));
            orderLine.setLineCost();
            order.addOrderLine(orderLine);
        }

        ordersResult.close();
        preparedStatement.close();

        return new ArrayList<>(orders.values());
    }

    /**
     * This sets an Order's status to CONFIRMED given that it is PENDING
     *
     * @param orderID    - the ID of the order
     * @param connection - the database connection
     * @return - True if the order is confirmed; otherwise false
     * @throws SQLException           if there is an issue with the SQL, query or
     *                                database
     * @throws NoOrderExistsException - if no Order exists with the given Order ID
     */
    public boolean confirmOrder(int orderID, Connection connection) throws SQLException, NoOrderExistsException {
        String sql = "UPDATE Orders SET status = 'CONFIRMED' WHERE order_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, orderID);

        Order.OrderStatus orderStatus = getOrderStatus(orderID, connection);

        if (orderStatus == null) {
            throw new NoOrderExistsException();
        }

        if (orderStatus.equals(Order.OrderStatus.PENDING)) {
            // Confirm the order, execute the SQL command
            preparedStatement.execute();

            return preparedStatement.getUpdateCount() > 0;
        } else
            return orderStatus.equals(Order.OrderStatus.CONFIRMED);
    }

    /**
     * This sets an Order's status to FULFILLED given that it is CONFIRMED
     *
     * @param orderID
     * @param connection
     * @return
     * @throws SQLException           if there is an issue with the SQL, query or
     *                                database
     * @throws NoOrderExistsException - if no Order exists with the given Order ID
     */
    public boolean fulfillOrder(int orderID, Connection connection) throws SQLException, NoOrderExistsException {
        String sql = "UPDATE Orders SET status = 'FULFILLED' WHERE order_id = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, orderID);
        Order.OrderStatus orderStatus = getOrderStatus(orderID, connection);

        Order order = getOrderByOrderID(orderID, getAllProducts(connection), connection);

        if (orderStatus == null) {
            throw new NoOrderExistsException();
        }

        // Check that the order's status is CONFIRMED
        if (orderStatus.equals(Order.OrderStatus.CONFIRMED)) {

            for (OrderLine o : order.getOrderLineArrayList()) {
                int stock = o.getProduct().getStock();

                // Prevent orders from being fulfilled if there is not enough stock available
                if (stock - o.getQuantity() < 0) {
                    return false;
                }
            }

            // Update the product's stock to reflect the fulfilled order
            for (OrderLine o : order.getOrderLineArrayList()) {
                o.getProduct().setStock(o.getProduct().getStock() - o.getQuantity());

                updateGeneralProduct(o.getProduct(), connection);
            }

            preparedStatement.execute();

            return preparedStatement.getUpdateCount() > 0;
        }

        // If the order status is fulfilled, return true. Else FALSE
        return orderStatus.equals(Order.OrderStatus.FULFILLED);
    }

    /**
     * This function cancels an order given that it is CONFIRMED
     *
     * @param orderID            - the ID of the order to be cancelled
     * @param cancellationReason - the reason for cancellation
     * @param connection         - the database connection
     * @return True if cancelled; false otherwise
     * @throws SQLException           if there is an issue with the SQL, query or
     *                                database
     * @throws NoOrderExistsException - if no Order exists with the given Order ID
     */
    public boolean cancelOrder(int orderID, String cancellationReason, Connection connection)
            throws SQLException, NoOrderExistsException {
        String sql = "UPDATE Orders SET status = 'CANCELLED' WHERE order_id = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, orderID);
        Order.OrderStatus orderStatus = getOrderStatus(orderID, connection);

        if (orderStatus == null) {
            throw new NoOrderExistsException();
        }

        if (orderStatus.equals(Order.OrderStatus.PENDING) || orderStatus.equals(Order.OrderStatus.CONFIRMED)) {
            preparedStatement.execute();

            return preparedStatement.getUpdateCount() > 0;
        } else
            return orderStatus.equals(Order.OrderStatus.CANCELLED);
    }

    /**
     * This function returns the Status of an Order (Confirmed, Cancelled, Pending,
     * Fulfilled)
     *
     * @param orderID    - the ID of the order which status will be queried
     * @param connection - the database connection
     * @return OrderStatus Enum
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    public Order.OrderStatus getOrderStatus(int orderID, Connection connection) throws SQLException {
        String sql = "SELECT status FROM Orders WHERE order_id = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, orderID);

        ResultSet usersQuery = preparedStatement.executeQuery();

        if (usersQuery.next()) {
            Order.OrderStatus status = Order.OrderStatus.valueOf(usersQuery.getString("status"));
            usersQuery.close();
            return status;
        }

        return null;
    }

    /**
     * This returns an ArrayList of all stored products in the database
     *
     * @param connection - the database connection
     * @return ArrayList<Product>
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    public ArrayList<Product> getAllProducts(Connection connection) throws SQLException {
        LinkedHashMap<Integer, Product> productMap = new LinkedHashMap<>();

        // Query the database to retrieve all relevant information about an order
        String sql = "SELECT " +
                "Products.*, " +
                "TrainSet.train_set_id, " +
                "TrainSetProducts.quantity AS tsp_quantity, " +
                "p2.product_id AS p2_product_id, " +
                "p2.product_name AS p2_product_name, " +
                "p2.brand_name AS p2_brand_name, " +
                "p2.product_code AS p2_product_code, " +
                "p2.manufacturer_code AS p2_manufacturer_code, " +
                "p2.price AS p2_price, " +
                "p2.size_ratio AS p2_size_ratio, " +
                "p2.stock AS p2_stock, " +
                "Locomotives.locomotive_model, " +
                "Era.start_era, " +
                "Era.end_era, " +
                "Controllers.signal_type " +
                "FROM Products " +
                "LEFT JOIN TrainSet ON Products.product_id = TrainSet.product_id " +
                "LEFT JOIN TrainSetProducts ON TrainSet.train_set_id = TrainSetProducts.train_set_id " +
                "LEFT JOIN Products p2 ON TrainSetProducts.product_id = p2.product_id " +
                "LEFT JOIN Locomotives ON Products.product_id = Locomotives.product_id OR p2.product_id = Locomotives.product_id "
                +
                "LEFT JOIN Era ON Products.product_id = Era.product_id OR p2.product_id = Era.product_id " +
                "LEFT JOIN Controllers ON Products.product_id = Controllers.product_id OR p2.product_id = Controllers.product_id;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet productsQuery = preparedStatement.executeQuery();

        // Regular expressions used to determine the product type from its Product Code
        String trackPieceRegex = "R\\d{3,5}";
        String controllerRegex = "C\\d{3,5}";
        String locomotiveRegex = "L\\d{3,5}";
        String rollingStockRegex = "S\\d{3,5}";
        String trainSetRegex = "M\\d{3,5}";
        String trackPackRegex = "P\\d{3,5}";

        // Iterate over the ResultSet of Products to build the Product objects
        while (productsQuery.next()) {
            // Regular Product Fields
            int productID = productsQuery.getInt("product_id");
            String productName = productsQuery.getString("product_name");
            String brandName = productsQuery.getString("brand_name");
            String productCode = productsQuery.getString("product_code");
            String manufacturerCode = productsQuery.getString("manufacturer_code");
            double price = productsQuery.getDouble("price");
            Product.SizeRatio sizeRatio = Product.SizeRatio.valueOf(productsQuery.getString("size_ratio"));
            int stock = productsQuery.getInt("stock");

            // Determine the type of Product by the ProductCode
            if (productCode.matches(trainSetRegex)) {
                int trainSetID = productsQuery.getInt("train_set_id");
                int trainSetProductQuantity = productsQuery.getInt("tsp_quantity");

                // If the productMap does not contain trainSet, add it
                productMap.computeIfAbsent(productID, k -> new TrainSet(
                        productID,
                        productCode,
                        brandName,
                        manufacturerCode,
                        productName,
                        price,
                        sizeRatio,
                        stock,
                        trainSetID,
                        new LinkedHashMap<>()));

                // Parse the query to create the Product 'p2' object. This is the product
                // contained in the TrainSet
                int trainSetProductID = productsQuery.getInt("p2_product_id");
                String trainSetProductName = productsQuery.getString("p2_product_name");
                String trainSetBrandName = productsQuery.getString("p2_brand_name");
                String trainSetProductCode = productsQuery.getString("p2_product_code");
                String trainSetManufacturerCode = productsQuery.getString("p2_manufacturer_code");
                double trainSetPrice = productsQuery.getDouble("p2_price");
                Product.SizeRatio trainSetSizeRatio = Product.SizeRatio
                        .valueOf(productsQuery.getString("p2_size_ratio"));
                int trainSetStock = productsQuery.getInt("p2_stock");

                if (trainSetProductCode.matches(trackPackRegex)) {
                    TrackPack.TrackType trackPackType = TrackPack.TrackType
                            .valueOf(trainSetProductName.toUpperCase().replaceAll("\\s", "_"));
                    TrackPack trackPack = new TrackPack(
                            trainSetProductID,
                            trainSetProductCode,
                            trainSetBrandName,
                            trainSetManufacturerCode,
                            trainSetProductName,
                            trainSetPrice,
                            trainSetSizeRatio,
                            trainSetStock,
                            trackPackType);
                    ((TrainSet) productMap.get(productID)).addProduct(trackPack, trainSetProductQuantity);
                } else if (trainSetProductCode.matches(locomotiveRegex)) {
                    Locomotive.LocomotiveModel locomotiveModel = Locomotive.LocomotiveModel
                            .valueOf(productsQuery.getString("locomotive_model"));
                    RollingStock.Era startEra = RollingStock.Era.valueOf(productsQuery.getString("start_era"));
                    RollingStock.Era endEra = RollingStock.Era.valueOf(productsQuery.getString("end_era"));
                    Locomotive locomotive = new Locomotive(
                            trainSetProductID,
                            trainSetProductCode,
                            trainSetBrandName,
                            trainSetManufacturerCode,
                            trainSetProductName,
                            trainSetPrice,
                            trainSetSizeRatio,
                            trainSetStock,
                            startEra,
                            endEra,
                            locomotiveModel);
                    ((TrainSet) productMap.get(productID)).addProduct(locomotive, trainSetProductQuantity);
                } else if (trainSetProductCode.matches(controllerRegex)) {
                    Controller.SignalType signalType = Controller.SignalType
                            .valueOf(productsQuery.getString("signal_type"));
                    Controller controller = new Controller(
                            trainSetProductID,
                            trainSetProductCode,
                            trainSetBrandName,
                            trainSetManufacturerCode,
                            trainSetProductName,
                            trainSetPrice,
                            trainSetSizeRatio,
                            trainSetStock,
                            signalType);
                    ((TrainSet) productMap.get(productID)).addProduct(controller, trainSetProductQuantity);
                } else if (trainSetProductCode.matches(rollingStockRegex)) {
                    RollingStock.Era startEra = RollingStock.Era.valueOf(productsQuery.getString("start_era"));
                    RollingStock.Era endEra = RollingStock.Era.valueOf(productsQuery.getString("end_era"));
                    RollingStock rollingStock = new RollingStock(
                            trainSetProductID,
                            trainSetProductCode,
                            trainSetBrandName,
                            trainSetManufacturerCode,
                            trainSetProductName,
                            trainSetPrice,
                            trainSetSizeRatio,
                            trainSetStock,
                            startEra,
                            endEra);
                    ((TrainSet) productMap.get(productID)).addProduct(rollingStock, trainSetProductQuantity);
                } else if (trainSetProductCode.matches(trackPieceRegex)) {
                    String regEx = "^((1st|2nd|3rd) Radius )?(.+)$";
                    //
                    Pattern pattern = Pattern.compile(regEx);
                    Matcher matcher = pattern.matcher(trainSetProductName);

                    if (matcher.find()) {
                        String radius = matcher.group(1);
                        TrackPiece trackPiece = new TrackPiece(
                                trainSetProductID,
                                trainSetProductCode,
                                trainSetBrandName,
                                trainSetManufacturerCode,
                                trainSetProductName,
                                trainSetPrice,
                                trainSetSizeRatio,
                                trainSetStock,
                                radius);
                        ((TrainSet) productMap.get(productID)).addProduct(trackPiece, trainSetProductQuantity);
                    } else {
                        TrackPiece trackPiece = new TrackPiece(
                                trainSetProductID,
                                trainSetProductCode,
                                trainSetBrandName,
                                trainSetManufacturerCode,
                                trainSetProductName,
                                trainSetPrice,
                                trainSetSizeRatio,
                                trainSetStock);
                        ((TrainSet) productMap.get(productID)).addProduct(trackPiece, trainSetProductQuantity);
                    }
                }

            } else if (productCode.matches(trackPackRegex)) {
                TrackPack.TrackType trackPackType = TrackPack.TrackType
                        .valueOf(productName.toUpperCase().replaceAll("\\s", "_"));
                TrackPack trackPack = new TrackPack(
                        productID,
                        productCode,
                        brandName,
                        manufacturerCode,
                        productName,
                        price,
                        sizeRatio,
                        stock,
                        trackPackType);
                productMap.put(productID, trackPack);
            } else if (productCode.matches(locomotiveRegex)) {
                Locomotive.LocomotiveModel locomotiveModel = Locomotive.LocomotiveModel
                        .valueOf(productsQuery.getString("locomotive_model"));
                RollingStock.Era startEra = RollingStock.Era.valueOf(productsQuery.getString("start_era"));
                RollingStock.Era endEra = RollingStock.Era.valueOf(productsQuery.getString("end_era"));
                Locomotive locomotive = new Locomotive(
                        productID,
                        productCode,
                        brandName,
                        manufacturerCode,
                        productName,
                        price,
                        sizeRatio,
                        stock,
                        startEra,
                        endEra,
                        locomotiveModel);
                productMap.put(productID, locomotive);
            } else if (productCode.matches(controllerRegex)) {
                Controller.SignalType signalType = Controller.SignalType
                        .valueOf(productsQuery.getString("signal_type"));
                Controller controller = new Controller(
                        productID,
                        productCode,
                        brandName,
                        manufacturerCode,
                        productName,
                        price,
                        sizeRatio,
                        stock,
                        signalType);
                productMap.put(productID, controller);
            } else if (productCode.matches(rollingStockRegex)) {
                RollingStock.Era startEra = RollingStock.Era.valueOf(productsQuery.getString("start_era"));
                RollingStock.Era endEra = RollingStock.Era.valueOf(productsQuery.getString("end_era"));
                RollingStock rollingStock = new RollingStock(
                        productID,
                        productCode,
                        brandName,
                        manufacturerCode,
                        productName,
                        price,
                        sizeRatio,
                        stock,
                        startEra,
                        endEra);
                productMap.put(productID, rollingStock);
            } else if (productCode.matches(trackPieceRegex)) {
                // Regular expressions used to retrieve the Radius from the Product Name from a
                // Track Piece
                String regEx = "^((1st|2nd|3rd) Radius) (.+)$";

                Pattern pattern = Pattern.compile(regEx);
                Matcher matcher = pattern.matcher(productName);

                if (matcher.find()) {
                    String radius = matcher.group(1);
                    TrackPiece trackPiece = new TrackPiece(
                            productID,
                            productCode,
                            brandName,
                            manufacturerCode,
                            productName,
                            price,
                            sizeRatio,
                            stock,
                            radius);
                    productMap.put(productID, trackPiece);
                } else {
                    TrackPiece trackPiece = new TrackPiece(
                            productID,
                            productCode,
                            brandName,
                            manufacturerCode,
                            productName,
                            price,
                            sizeRatio,
                            stock);
                    productMap.put(productID, trackPiece);
                }
            }
        }

        // Convert the Product Map to an ArrayList and return it
        ArrayList<Product> finalProducts = new ArrayList<>();

        for (Map.Entry<Integer, Product> entry : productMap.entrySet()) {
            finalProducts.add(entry.getValue());
        }

        return finalProducts;
    }

    /**
     * This adds a new Product to the database given that existing products do not
     * have any conflicting information
     *
     * @param allProducts
     * @param product
     * @param connection
     * @return
     * @throws SQLException
     */
    public boolean addNewProduct(ArrayList<Product> allProducts, Product product, Connection connection)
            throws SQLException {
        Statement statement = connection.createStatement();
        // insert empty record, get product id
        // update record

        if (product.getProductID() != 0) {
            return false;
        }

        for (Product p : allProducts) {
            if (p.getProductCode().equals(product.getProductCode()) ||
                    (p.getProductName().equals(product.getProductName()) &&
                            p.getModellingScale().equals(product.getModellingScale()))) {
                return false;
            }
        }

        // Check if a product exists with the given product code,
        // check if a product exists with the given product name and size ratio

        // Insert a new product and retrieve the ID of the newly inserted product.
        // This is done as a product's id is not know before it is inserted
        statement.execute(
                "INSERT INTO Products (product_name, brand_name, price, stock, size_ratio, manufacturer_code, product_code) VALUES "
                        +
                        "(null, null, null, null, null, null, null);",
                Statement.RETURN_GENERATED_KEYS);

        ResultSet keys = statement.getGeneratedKeys();
        if (keys.next()) {
            int productID = keys.getInt(1);
            // Update the newly inserted product record with the product Information
            String sql = "UPDATE Products SET " +
                    "product_code = ?, " +
                    "product_name = ?, " +
                    "brand_name = ?, " +
                    "manufacturer_code = ?, " +
                    "price = ?, " +
                    "stock = ?, " +
                    "size_ratio = ? " +
                    "WHERE product_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, product.getProductCode());
            preparedStatement.setString(2, product.getProductName());
            preparedStatement.setString(3, product.getBrandName());
            preparedStatement.setString(4, product.getManufacturerCode());
            preparedStatement.setDouble(5, product.getRetailPrice());
            preparedStatement.setInt(6, product.getStock());
            preparedStatement.setString(7, product.getModellingScale().name());
            preparedStatement.setInt(8, productID);
            preparedStatement.execute();

            assert preparedStatement.getUpdateCount() == 1;

            // Insert specific information for certain Product Types
            if (product instanceof RollingStock rollingStock) {
                // Insert into Era table

                sql = "INSERT INTO Era (product_id, start_era, end_era) VALUES (?, ?, ?);";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, productID);
                preparedStatement.setString(2, rollingStock.getStartEra().name());
                preparedStatement.setString(3, rollingStock.getEndEra().name());
                preparedStatement.execute();
                assert preparedStatement.getUpdateCount() == 1;
            }

            if (product instanceof Locomotive locomotive) {
                // insert into Locomotives table

                sql = "INSERT INTO Locomotives (product_id, locomotive_model) VALUES (?, ?);";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, productID);
                preparedStatement.setString(2, locomotive.getLocomotiveModel().name());
                preparedStatement.execute();
                assert preparedStatement.getUpdateCount() == 1;
            } else if (product instanceof Controller controller) {
                // insert into Controllers table

                sql = "INSERT INTO Controllers (product_id, signal_type) VALUES (?, ?);";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, productID);
                preparedStatement.setString(2, controller.getSignalType().name());
                preparedStatement.execute();
                assert preparedStatement.getUpdateCount() == 1;
            }
            return true;
        }

        // Return false if no Order ID is returned. This shouldn't happen

        return false;
    }

    /**
     * This functio deletes an existing product from the database
     *
     * @param product    - the product to delete
     * @param connection - the database connection
     * @return True if deleted; false if not
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    public boolean deleteProduct(Product product, Connection connection) throws SQLException {

        // Train Set - TrainSet, TrainSetProducts
        // Locomotives - Locomotives
        // Controllers
        // RollingStock, Locomotive - Era

        // Check if the product exists in the Products table
        // Delete from Products table first

        String sql = "SELECT * FROM Products WHERE product_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, product.getProductID());
        ResultSet productQuery = preparedStatement.executeQuery();

        if (!productQuery.next()) {
            return false;
        }

        if (product instanceof Locomotive) {
            // Delete from Locomotives table
            sql = "DELETE FROM Locomotives WHERE product_id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, product.getProductID());
            preparedStatement.execute();

            assert preparedStatement.getUpdateCount() == 1;
        } else if (product instanceof TrainSet trainSet) {
            int trainSetID = trainSet.getTrainSetID();
            // Delete from TrainSet table
            // Delete products from TrainSetProducts

            sql = "DELETE FROM TrainSetProducts WHERE train_set_id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, trainSetID);
            preparedStatement.execute();

            assert preparedStatement.getUpdateCount() > 0;

            sql = "DELETE FROM TrainSet WHERE product_id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, product.getProductID());
            preparedStatement.execute();

            assert preparedStatement.getUpdateCount() == 1;
        } else if (product instanceof Controller) {
            // Delete from Controllers
            sql = "DELETE FROM Controllers WHERE product_id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, product.getProductID());

            assert preparedStatement.getUpdateCount() == 1;
        }

        if (product instanceof RollingStock) {
            sql = "DELETE FROM Era WHERE product_id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, product.getProductID());
            preparedStatement.execute();

            assert preparedStatement.getUpdateCount() == 1;
        }

        sql = "DELETE FROM Products WHERE product_id = ?";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, product.getProductID());
        preparedStatement.execute();

        return preparedStatement.getUpdateCount() == 1;
    }

    /**
     * This updates an existing product with data from the newly passed Product
     * object
     *
     * @param oldProduct - Existing Product object to be updated
     * @param newProduct - New Product object containing values to be inserted into
     *                   the database
     * @param connection - Database connection
     * @return - True if the database record was updated
     * @throws SQLException if there is an issue with the SQL, query or database
     *                      <p>
     *                      Fields that can be updated
     *                      (Product Name + Modelling Scale) - UNIQUE
     *                      Manufacturer Name
     *                      Brand Name
     *                      Price
     *                      Stock
     *                      Radius | (TrackPiece)
     *                      Signal Type | (Controller)
     *                      Locomotive Model | (Locomotive)
     *                      Start Era | (Rolling Stock, Locomotive)
     *                      End Era | (Rolling Stock, Locomotive)
     *                      (Company Name/BR Standard Mark + Carriage Description)
     *                      -> Product Name | (Carriage (RollingStock))
     *                      Track Piece -> Product Name = Radius (if not null) +
     *                      Product Name
     *                      Carriage (Rolling Stock) -> Product Name = Company
     *                      Name/BR Standard Mark + Carriage Description
     */
    public boolean editExistingProduct(Product oldProduct, Product newProduct, Connection connection)
            throws SQLException {
        assert oldProduct.getProductID() == newProduct.getProductID();
        assert Objects.equals(oldProduct.getProductCode(), newProduct.getProductCode());

        String sql = "SELECT * FROM Products WHERE product_id != ? AND product_name = ? AND size_ratio = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, newProduct.getProductID());
        preparedStatement.setString(2, newProduct.getProductName());
        preparedStatement.setString(3, newProduct.getModellingScale().name());

        ResultSet uniqueProductQuery = preparedStatement.executeQuery();

        if (uniqueProductQuery.next()) {
            // Duplicate record found with (Product Name + Modelling Scale)
            return false;
        }

        uniqueProductQuery.close();

        if (newProduct instanceof TrainSet || newProduct instanceof TrackPack) {
            return updateGeneralProduct(newProduct, connection);

        } else if (newProduct instanceof TrackPiece trackPiece) {
            String radius = trackPiece.getRadius();
            radius = radius == null ? newProduct.getProductName() : radius + " " + newProduct.getProductName();

            sql = "SELECT * FROM Products WHERE product_id != ? AND product_name = ? AND size_ratio = ?;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, newProduct.getProductID());
            preparedStatement.setString(2, radius);
            preparedStatement.setString(3, newProduct.getModellingScale().name());
            uniqueProductQuery = preparedStatement.executeQuery();

            if (uniqueProductQuery.next()) {
                // Duplicate record found with (Product Name + Modelling Scale)
                uniqueProductQuery.close();
                return false;
            } else {
                // No duplicate records found with (Product Name + Modelling Scale)
                return updateGeneralProduct(newProduct, connection);
            }
        } else if (newProduct instanceof Locomotive locomotive) {
            Locomotive.LocomotiveModel locomotiveModel = locomotive.getLocomotiveModel();
            RollingStock.Era startEra = locomotive.getStartEra();
            RollingStock.Era endEra = locomotive.getEndEra();

            boolean res = updateGeneralProduct(newProduct, connection);

            sql = "UPDATE Era SET start_era = ?, end_era = ? WHERE product_id = ?;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, startEra.name());
            preparedStatement.setString(2, endEra.name());
            preparedStatement.setInt(3, newProduct.getProductID());
            preparedStatement.execute();

            sql = "Update Locomotives SET locomotive_model = ? WHERE product_id = ?;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, locomotiveModel.name());
            preparedStatement.setInt(2, newProduct.getProductID());
            preparedStatement.execute();

            return preparedStatement.getUpdateCount() > 0;

        } else if (newProduct instanceof RollingStock rollingStock) {
            RollingStock.Era startEra = rollingStock.getStartEra();
            RollingStock.Era endEra = rollingStock.getEndEra();

            // Check if oldProduct productName == (LNER/...) (.+)
            String carriageRegex = "^(LMS|LNER|GWR|SR|MARK 1|MARK 2|MARK 3|MARK 4) (.+)";
            Pattern pattern = Pattern.compile(carriageRegex);
            String rollingStockName = rollingStock.getProductName();
            Matcher matcher = pattern.matcher(rollingStockName);

            if (matcher.find()) {
                // Carriage
                String carriageType = matcher.group(1);
                String carriageDescription = matcher.group(2);
                String newProductName = carriageType + " " + carriageDescription;

                sql = "SELECT * FROM Products WHERE product_id != ? AND product_name = ? AND size_ratio = ?;";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, newProduct.getProductID());
                preparedStatement.setString(2, newProductName);
                preparedStatement.setString(3, newProduct.getModellingScale().name());
                uniqueProductQuery = preparedStatement.executeQuery();

                if (uniqueProductQuery.next()) {
                    // Duplicate record found with (Product Name + Modelling Scale)
                    return false;
                } else {
                    // No duplicate records found with (Product Name + Modelling Scale)

                    return updateProductEra(newProduct, connection, startEra, endEra);
                }
            } else {
                // Wagon
                boolean res = updateProductEra(newProduct, connection, startEra, endEra);

                return updateProductEra(newProduct, connection, startEra, endEra);
            }
        } else if (newProduct instanceof Controller controller) {
            Controller.SignalType signalType = controller.getSignalType();

            boolean res = updateGeneralProduct(newProduct, connection);

            sql = "UPDATE Controllers SET signal_type = ? WHERE product_id = ?;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, signalType.name());
            preparedStatement.setInt(2, newProduct.getProductID());
            preparedStatement.execute();

            return preparedStatement.getUpdateCount() > 0;
        }
        return false;
    }

    /**
     * This updates the Era for a specific product. Only RollingStock Products have
     * an Era.
     *
     * @param newProduct - the product to be updated
     * @param connection - the database connection
     * @param startEra   - the new Start Era
     * @param endEra     - the new End Era
     * @return True if updated; false if not
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    private boolean updateProductEra(Product newProduct, Connection connection, RollingStock.Era startEra,
            RollingStock.Era endEra) throws SQLException {
        String sql;
        PreparedStatement preparedStatement;
        sql = "UPDATE Era SET start_era = ?, end_era = ? WHERE product_id = ?;";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, startEra.name());
        preparedStatement.setString(2, endEra.name());
        preparedStatement.setInt(3, newProduct.getProductID());
        preparedStatement.execute();

        return updateGeneralProduct(newProduct, connection);
    }

    /**
     * This function updates the general fields for a stored product which all
     * Products share
     *
     * @param newProduct - the product to be updated
     * @param connection - the database connection
     * @return True if successfully updated; false if not
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    private boolean updateGeneralProduct(Product newProduct, Connection connection) throws SQLException {
        String sql = "UPDATE Products SET " +
                "manufacturer_code = ?, " +
                "brand_name = ?, " +
                "price = ?, " +
                "stock = ?, " +
                "product_name = ?, " +
                "size_ratio = ? " +
                "WHERE product_id = ?;";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, newProduct.getManufacturerCode());
        preparedStatement.setString(2, newProduct.getBrandName());
        preparedStatement.setDouble(3, newProduct.getRetailPrice());
        preparedStatement.setInt(4, newProduct.getStock());
        preparedStatement.setString(5, newProduct.getProductName());
        preparedStatement.setString(6, newProduct.getModellingScale().name());
        preparedStatement.setInt(7, newProduct.getProductID());
        preparedStatement.execute();

        return preparedStatement.getUpdateCount() > 0;
    }

    /**
     * This creates a new Pending Order and inserts it into the Orders table
     *
     * @param connection - Connection to the database
     * @return - The orderID of the order which is just inserted
     * @throws SQLException - if there is an issue with the SQL, query or database
     */
    public int createPendingOrder(Order order, Connection connection) throws SQLException {
        // Insert new order

        String sql = "INSERT INTO Orders (user_id, order_date, status, total_cost) " +
                "VALUES(?, ?, ?, ?);";
        PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setInt(1, order.getUserID());

        // Convert the string to java.sql.Date
        java.sql.Date orderDate = java.sql.Date.valueOf(order.getDate()); // Ensure order.getDate() returns in
                                                                          // "YYYY-MM-DD"
        preparedStatement.setDate(2, orderDate);

        preparedStatement.setString(3, order.getOrderStatus().name());
        preparedStatement.setDouble(4, order.getTotalCost());
        preparedStatement.execute();

        ResultSet keys = preparedStatement.getGeneratedKeys();
        if (keys.next()) {
            return keys.getInt(1);
        }

        return -1;
    }

    /**
     * This updates an existing Order by adding a new OrderLine, removing an
     * OrderLine, or updating the
     * quantity of an existing OrderLine for a given Order
     *
     * @param orderID    - the order to be updated
     * @param orderLine  - the OrderLine to add/delete/edit
     * @param connection 0 the database connection
     * @return True if successfully updated; false if not
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    public boolean updateOrder(int orderID, OrderLine orderLine, Connection connection) throws SQLException {
        String sql = "SELECT * FROM OrderLine WHERE order_id = ? AND product_id = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, orderID);
        preparedStatement.setInt(2, orderLine.getProduct().getProductID());

        Statement statement = connection.createStatement();
        ResultSet existingOrderLine = preparedStatement.executeQuery();

        if (existingOrderLine.next()) {
            int orderLineID = existingOrderLine.getInt("order_line_id");
            if (orderLine.getQuantity() == 0) {
                // Delete from database

                sql = "DELETE FROM OrderLine WHERE order_line_id = ?;";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, orderLineID);
                preparedStatement.execute();
            } else {
                // Increment/Decrement OrderLine quantity
                sql = "UPDATE OrderLine SET quantity = ? WHERE order_line_id = ?;";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, orderLine.getQuantity());
                preparedStatement.setInt(2, orderLineID);
                preparedStatement.execute();
            }
            assert preparedStatement.getUpdateCount() == 1;
            // Update Order with total cost

            sql = "SELECT " +
                    "ol.order_line_id, " +
                    "ol.order_id, " +
                    "SUM(p.price * ol.quantity) AS total_price " +
                    "FROM OrderLine AS ol " +
                    "JOIN Products AS p ON ol.product_id = p.product_id " +
                    "WHERE ol.order_id = ? " +
                    "GROUP BY ol.order_line_id;";

            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, orderID);

            ResultSet orderLineCosts = preparedStatement.executeQuery();

            double totalOrderLinesCost = 0;
            while (orderLineCosts.next()) {
                totalOrderLinesCost += orderLineCosts.getDouble("total_price");
            }

            sql = "UPDATE Orders SET total_cost = ? WHERE order_id = ?;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDouble(1, totalOrderLinesCost);
            preparedStatement.setInt(2, orderID);
            preparedStatement.execute();

            assert preparedStatement.getUpdateCount() == 1;
        } else {
            sql = "INSERT INTO OrderLine (order_id, product_id, quantity) VALUES (?, ?, ?);";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, orderID);
            preparedStatement.setInt(2, orderLine.getProduct().getProductID());
            preparedStatement.setInt(3, orderLine.getQuantity());
            preparedStatement.execute();

            assert preparedStatement.getUpdateCount() == 1;
            return true;
        }

        statement.close();
        preparedStatement.close();
        return false;
    }

    /**
     * This function returns the salt stored for a given user, which is used in the
     * key derivation
     * function for generating the symmetric key used in AES encryption
     *
     * @param userID     - the ID of the user
     * @param connection - the databse connection
     * @return byte[] array containing the decoded Base64 salt String data
     * @throws SQLException if there is an issue with the SQL, query or database
     */
    public byte[] getEncryptionSalt(int userID, Connection connection) throws SQLException {
        String sql = "SELECT salt_aes FROM Users WHERE user_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, userID);

        ResultSet userEncryptionSaltQuery = preparedStatement.executeQuery();

        if (userEncryptionSaltQuery.next()) {
            return Base64.getDecoder().decode(userEncryptionSaltQuery.getString("salt_aes"));
        }

        return null;
    }
}