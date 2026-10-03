package model;

public class Courier {
    private final String name;
    private final String phone;

    public Courier(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
}
