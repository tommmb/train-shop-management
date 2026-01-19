package model;

public class Locomotive extends RollingStock {
    private LocomotiveModel locomotiveModel;
    public enum LocomotiveModel {
        ANALOGUE,
        DCC_READY,
        DCC_FITTED,
        DCC_SOUND
    }

    public Locomotive(int productID, String productCode, String brandName, String manufacturerCode, String productName, double retailPrice, SizeRatio modellingScale, int stock, Era startEra, Era endEra, LocomotiveModel locomotiveModel) {
        super(productID, productCode, brandName, manufacturerCode, productName, retailPrice, modellingScale, stock, startEra, endEra);
        this.locomotiveModel = locomotiveModel;
    }

    public LocomotiveModel getLocomotiveModel() {
        return locomotiveModel;
    }

    public void setLocomotiveModel(LocomotiveModel locomotiveModel) {
        this.locomotiveModel = locomotiveModel;
    }

    @Override
    public String toString() {
        return super.toString().replace("}", "").replace("Product", "Locomotive") +
            ", startEra='" + getStartEra() + '\'' +
            ", endEra='" + getEndEra() + '\'' +
            ", locomotiveModel='" + locomotiveModel + '\'' +
            '}';
    }
}
