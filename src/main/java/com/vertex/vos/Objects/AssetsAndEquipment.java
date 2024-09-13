package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class AssetsAndEquipment {
    private int id;
    private String itemImage;
    private String itemName;
    private int quantity;
    private int department;
    private int employee;
    private double costPerItem;
    private double total;
    private int condition;
    private int lifeSpan;
    private int encoder;

    public AssetsAndEquipment() {

    }

    public AssetsAndEquipment(int id, String itemImage, String itemName, int quantity, int department, int employee, double costPerItem, double total, int condition, int lifeSpan, int encoder, LocalDateTime dateAcquired) {
        this.id = id;
        this.itemImage = itemImage;
        this.itemName = itemName;
        this.quantity = quantity;
        this.department = department;
        this.employee = employee;
        this.costPerItem = costPerItem;
        this.total = total;
        this.condition = condition;
        this.lifeSpan = lifeSpan;
        this.encoder = encoder;
        this.dateAcquired = dateAcquired;
    }

    private LocalDateTime dateAcquired;
}
