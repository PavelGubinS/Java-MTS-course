package delivery;

public interface DeliveryMethod {
    double calculateCost(double baseOrderCost);
    int estimateDeliveryTime();
    boolean requiresCourier();
    String getName();
}
