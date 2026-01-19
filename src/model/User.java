package model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;

public class User {

    private int userID;
    private String forename;
    private String surname;
    private String email;
    private String password;
    private String salt;
    private boolean isCustomer;
    private boolean isStaff;
    private boolean isManager;
    private Address address;
    private ArrayList<Role> userRoles;
    private PaymentMethod paymentMethod;

    public User(int userID, String forename, String surname, String email, String password, String salt, boolean isCustomer, boolean isStaff, boolean isManager, Address address, ArrayList<Role> userRoles, PaymentMethod paymentMethod) {
        this.userID = userID;
        this.forename = forename;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.salt = salt;
        this.isCustomer = isCustomer;
        this.isStaff = isStaff;
        this.isManager = isManager;
        this.address = address;
        this.userRoles = userRoles;
        this.paymentMethod = paymentMethod;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public boolean isCustomer() {
        return isCustomer;
    }

    public void setCustomer(boolean customer) {
        isCustomer = customer;
    }

    public boolean isStaff() {
        return isStaff;
    }

    public void setStaff(boolean staff) {
        isStaff = staff;
    }

    public boolean isManager() {
        return isManager;
    }

    public void setManager(boolean manager) {
        isManager = manager;
    }

    public ArrayList<Role> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(ArrayList<Role> userRoles) {
        this.userRoles = userRoles;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getSalt() {
        return salt;
    }
    public Boolean passwordsMatch(String password){
        String hashString = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(this.salt.getBytes());
            byte[] hash = md.digest(password.getBytes());
            hashString = HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException er){
            er.printStackTrace();
            return false;
        }

        return hashString.equals(this.password);
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    public String toString() {

        String output = "User{" + "\n" +
                "   userID='" + userID + "',\n" +
                "   forename='" + forename + "',\n" +
                "   surname='" + surname + "',\n" +
                "   email='" + email + "',\n" +
                "   password='" + password + "',\n" +
                "   isCustomer=" + isCustomer + "',\n" +
                "   isStaff=" + isStaff + "',\n" +
                "   isManager=" + isManager + "',\n" +
                "   address=" + address + "',\n" +
                "   userRoles=" + userRoles.toString() + "'\n";

        if (paymentMethod != null) {
            output +=  "   paymentMethod=" + paymentMethod.toString() + "'\n";
        }

        output += "}";

        return output;
    }
}
