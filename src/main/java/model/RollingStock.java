package model;

public class RollingStock extends Product {
    private Era startEra;
    private Era endEra;
    public enum Era {
        ERA_1,
        ERA_2,
        ERA_3,
        ERA_4,
        ERA_5,
        ERA_6,
        ERA_7,
        ERA_8,
        ERA_9,
        ERA_10,
        ERA_11
    }

    public RollingStock(int productID, String productCode, String brandName, String manufacturerCode, String productName, double retailPrice, SizeRatio modellingScale, int stock, Era startEra, Era endEra) {
        super(productID, productCode, brandName, manufacturerCode, productName, retailPrice, modellingScale, stock);
        this.startEra = startEra;
        this.endEra = endEra;
    }

    public Era getStartEra() {
        return startEra;
    }

    public void setStartEra(Era startEra) {
        this.startEra = startEra;
    }

    public Era getEndEra() {
        return endEra;
    }

    public void setEndEra(Era endEra) {
        this.endEra = endEra;
    }


}
