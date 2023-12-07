package com.vertex.vos.Constructors;

public class ProductToPOListener implements ProductToPOInterface{
    @Override
    public void onProductAddedToPO(int productId) {
        System.out.println("Product added to purchase order with ID: " + productId);
    }
}
