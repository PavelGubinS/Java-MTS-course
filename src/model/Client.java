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
        if (phone == null) {
            return false;
        }

        int digitCount = 0;
        for (int i = 0; i < phone.length(); i++) {
            char c = phone.charAt(i);
            if (Character.isDigit(c)) {
                digitCount++;
            }
        }

        return digitCount >= 10 && digitCount <= 12;
    }
}