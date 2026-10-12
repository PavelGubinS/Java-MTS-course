package model;

public class Client {
    private final String name;
    private final String phone;

    public Client(String name, String phone) {
        if (!isValidPhone(phone)) {
            throw new IllegalArgumentException("Неверный формат телефона");
        }
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        String clean = phone.replaceAll("[\\s\\-()]", "");
        return clean.matches("\\+?[78]\\d{10}");
    }
}