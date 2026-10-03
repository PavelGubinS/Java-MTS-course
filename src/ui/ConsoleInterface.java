package ui;

import delivery.*;
import model.*;
import service.OrderService;
import java.util.Scanner;

public class ConsoleInterface {
    private final OrderService orderService = new OrderService();
    private final Scanner scanner = new Scanner(System.in);

    public void start() {
        while (true) {
            System.out.println("=== СИМУЛЯТОР СЛУЖБЫ ДОСТАВКИ ===");
            System.out.println("1. Создать новый заказ и добавить позиции");
            System.out.println("2. Показать список всех заказов");
            System.out.println("3. Выбрать способ доставки для заказа");
            System.out.println("4. Назначить курьера на заказ");
            System.out.println("5. Изменить статус заказа (Бизнес-цепочка)");
            System.out.println("6. Завершить работу");
            System.out.print("Выберите действие (1-6): ");

            String input = scanner.nextLine();
            try {
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
                        System.out.println("Работа программы завершена.");
                        return;
                    default:
                        System.out.println("Ошибка: Неверный пункт меню!");
                }
            } catch (Exception e) {
                System.out.println("Ошибка выполнения: " + e.getMessage());
            }
            System.out.println("\n-----------------------------------------");
        }
    }

    private void createOrder() {
        System.out.print("Введите имя клиента: ");
        String name = scanner.nextLine();
        System.out.print("Введите телефон клиента: ");
        String phone = scanner.nextLine();
        System.out.print("Введите адрес доставки: ");
        String address = scanner.nextLine();

        Client client = new Client(name, phone);
        Order order = new Order(orderService.getNextId(), client, address);

        while (true) {
            System.out.print("Введите название товара (или 'стоп' для завершения): ");
            String item = scanner.nextLine();
            if (item.equalsIgnoreCase("стоп"))
                break;

            System.out.print("Введите количество (целое число): ");
            int qty = Integer.parseInt(scanner.nextLine());

            System.out.print("Введите цену за единицу: ");
            double price = Double.parseDouble(scanner.nextLine());

            order.addItem(new OrderItem(item, qty, price));
        }

        orderService.registerOrder(order);
        System.out.println("Успех: Заказ №" + order.getId() + " сохранен в памяти.");
    }

    private void showOrders() {
        if (orderService.getAllOrders().isEmpty()) {
            System.out.println("Заказов пока нет.");
            return;
        }
        orderService.getAllOrders().forEach(System.out::println);
    }

    private void chooseDelivery() {
        Order order = selectOrder();
        if (order == null)
            return;

        System.out.println("Доступные способы доставки:");
        System.out.println("1. Стандартная доставка курьером (150 руб.)");
        System.out.println("2. Экспресс-доставка (350 руб.)");
        System.out.println("3. Самовывоз (0 руб.)");
        System.out.print("Ваш выбор: ");
        String choice = scanner.nextLine();

        DeliveryMethod method = null;
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
                throw new IllegalArgumentException("Неверный выбор способа.");
        }

        order.setDeliveryMethod(method);
        System.out.println("Способ успешно применен к заказу №" + order.getId());
    }

    private void assignCourier() {
        Order order = selectOrder();
        if (order == null)
            return;

        System.out.print("Введите имя курьера: ");
        String name = scanner.nextLine();
        System.out.print("Введите телефон курьера: ");
        String phone = scanner.nextLine();

        order.assignCourier(new Courier(name, phone));
        System.out.println("Курьер успешно назначен на заказ №" + order.getId());
    }

    private void changeStatus() {
        Order order = selectOrder();
        if (order == null)
            return;

        System.out.println("Выберите новый статус:");
        System.out.println("1. Передать в доставку / В пути (IN_TRANSIT)");
        System.out.println("2. Завершить / Доставлен (DELIVERED)");
        System.out.println("3. Отменить заказ (CANCELLED)");
        System.out.print("Ваш выбор: ");
        String choice = scanner.nextLine();

        OrderStatus nextStatus = null;
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
                throw new IllegalArgumentException("Неверный выбор статуса.");
        }

        order.updateStatus(nextStatus);
        System.out.println("Статус заказа успешно обновлен на: " + order.getStatus());
    }

    private Order selectOrder() {
        System.out.print("Введите ID заказа для выполнения операции: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            return orderService.findOrderById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ с ID " + id + " не существует."));
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: ID должен быть числом!");
            return null;
        }
    }
}
