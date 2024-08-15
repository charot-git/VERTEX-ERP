package com.vertex.vos.Objects;

public class Unit {
    private int unit_id;
    private String unit_name;
    private String unit_shortcut;
    private int unit_order;

    public String getUnit_shortcut() {
        return unit_shortcut;
    }

    public void setUnit_shortcut(String unit_shortcut) {
        this.unit_shortcut = unit_shortcut;
    }

    public int getUnit_order() {
        return unit_order;
    }

    public void setUnit_order(int unit_order) {
        this.unit_order = unit_order;
    }

    public Unit(int unit_id, String unit_name, String unit_shortcut, int unit_order) {
        this.unit_id = unit_id;
        this.unit_name = unit_name;
        this.unit_shortcut = unit_shortcut;
        this.unit_order = unit_order;
    }

    public int getUnit_id() {
        return unit_id;
    }

    public void setUnit_id(int unit_id) {
        this.unit_id = unit_id;
    }

    public String getUnit_name() {
        return unit_name;
    }

    public void setUnit_name(String unit_name) {
        this.unit_name = unit_name;
    }
}
