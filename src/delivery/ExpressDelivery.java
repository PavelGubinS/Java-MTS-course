package delivery;

public class ExpressDelivery implements DeliveryMethod {
    @Override
    public double calculateCost(double baseOrderCost) { return 350.0; }
    @Override
    public int estimateDeliveryTime() { return 40; }
    @Override
    public boolean requiresCourier() { return true; }
    @Override
    public String getName() { return "Экспресс-доставка"; }
}
