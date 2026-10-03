package service;

import model.Order;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderService {
    private final List<Order> orders = new ArrayList<>();
    private int currentId = 1;

    public void registerOrder(Order order) {
        orders.add(order);
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders);
    }

    public Optional<Order> findOrderById(int id) {
        return orders.stream().filter(o -> o.getId() == id).findFirst();
    }

    public int getNextId() {
        return currentId++;
    }
}
