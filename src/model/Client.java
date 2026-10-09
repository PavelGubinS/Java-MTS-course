package model;

public class Client {
    private final String name;
    private final String phone;

    public Client(String name, String phone) {
        if (!isValidPhone(phone)) {
            throw new IllegalArgumentException(
                    "Неверный формат телефона! Поддерживаются РФ/КЗ (11 цифр) и РБ (12 цифр).");
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

    /**
     * Валидация номеров СНГ со строгим подсчетом количества цифр:
     * 1. РФ и Казахстан: начало на +7 или 8, затем ровно 10 цифр (напр.
     * +79991112233 или 89991112233)
     * 2. Беларусь: начало на +375 или 375, затем ровно 9 цифр (напр. +375291112233)
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }
        // Удаляем все пробелы и дефисы, если пользователь их случайно ввёл
        String cleanPhone = phone.replaceAll("[\\s\\-()]", "");

        // Регулярное выражение для проверки форматов РФ/КЗ и РБ
        return cleanPhone.matches("^(\\+7|8)\\d{10}$") || cleanPhone.matches("^(\\+?375)\\d{9}$");
    }
}
