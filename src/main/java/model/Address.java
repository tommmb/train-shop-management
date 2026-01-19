package model;

public class Address {
    private String postcode;
    private int houseNumber;
    private String roadName;
    private String cityName;
    private String county;

    public Address(String postcode, int houseNumber, String roadName, String cityName, String county) {
        this.postcode = postcode;
        this.houseNumber = houseNumber;
        this.roadName = roadName;
        this.cityName = cityName;
        this.county = county;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public int getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(int houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }



    @Override
    public String toString() {
        return "{ " +
                " postcode='" + getPostcode() + '\'' +
                ", houseNumber=" + getHouseNumber() +
                ", roadName='" + getRoadName() + '\'' +
                ", cityName='" + getCityName() + '\'' +
                '}';
    }
}
