package com.vertex.vos.Constructors;

public class Tax {
    private double withholdingRate;
    private double vatRate;

    public Tax(double withholdingRate, double vatRate) {
        this.withholdingRate = withholdingRate;
        this.vatRate = vatRate;
    }

    public Tax(){

    }

    public double getWithholdingRate() {
        return withholdingRate;
    }

    public void setWithholdingRate(double withholdingRate) {
        this.withholdingRate = withholdingRate;
    }

    public double getVatRate() {
        return vatRate;
    }

    public void setVatRate(double vatRate) {
        this.vatRate = vatRate;
    }
}
