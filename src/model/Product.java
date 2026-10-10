package model;

public class Product {
    private final String name;
    private int availableQuantity;
    private final double price;

    public Product(String name, int availableQuantity, double price) {
        this.name = name;
        this.availableQuantity = availableQuantity;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public double getPrice() {
        return price;
    }

    public boolean decreaseQuantity(int amount) {
        if (availableQuantity >= amount) {
            availableQuantity -= amount;
            return true;
        }
        return false;
    }

    public void increaseQuantity(int amount) {
        availableQuantity += amount;
    }

    @Override
    public String toString() {
        return String.format("%s - %.2f руб. (В наличии: %d шт.)", name, price, availableQuantity);
    }
}