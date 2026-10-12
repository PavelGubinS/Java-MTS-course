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

    private String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Ошибка: поле не может быть пустым. Попробуйте снова.");
        }
    }

    private String readValidPhone(String prompt) {
        System.out.println("Допустимые форматы телефона:");
        System.out.println("  +7(XXX)XXX-XX-XX - например, +7(921)123-45-67");
        System.out.println("  +7XXXXXXXXXX     - например, +79211234567");
        System.out.println("  8XXXXXXXXXX      - например, 89211234567");

        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (Client.isValidPhone(value)) {
                return value;
            }
            System.out.println("Ошибка: неверный формат телефона. Попробуйте снова.");
        }
    }

    private int readIntInRange(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Ошибка: число должно быть в диапазоне от " + min + " до " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число.");
            }
        }
    }

    private boolean askYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String answer = scanner.nextLine().trim();
            if (answer.equals("1")) {
                return true;
            }
            if (answer.equals("2")) {
                return false;
            }
            System.out.println("Ошибка: введите 1 (да) или 2 (нет).");
        }
    }

    private void createOrder() {
        String name = readNonEmptyString("Введите имя клиента: ");
        String phone = readValidPhone("Введите телефон: ");
        String address = readNonEmptyString("Введите адрес доставки: ");

        Client client = new Client(name, phone);
        Order order = new Order(orderService.getNextId(), client, address);

        boolean addMore = true;
        while (addMore) {
            OrderItem item = readOrderItem();
            if (item != null) {
                order.addItem(item);
            }
            addMore = askYesNo("Добавить ещё позицию? (1 - да, 2 - нет): ");
        }

        if (order.getItems().isEmpty()) {
            System.out.println("Отменено: заказ должен содержать хотя бы одну позицию.");
            return;
        }

        orderService.registerOrder(order);
        System.out.println("Успех: заказ №" + order.getId() + " создан.");
    }

    private OrderItem readOrderItem() {
        List<Product> products = orderService.getWarehouse();

        boolean anyAvailable = false;
        for (Product p : products) {
            if (p.getAvailableQuantity() > 0) {
                anyAvailable = true;
                break;
            }
        }
        if (!anyAvailable) {
            System.out.println("На складе закончились все товары.");
            return null;
        }

        System.out.println("\nТовары на складе:");
        for (int i = 0; i < products.size(); i++) {
            System.out.println((i + 1) + ". " + products.get(i));
        }

        int prodChoice = readIntInRange(
                "Выберите номер товара (1-" + products.size() + "): ",
                1, products.size());
        Product selected = products.get(prodChoice - 1);

        if (selected.getAvailableQuantity() == 0) {
            System.out.println("Ошибка: этот товар отсутствует на складе.");
            return null;
        }

        int qty = readIntInRange(
                "Введите количество (1-" + selected.getAvailableQuantity() + "): ",
                1, selected.getAvailableQuantity());

        if (!selected.decreaseQuantity(qty)) {
            System.out.println("Ошибка: не удалось списать товар со склада.");
            return null;
        }

        return new OrderItem(selected.getName(), qty, selected.getPrice());
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

        int choice = readIntInRange("Ваш выбор: ", 1, 3);

        DeliveryMethod method;
        switch (choice) {
            case 1:
                method = new StandardDelivery();
                break;
            case 2:
                method = new ExpressDelivery();
                break;
            case 3:
                method = new PickupDelivery();
                break;
            default:
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

        int courierChoice = readIntInRange(
                "Выберите номер курьера (1-" + couriers.size() + "): ",
                1, couriers.size());
        Courier courier = couriers.get(courierChoice - 1);

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

        OrderStatus current = order.getStatus();

        if (current == OrderStatus.DELIVERED) {
            System.out.println("Заказ уже доставлен. Действия невозможны.");
            return;
        }
        if (current == OrderStatus.CANCELLED) {
            System.out.println("Заказ отменён. Действия невозможны.");
            return;
        }

        boolean canGoInTransit = current == OrderStatus.COURIER_ASSIGNED
                || (current == OrderStatus.DELIVERY_METHOD_CHOSEN
                        && order.getDeliveryMethod() != null
                        && !order.getDeliveryMethod().requiresCourier());
        boolean canBeDelivered = current == OrderStatus.IN_TRANSIT;

        System.out.println("Текущий статус: " + current);
        System.out.println("Доступные действия:");

        int optInTransit = -1;
        int optDelivered = -1;
        int option = 1;

        if (canGoInTransit) {
            System.out.println(option + ". Отправить в путь (IN_TRANSIT)");
            optInTransit = option;
            option++;
        }
        if (canBeDelivered) {
            System.out.println(option + ". Отметить как доставленный (DELIVERED)");
            optDelivered = option;
            option++;
        }
        System.out.println(option + ". Отменить заказ (CANCELLED)");
        int optCancel = option;

        int choice = readIntInRange("Ваш выбор: ", 1, optCancel);

        OrderStatus nextStatus;
        if (choice == optInTransit) {
            nextStatus = OrderStatus.IN_TRANSIT;
        } else if (choice == optDelivered) {
            nextStatus = OrderStatus.DELIVERED;
        } else {
            nextStatus = OrderStatus.CANCELLED;
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
            System.out.printf("%d. Заказ №%d | Клиент: %s | Статус: %s | Сумма: %.2f руб.%n",
                    (i + 1), o.getId(), o.getClient().getName(),
                    o.getStatus(), o.getTotalCost());
        }

        int index = readIntInRange(
                "Введите номер заказа (1-" + orders.size() + "): ",
                1, orders.size());

        return orders.get(index - 1);
    }
}