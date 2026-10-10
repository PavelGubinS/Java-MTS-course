package model;

public class Courier {
    private final String name;
    private final String phone;
    private boolean isAvailable;

    public Courier(String name, String phone) {
        this.name = name;
        this.phone = phone;
        this.isAvailable = true;
    }

    public String getName() {
        return name;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        this.isAvailable = available;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) [%s]", name, phone, isAvailable ? "Свободен" : "Занят");
    }
}