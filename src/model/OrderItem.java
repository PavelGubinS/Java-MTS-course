package model;

public class OrderItem {
    private final String productName;
    private final int quantity;
    private final double price;

    public OrderItem(String productName, int quantity, double price) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return price * quantity;
    }

    @Override
    public String toString() {
        return productName + " x" + quantity + " (" + price + " руб.)";
    }
}