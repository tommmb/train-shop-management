package model;

import java.util.ArrayList;

public class Order {
    private int orderNumber;
    private int userID;
    private String date;
    private OrderStatus orderStatus;
    private double totalCost;
    private ArrayList<OrderLine> orderLineArrayList;

    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        FULFILLED,
        CANCELLED
    }

    public Order(int orderNumber, int userID, String date, double totalCost, OrderStatus orderStatus) {
        this.orderNumber = orderNumber;
        this.userID = userID;
        this.date = date;
        this.orderStatus = orderStatus;
        this.totalCost = totalCost;
        this.orderLineArrayList = new ArrayList<>();
    }

    public Order(int userID, String date, double totalCost, OrderStatus orderStatus, ArrayList<OrderLine> orderLineArrayList) {
        this.userID = userID;
        this.date = date;
        this.orderStatus = orderStatus;
        this.totalCost = totalCost;
        this.orderLineArrayList = orderLineArrayList;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost() {
        int total = 0;
        for (OrderLine ol : getOrderLineArrayList()) {
            ol.setLineCost();
            total += ol.getLineCost();
        }

        this.totalCost = total;
    }

    public ArrayList<OrderLine> getOrderLineArrayList() {
        return orderLineArrayList;
    }

    public void addOrderLine(OrderLine orderLine) {
        boolean productExistsInOrder = false;

        for (OrderLine o : getOrderLineArrayList()) {
            if (o.getProduct().equals(orderLine.getProduct())) {
                getOrderLineArrayList().set(getOrderLineArrayList().indexOf(o), orderLine);
                productExistsInOrder = true;
                break;
            }
        }

        if (!productExistsInOrder) {
            this.orderLineArrayList.add(orderLine);
        }

        setTotalCost();
    }

    public void removeOrderLine(OrderLine orderLine) {
        this.orderLineArrayList.remove(orderLine);
        setTotalCost();
    }

    @Override
    public String toString() {
        ArrayList<String> orderLinesToString = new ArrayList<>();

        for (OrderLine orderLine : orderLineArrayList) {
            orderLinesToString.add(orderLine.toString());
        }

        return "Order{" +
                "orderNumber=" + orderNumber +
                ", userID=" + userID +
                ", date='" + date + '\'' +
                ", orderStatus=" + orderStatus +
                ", total_cost=" + totalCost +
                ", orderLineArrayList=" + orderLinesToString +
                '}';
    }
}
