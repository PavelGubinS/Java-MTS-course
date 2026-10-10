package ui;

import delivery.DeliveryMethod;
import delivery.ExpressDelivery;
import delivery.PickupDelivery;
import delivery.StandardDelivery;
import model.Client;
import model.Courier;
import model.Order;
import model.OrderItem;
import model.OrderStatus;
import model.Product;
import service.OrderService;

import java.util.List;
import java.util.Scanner;

public class ConsoleInterface {
    private final OrderService orderService = new OrderService();
    private final Scanner scanner = new Scanner(System.in);

    public void start() {
        while (true) {
            System.out.println("\n=== СИМУЛЯТОР СЛУЖБЫ ДОСТАВКИ ===");
            System.out.println("1. Создать новый заказ");
            System.out.println("2. Показать все заказы");
            System.out.println("3. Выбрать способ доставки для заказа");
            System.out.println("4. Назначить курьера на заказ");
            System.out.println("5. Изменить статус заказа");
            System.out.println("6. Выход");
            System.out.print("Выберите действие (1-6): ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    createOrder();
                    break;
                case "2":
                    showOrders();
                    break;
                case "3":
                    chooseDelivery();
                    break;
                case "4":
                    assignCourier();
                    break;
                case "5":
                    changeStatus();
                    break;
                case "6":
                    System.out.println("Программа завершена.");
                    return;
                default:
                    System.out.println("Ошибка: неверный пункт меню!");
            }
            System.out.println("-----------------------------------------");
        }
    }

    private void createOrder() {
        System.out.print("Введите имя клиента: ");
        String name = scanner.nextLine().trim();

        System.out.print("Введите телефон (РФ/КЗ: +7..., РБ: +375...): ");
        String phone = scanner.nextLine().trim();
        if (!Client.isValidPhone(phone)) {
            System.out.println("Отменено: неверный формат телефона.");
            return;
        }

        System.out.print("Введите адрес доставки: ");
        String address = scanner.nextLine().trim();

        if (name.isEmpty() || address.isEmpty()) {
            System.out.println("Отменено: имя и адрес обязательны.");
            return;
        }

        System.out.println("\nТовары на складе:");
        List<Product> products = orderService.getWarehouse();
        for (int i = 0; i < products.size(); i++) {
            System.out.println((i + 1) + ". " + products.get(i));
        }
        System.out.print("Выберите номер товара: ");

        int prodChoice;
        try {
            prodChoice = Integer.parseInt(scanner.nextLine().trim()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Отменено: введите число.");
            return;
        }
        if (prodChoice < 0 || prodChoice >= products.size()) {
            System.out.println("Отменено: неверный номер товара.");
            return;
        }
        Product selectedProduct = products.get(prodChoice);

        System.out.print("Введите количество: ");
        int qty;
        try {
            qty = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Отменено: введите целое число.");
            return;
        }
        if (qty <= 0) {
            System.out.println("Количество должно быть больше 0.");
            return;
        }

        if (!selectedProduct.decreaseQuantity(qty)) {
            System.out.println("Отменено: на складе только " + selectedProduct.getAvailableQuantity() + " шт.");
            return;
        }

        Client client = new Client(name, phone);
        Order order = new Order(orderService.getNextId(), client, address);
        order.addItem(new OrderItem(selectedProduct.getName(), qty, selectedProduct.getPrice()));
        orderService.registerOrder(order);

        System.out.println("Успех: заказ №" + order.getId() + " создан.");
    }

    private void showOrders() {
        List<Order> orders = orderService.getAllOrders();
        if (orders.isEmpty()) {
            System.out.println("Заказов нет.");
            return;
        }
        System.out.println("\n--- СПИСОК ЗАКАЗОВ ---");
        for (Order order : orders) {
            System.out.println(order);
        }
    }

    private void chooseDelivery() {
        Order order = selectOrder();
        if (order == null) {
            return;
        }

        System.out.println("Способы доставки:");
        System.out.println("1. Стандартная курьером");
        System.out.println("2. Экспресс");
        System.out.println("3. Самовывоз");
        System.out.print("Ваш выбор: ");
        String choice = scanner.nextLine().trim();

        DeliveryMethod method;
        switch (choice) {
            case "1":
                method = new StandardDelivery();
                break;
            case "2":
                method = new ExpressDelivery();
                break;
            case "3":
                method = new PickupDelivery();
                break;
            default:
                System.out.println("Ошибка: неверный выбор.");
                return;
        }

        if (!order.setDeliveryMethod(method)) {
            System.out.println("Ошибка: нельзя изменить способ доставки на текущем этапе.");
            return;
        }
        System.out.println("Способ доставки применён к заказу №" + order.getId());
    }

    private void assignCourier() {
        Order order = selectOrder();
        if (order == null) {
            return;
        }

        System.out.println("\nДоступные курьеры:");
        List<Courier> couriers = orderService.getCouriers();
        for (int i = 0; i < couriers.size(); i++) {
            System.out.println((i + 1) + ". " + couriers.get(i));
        }
        System.out.print("Выберите номер курьера: ");

        int courierChoice;
        try {
            courierChoice = Integer.parseInt(scanner.nextLine().trim()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: введите число.");
            return;
        }
        if (courierChoice < 0 || courierChoice >= couriers.size()) {
            System.out.println("Ошибка: неверный номер.");
            return;
        }

        Courier courier = couriers.get(courierChoice);
        String error = order.assignCourier(courier);
        if (error != null) {
            System.out.println("Ошибка: " + error);
        } else {
            System.out.println("Курьер " + courier.getName() + " назначен на заказ №" + order.getId());
        }
    }

    private void changeStatus() {
        Order order = selectOrder();
        if (order == null) {
            return;
        }

        System.out.println("Выберите новый статус:");
        System.out.println("1. В пути (IN_TRANSIT)");
        System.out.println("2. Доставлено (DELIVERED)");
        System.out.println("3. Отменить заказ (CANCELLED)");
        System.out.print("Ваш выбор: ");
        String choice = scanner.nextLine().trim();

        OrderStatus nextStatus;
        switch (choice) {
            case "1":
                nextStatus = OrderStatus.IN_TRANSIT;
                break;
            case "2":
                nextStatus = OrderStatus.DELIVERED;
                break;
            case "3":
                nextStatus = OrderStatus.CANCELLED;
                break;
            default:
                System.out.println("Ошибка: неверный выбор.");
                return;
        }

        String error = order.updateStatus(nextStatus);
        if (error != null) {
            System.out.println("Ошибка: " + error);
            return;
        }

        if (nextStatus == OrderStatus.CANCELLED) {
            orderService.returnProductsToWarehouse(order);
            System.out.println("Товары возвращены на склад.");
        }
        System.out.println("Статус изменён на: " + order.getStatus());
    }

    private Order selectOrder() {
        List<Order> orders = orderService.getAllOrders();
        if (orders.isEmpty()) {
            System.out.println("Заказов нет. Сначала создайте заказ.");
            return null;
        }

        System.out.println("\n--- ВЫБОР ЗАКАЗА ---");
        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);
            String status = o.getStatus().toString();
            String clientName = o.getClient().getName();
            double cost = o.getTotalCost();
            System.out.printf("%d. Заказ №%d | Клиент: %s | Статус: %s | Сумма: %.2f руб.%n",
                    (i + 1), o.getId(), clientName, status, cost);
        }

        System.out.print("Введите номер заказа из списка (1-" + orders.size() + "): ");
        int index;
        try {
            index = Integer.parseInt(scanner.nextLine().trim()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: введите число.");
            return null;
        }

        if (index < 0 || index >= orders.size()) {
            System.out.println("Ошибка: неверный номер заказа.");
            return null;
        }

        return orders.get(index);
    }
}