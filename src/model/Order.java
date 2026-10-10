package model;

import delivery.DeliveryMethod;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private final int id;
    private final Client client;
    private final String deliveryAddress;
    private final List<OrderItem> items = new ArrayList<>();

    private DeliveryMethod deliveryMethod;
    private Courier assignedCourier;
    private OrderStatus status;

    public Order(int id, Client client, String deliveryAddress) {
        this.id = id;
        this.client = client;
        this.deliveryAddress = deliveryAddress;
        this.status = OrderStatus.CREATED;
    }

    public boolean addItem(OrderItem item) {
        if (status != OrderStatus.CREATED) {
            return false;
        }
        items.add(item);
        return true;
    }

    public boolean setDeliveryMethod(DeliveryMethod method) {
        if (status != OrderStatus.CREATED && status != OrderStatus.DELIVERY_METHOD_CHOSEN) {
            return false;
        }
        this.deliveryMethod = method;
        this.status = OrderStatus.DELIVERY_METHOD_CHOSEN;
        return true;
    }

    public String assignCourier(Courier courier) {
        if (status == OrderStatus.CANCELLED || status == OrderStatus.DELIVERED) {
            return "Нельзя назначить курьера на завершённый или отменённый заказ";
        }
        if (deliveryMethod == null) {
            return "Сначала выберите способ доставки";
        }
        if (!deliveryMethod.requiresCourier()) {
            return "Для самовывоза курьер не требуется";
        }
        if (!courier.isAvailable()) {
            return "Этот курьер уже занят другим заказом";
        }
        if (status != OrderStatus.DELIVERY_METHOD_CHOSEN && status != OrderStatus.COURIER_ASSIGNED) {
            return "Невозможно назначить курьера на текущем этапе";
        }

        if (this.assignedCourier != null) {
            this.assignedCourier.setAvailable(true);
        }
        this.assignedCourier = courier;
        this.assignedCourier.setAvailable(false);
        this.status = OrderStatus.COURIER_ASSIGNED;
        return null;
    }

    public String updateStatus(OrderStatus newStatus) {
        if (this.status == OrderStatus.CANCELLED) {
            return "Заказ уже отменён";
        }
        if (this.status == OrderStatus.DELIVERED) {
            return "Заказ уже доставлен";
        }

        if (newStatus == OrderStatus.CANCELLED) {
            if (this.assignedCourier != null) {
                this.assignedCourier.setAvailable(true);
            }
            this.status = newStatus;
            return null;
        }

        if (newStatus == OrderStatus.IN_TRANSIT) {
            boolean canGo = (status == OrderStatus.COURIER_ASSIGNED)
                    || (status == OrderStatus.DELIVERY_METHOD_CHOSEN && !deliveryMethod.requiresCourier());
            if (!canGo) {
                return "Перед отправкой нужно назначить курьера (или выбрать самовывоз)";
            }
            this.status = newStatus;
            return null;
        }

        if (newStatus == OrderStatus.DELIVERED) {
            if (status != OrderStatus.IN_TRANSIT) {
                return "Нельзя завершить заказ, который ещё не в пути";
            }
            this.status = newStatus;
            if (this.assignedCourier != null) {
                this.assignedCourier.setAvailable(true);
            }
            return null;
        }

        return "Неверный переход статуса";
    }

    public double getBaseCost() {
        double total = 0;
        for (OrderItem item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public double getTotalCost() {
        double cost = getBaseCost();
        if (deliveryMethod != null) {
            cost += deliveryMethod.calculateCost(cost);
        }
        return cost;
    }

    public int getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    @Override
    public String toString() {
        String methodName = (deliveryMethod != null) ? deliveryMethod.getName() : "Не выбран";
        String courierName = (assignedCourier != null) ? assignedCourier.getName() : "Нет";
        int time = (deliveryMethod != null) ? deliveryMethod.estimateDeliveryTime() : 0;

        return String.format(
                "Заказ №%d [%s]%n" +
                "Клиент: %s (%s) | Адрес: %s%n" +
                "Товары: %s%n" +
                "Способ доставки: %s%n" +
                "Курьер: %s%n" +
                "Стоимость товаров: %.2f руб. | Итого: %.2f руб. (Срок: %d мин.)%n",
                id, status,
                client.getName(), client.getPhone(), deliveryAddress,
                items, methodName, courierName,
                getBaseCost(), getTotalCost(), time);
    }
}