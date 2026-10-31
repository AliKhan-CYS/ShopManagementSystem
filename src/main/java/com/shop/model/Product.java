package com.shop.model;

public class Product {

    private final int id;
    private final String name;
    private final double price;
    private final int quantity;
    private final String category;

    public Product(String name, double price, int quantity, String category) {
        this(0, name, price, quantity, category);
    }

    public Product(int id, String name, double price, int quantity, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getCategory() { return category; }

    @Override
    public String toString() {
        return name;
    }
}
