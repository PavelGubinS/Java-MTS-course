package delivery;

public class PickupDelivery implements DeliveryMethod {
    @Override
    public double calculateCost(double baseOrderCost) {
        return 0.0;
    }

    @Override
    public int estimateDeliveryTime() {
        return 15;
    }

    @Override
    public boolean requiresCourier() {
        return false;
    }

    @Override
    public String getName() {
        return "Самовывоз из пункта выдачи";
    }
}
