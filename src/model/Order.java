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

    public List<OrderItem> getItems() {
        return items;
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
        if (status == OrderStatus.CANCELLED || status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Нельзя назначить курьера на завершенный или отмененный заказ!");
        }
        if (deliveryMethod == null) {
            throw new IllegalStateException("Сначала выберите способ доставки!");
        }
        if (!deliveryMethod.requiresCourier()) {
            throw new IllegalStateException("Для самовывоза курьер не требуется!");
        }
        if (!courier.isAvailable()) {
            throw new IllegalStateException("Этот курьер уже занят другим заказом!");
        }
        if (status == OrderStatus.DELIVERY_METHOD_CHOSEN || status == OrderStatus.COURIER_ASSIGNED) {
            if (this.assignedCourier != null) {
                this.assignedCourier.setAvailable(true);
            }
            this.assignedCourier = courier;
            this.assignedCourier.setAvailable(false);
            this.status = OrderStatus.COURIER_ASSIGNED;
        } else {
            throw new IllegalStateException("Невозможно назначить курьера на текущем этапе.");
        }
    }

    public void updateStatus(OrderStatus newStatus) {
        // Защита №1: Если заказ уже отменен или доставлен, его статус изменять нельзя вообще
        if (this.status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Ошибка: Нельзя изменить статус! Этот заказ уже БЫЛ ОТМЕНЕН ранее.");
        }
        if (this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Ошибка: Нельзя изменить статус! Этот заказ уже БЫЛ ДОСТАВЛЕН.");
        }

        // Защита №2: Переход в статус CANCELLED из рабочих статусов
        if (newStatus == OrderStatus.CANCELLED) {
            if (this.assignedCourier != null) {
                this.assignedCourier.setAvailable(true);
            }
            animateStatusTransition(this.status, newStatus);
            this.status = newStatus;
            return;
        }

        // Защита №3: Строгие переходы по линейной цепочке
        switch (newStatus) {
            case IN_TRANSIT:
                if (status == OrderStatus.COURIER_ASSIGNED ||
                        (status == OrderStatus.DELIVERY_METHOD_CHOSEN && !deliveryMethod.requiresCourier())) {
                    animateStatusTransition(this.status, newStatus);
                    this.status = newStatus;
                } else {
                    throw new IllegalArgumentException("Ошибка перехода: Перед отправкой необходимо назначить курьера (или выбрать самовывоз)!");
                }
                break;
            case DELIVERED:
                if (status == OrderStatus.IN_TRANSIT) {
                    animateStatusTransition(this.status, newStatus);
                    this.status = newStatus;
                    if (this.assignedCourier != null) {
                        this.assignedCourier.setAvailable(true);
                    }
                } else {
                    throw new IllegalArgumentException("Ошибка перехода: Нельзя завершить доставку заказа, который еще не отправлен в путь (IN_TRANSIT)!");
                }
                break;
            default:
                throw new IllegalArgumentException("Неверный или нелинейный переход статуса.");
        }
    }

    private void animateStatusTransition(OrderStatus from, OrderStatus to) {
        System.out.print("\nИзменение статуса [" + from + " -> " + to + "]: ");
        try {
            for (int i = 0; i < 10; i++) {
                Thread.sleep(150);
                System.out.print("■");
            }
            System.out.println(" Успешно!\n");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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

    public Courier getAssignedCourier() {
        return assignedCourier;
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
