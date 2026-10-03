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

    public void addItem(OrderItem item) {
        if (status == OrderStatus.CREATED) {
            items.add(item);
        } else {
            throw new IllegalStateException("Нельзя добавлять товары. Заказ уже оформлен.");
        }
    }

    public double getBaseCost() {
        return items.stream().mapToDouble(OrderItem::getTotalPrice).sum();
    }

    public double getTotalCost() {
        double cost = getBaseCost();
        if (deliveryMethod != null) {
            cost += deliveryMethod.calculateCost(cost);
        }
        return cost;
    }

    public void setDeliveryMethod(DeliveryMethod method) {
        if (status == OrderStatus.CREATED || status == OrderStatus.DELIVERY_METHOD_CHOSEN) {
            this.deliveryMethod = method;
            this.status = OrderStatus.DELIVERY_METHOD_CHOSEN;
        } else {
            throw new IllegalStateException("Нельзя изменить способ доставки на текущем этапе.");
        }
    }

    public void assignCourier(Courier courier) {
        if (deliveryMethod == null) {
            throw new IllegalStateException("Сначала выберите способ доставки!");
        }
        if (!deliveryMethod.requiresCourier()) {
            throw new IllegalStateException("Для самовывоза курьер не требуется!");
        }
        if (status == OrderStatus.DELIVERY_METHOD_CHOSEN || status == OrderStatus.COURIER_ASSIGNED) {
            this.assignedCourier = courier;
            this.status = OrderStatus.COURIER_ASSIGNED;
        } else {
            throw new IllegalStateException("Невозможно назначить курьера на текущем этапе.");
        }
    }

    public void updateStatus(OrderStatus newStatus) {
        if (newStatus == OrderStatus.CANCELLED) {
            if (status == OrderStatus.DELIVERED) {
                throw new IllegalArgumentException("Нельзя отменить уже доставленный заказ.");
            }
            this.status = newStatus;
            return;
        }

        // Классический синтаксис switch, совместимый с Java 8
        switch (newStatus) {
            case IN_TRANSIT:
                if (status == OrderStatus.COURIER_ASSIGNED ||
                        (status == OrderStatus.DELIVERY_METHOD_CHOSEN && !deliveryMethod.requiresCourier())) {
                    this.status = newStatus;
                } else {
                    throw new IllegalArgumentException("Перед отправкой необходимо назначить курьера!");
                }
                break;
            case DELIVERED:
                if (status == OrderStatus.IN_TRANSIT) {
                    this.status = newStatus;
                } else {
                    throw new IllegalArgumentException("Нельзя завершить доставку заказа, который еще не отправлен.");
                }
                break;
            default:
                throw new IllegalArgumentException("Неверный или нелинейный переход статуса.");
        }
    }

    public int getId() {
        return id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }

    @Override
    public String toString() {
        return String.format(
                "Заказ №%d [%s]\nКлиент: %s (%s) | Адрес: %s\nТовары: %s\nСпособ доставки: %s\nКурьер: %s\nСтоимость товаров: %.2f руб. | Итого (с доставкой): %.2f руб. (Срок: %d мин.)\n",
                id, status, client.getName(), client.getPhone(), deliveryAddress, items,
                (deliveryMethod != null ? deliveryMethod.getName() : "Не выбран"),
                (assignedCourier != null ? assignedCourier.getName() : "Нет"),
                getBaseCost(), getTotalCost(), (deliveryMethod != null ? deliveryMethod.estimateDeliveryTime() : 0));
    }
}
