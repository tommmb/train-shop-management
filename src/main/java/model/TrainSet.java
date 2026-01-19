package model;

import java.util.LinkedHashMap;

public class TrainSet extends Product {
    private int trainSetID;
    private LinkedHashMap<Product, Integer> products;

    public TrainSet(int productID, String productCode, String brandName, String manufacturerCode, String productName, double retailPrice, SizeRatio modellingScale, int stock, int trainSetID, LinkedHashMap<Product, Integer> products) {
        super(productID, productCode, brandName, manufacturerCode, productName, retailPrice, modellingScale, stock);
        this.trainSetID = trainSetID;
        this.products = products;
    }

    public TrainSet(int productID, String productCode, String brandName, String manufacturerCode, String productName, double retailPrice, SizeRatio modellingScale, int stock, int trainSetID) {
        super(productID, productCode, brandName, manufacturerCode, productName, retailPrice, modellingScale, stock);
        this.trainSetID = trainSetID;
    }

    public int getTrainSetID() {
        return trainSetID;
    }

    public void setTrainSetID(int trainSetID) {
        this.trainSetID = trainSetID;
    }

    public LinkedHashMap<Product, Integer> getProducts() {
        return products;
    }

    public void setProducts(LinkedHashMap<Product, Integer> products) {
        this.products = products;
    }

    @Override
    public String toString() {
        return super.toString().replace("}", "").replace("Product", "TrainSet") +
                ", trainSetID='" + trainSetID + '\'' +
                ", products='" + products + '\'' +
                '}';
    }

    public void addProduct(Product product, int quantity) {
        this.products.put(product, quantity);
    }
}
