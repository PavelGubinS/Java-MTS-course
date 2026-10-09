package delivery;

// Паттерн "Стратегия" и полиморфизм в деле
public interface DeliveryMethod {
    double calculateCost(double baseOrderCost);

    int estimateDeliveryTime();

    boolean requiresCourier();

    String getName();
}

// Использовать base cost нужно в расчётах