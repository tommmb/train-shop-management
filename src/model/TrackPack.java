package model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TrackPack extends Product {
    private TrackType type;
    private LinkedHashMap<String, Integer> trackPieces;

    public enum TrackType {
        SECOND_RADIUS_STARTER_OVAL,
        THIRD_RADIUS_STARTER_OVAL,
        TRACK_PACK_A,
        TRACK_PACK_B
    }

    public TrackPack(int productID, String productCode, String brandName, String manufacturerCode, String productName, double retailPrice, SizeRatio modellingScale, int stock, TrackType type) {
        super(productID, productCode, brandName, manufacturerCode, productName, retailPrice, modellingScale, stock);
        this.type = type;
        this.trackPieces = new LinkedHashMap<>();

        if (type.equals(TrackType.SECOND_RADIUS_STARTER_OVAL)) {
            trackPieces.put("2nd Radius Double Curve", 8);
            trackPieces.put("Single Straight", 2);
        } else if (type.equals(TrackType.THIRD_RADIUS_STARTER_OVAL)) {
            trackPieces.put("3rd Radius Double Curve", 8);
            trackPieces.put("Single Straight", 2);
        } else if (type.equals(TrackType.TRACK_PACK_A)) {
            trackPieces.put("Single Straight", 1);
            trackPieces.put("Double Straight", 1);
            trackPieces.put("2nd Radius Single Curve", 2);
            trackPieces.put("Left-Hand Point", 1);
            trackPieces.put("Buffer Stop", 1);
        } else if (type.equals(TrackType.TRACK_PACK_B)) {
            trackPieces.put("Single Straight", 2);
            trackPieces.put("2nd Radius Single Curve", 4);
            trackPieces.put("2nd Radius Double Curve", 1);
            trackPieces.put("Right-Hand Point", 1);
            trackPieces.put("Buffer Stop", 1);
        }
    }

    public TrackType getType() {
        return type;
    }

    public void setType(TrackType type) {
        this.type = type;
    }

    public LinkedHashMap<String, Integer> getTrackPieces() {
        return trackPieces;
    }

    @Override
    public String toString() {

        return super.toString().replace("}", "").replace("Product", "TrackPack") +
                ", trackType='" + type + '\'' +
                ", trackPieceMap='" + trackPieces.toString() + '\'' +
                '}';
    }
}
