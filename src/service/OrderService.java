package service;

import model.Courier;
import model.Order;
import model.OrderItem;
import model.Product;

import java.util.ArrayList;
import java.util.List;

public class OrderService {
    private final List<Order> orders = new ArrayList<>();
    private final List<Product> warehouse = new ArrayList<>();
    private final List<Courier> couriers = new ArrayList<>();
    private int idSequence = 1;

    public OrderService() {
        initData();
    }

    private void initData() {
        warehouse.add(new Product("Пицца Маргарита", 5, 450.0));
        warehouse.add(new Product("Бургер Классический", 3, 320.0));
        warehouse.add(new Product("Суши Сет", 2, 1200.0));
        warehouse.add(new Product("Кола 0.5", 10, 100.0));

        couriers.add(new Courier("Иван", "89991112233"));
        couriers.add(new Courier("Алексей", "89994445566"));
        couriers.add(new Courier("Дмитрий", "89997778899"));
    }

    public void registerOrder(Order order) {
        orders.add(order);
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders);
    }

    public List<Product> getWarehouse() {
        return warehouse;
    }

    public List<Courier> getCouriers() {
        return couriers;
    }

    public Order findOrderById(int id) {
        for (Order order : orders) {
            if (order.getId() == id) {
                return order;
            }
        }
        return null;
    }

    public int getNextId() {
        return idSequence++;
    }

    public void returnProductsToWarehouse(Order order) {
        for (OrderItem item : order.getItems()) {
            for (Product product : warehouse) {
                if (product.getName().equalsIgnoreCase(item.getProductName())) {
                    product.increaseQuantity(item.getQuantity());
                    break;
                }
            }
        }
    }
}