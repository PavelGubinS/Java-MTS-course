package delivery;

public class StandardDelivery implements DeliveryMethod {
    @Override
    public double calculateCost(double baseOrderCost) {
        return baseOrderCost >= 1500.0 ? 0.0 : 150.0;
    }

    @Override
    public int estimateDeliveryTime() {
        return 120;
    }

    @Override
    public boolean requiresCourier() {
        return true;
    }

    @Override
    public String getName() {
        return "Стандартная доставка курьером";
    }
}