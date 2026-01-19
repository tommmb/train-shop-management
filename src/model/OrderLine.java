package model;

public class OrderLine {
    private Product product;
    private int quantity;
    private String brand;
    private double lineCost;

    public OrderLine(Product product, int quantity, String brand) {
        this.product = product;
        this.quantity = quantity;
        this.brand = brand;
        setLineCost();
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public double getLineCost() {
        return lineCost;
    }

    public void setLineCost() {
        this.lineCost = getQuantity() * getProduct().getRetailPrice();
    }

    @Override
    public String toString() {
        return "OrderLine{" +
                "product=" + product.toString() +
                ", quantity=" + quantity +
                ", brand='" + brand + '\'' +
                ", lineCost=" + lineCost +
                '}';
    }
}
