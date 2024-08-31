package com.vertex.vos.Objects;

public class AreaPerCluster {
    private int id;
    private int clusterId;
    private String province;
    private String city;
    private String baranggay;

    public AreaPerCluster() {
    }

    public AreaPerCluster(int id, int clusterId, String province, String city, String baranggay) {
        this.id = id;
        this.clusterId = clusterId;
        this.province = province;
        this.city = city;
        this.baranggay = baranggay;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getBaranggay() {
        return baranggay;
    }

    public void setBaranggay(String baranggay) {
        this.baranggay = baranggay;
    }

    @Override
    public String toString() {
        return "AreaPerCluster{" +
                "id=" + id +
                ", clusterId=" + clusterId +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", baranggay='" + baranggay + '\'' +
                '}';
    }
}
