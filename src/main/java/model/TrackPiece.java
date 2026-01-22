package model;

public class TrackPiece extends Product {
    private String radius;


    public TrackPiece(int productID, String productCode, String brandName, String manufacturerCode, String productName, double retailPrice, SizeRatio modellingScale, int stock, String radius) {
        super(productID, productCode, brandName, manufacturerCode, productName, retailPrice, modellingScale, stock);
        this.radius = radius;
    }

    public TrackPiece(int productID, String productCode, String brandName, String manufacturerCode, String productName, double retailPrice, SizeRatio modellingScale, int stock) {
        super(productID, productCode, brandName, manufacturerCode, productName, retailPrice, modellingScale, stock);
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }

    @Override
    public String toString() {
        return super.toString().replace("}", "").replace("Product", "TrackPiece") +
                (", radius='" + radius + "'}");
    }
}
