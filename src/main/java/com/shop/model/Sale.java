package com.shop.model;

public class Sale {

    private final int customerId;
    private final double total;

    public Sale(int customerId, double total) {
        this.customerId = customerId;
        this.total = total;
    }

    public int getCustomerId() {
        return customerId;
    }

    public double getTotal() {
        return total;
    }
}
