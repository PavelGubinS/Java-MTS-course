package service;

import model.Order;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderService {
    // Хранилище заказов в оперативной памяти, автогенерация уникальных ID для
    // заказов
    private final List<Order> orders = new ArrayList<>();
    private int idSequence = 1;

    public void registerOrder(Order order) {
        orders.add(order);
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders); // Возвращаем копию списка во избежание внешних модификаций
    }

    // Безопасный поиск заказа по его идентификатору
    public Optional<Order> findOrderById(int id) {
        return orders.stream().filter(o -> o.getId() == id).findFirst();
    }

    public int getNextId() {
        return idSequence++;
    }
}
