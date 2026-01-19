package model;

public class Controller extends Product {
    private SignalType signalType;
    public enum SignalType {
        ANALOGUE,
        DIGITAL
    }

    public Controller(int productID, String productCode, String brandName, String manufacturerCode, String productName, double retailPrice, SizeRatio modellingScale, int stock, SignalType signalType) {
        super(productID, productCode, brandName, manufacturerCode, productName, retailPrice, modellingScale, stock);
        this.signalType = signalType;
    }

    public SignalType getSignalType() {
        return signalType;
    }

    public void setSignalType(SignalType signalType) {
        this.signalType = signalType;
    }

    @Override
    public String toString() {
        return super.toString().replace("}", "").replace("Product", "Controller") +
                ", signalType='" + signalType + '\'' +

                '}';
    }
}
