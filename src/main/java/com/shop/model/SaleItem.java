package com.shop.model;

public class SaleItem {

    private final int productId;
    private final int quantity;
    private final double price;

    public SaleItem(int productId, int quantity, double price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }
}
