package model;

public class Product {
    private int productID;
    private String productCode;
    private String brandName;
    private String manufacturerCode;
    private String productName;
    private double retailPrice;
    private int stock;
    private SizeRatio modellingScale;

    public enum SizeRatio {
        OO_GAUGE,
        TT_GAUGE,
        N_GAUGE
    }

    public Product(int productID, String productCode, String brandName, String manufacturerCode, String productName, double retailPrice, SizeRatio modellingScale, int stock) {
        this.productID = productID;
        this.productCode = productCode;
        this.brandName = brandName;
        this.manufacturerCode = manufacturerCode;
        this.productName = productName;
        this.retailPrice = retailPrice;
        this.modellingScale = modellingScale;
        this.stock = stock;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getManufacturerCode() {
        return manufacturerCode;
    }

    public void setManufacturerCode(String manufacturerCode) {
        this.manufacturerCode = manufacturerCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(double retailPrice) {
        this.retailPrice = retailPrice;
    }

    public SizeRatio getModellingScale() {
        return modellingScale;
    }

    public void setModellingScale(SizeRatio modellingScale) {
        this.modellingScale = modellingScale;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productID=" + productID +
                ", productCode='" + productCode + '\'' +
                ", brandName='" + brandName + '\'' +
                ", manufacturerCode='" + manufacturerCode + '\'' +
                ", productName='" + productName + '\'' +
                ", retailPrice=" + retailPrice +
                ", modellingScale=" + modellingScale +
                ", stock=" + stock +
                '}';
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
