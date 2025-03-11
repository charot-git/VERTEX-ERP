package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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
