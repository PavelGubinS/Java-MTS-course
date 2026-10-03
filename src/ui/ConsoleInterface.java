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
            System.out.println("\n=== DELIVERY SERVICE SIMULATOR ===");
            System.out.println("1. Create new order");
            System.out.println("2. Show all orders");
            System.out.println("3. Choose delivery method");
            System.out.println("4. Assign courier to order");
            System.out.println("5. Change order status");
            System.out.println("6. Exit");
            System.out.print("Select action (1-6): ");

            String input = scanner.nextLine().trim();
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
                        System.out.println("Program finished.");
                        return;
                    default:
                        System.out.println("Error: Invalid menu item!");
                }
            } catch (Exception e) {
                System.out.println("Execution error: " + e.getMessage());
            }
            System.out.println("-----------------------------------------");
        }
    }

    private void createOrder() {
        System.out.print("Enter client name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter client phone: ");
        String phone = scanner.nextLine().trim();
        System.out.print("Enter delivery address: ");
        String address = scanner.nextLine().trim();

        if (name.isEmpty() || address.isEmpty()) {
            System.out.println("Cancelled: Name and address are required.");
            return;
        }

        System.out.print("Enter product name: ");
        String item = scanner.nextLine().trim();
        if (item.isEmpty()) {
            System.out.println("Cancelled: Cannot create order without product.");
            return;
        }

        int qty = 0;
        while (true) {
            System.out.print("Enter quantity (integer): ");
            try {
                qty = Integer.parseInt(scanner.nextLine().trim());
                if (qty <= 0) {
                    System.out.println("Quantity must be greater than 0.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Error: Enter a valid integer.");
            }
        }

        double price = 0.0;
        while (true) {
            System.out.print("Enter price per unit: ");
            try {
                price = Double.parseDouble(scanner.nextLine().trim());
                if (price < 0) {
                    System.out.println("Price cannot be negative.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Error: Enter a valid number for price.");
            }
        }

        Client client = new Client(name, phone);
        Order order = new Order(orderService.getNextId(), client, address);
        order.addItem(new OrderItem(item, qty, price));

        // Сохранение заказа в список
        orderService.registerOrder(order);
        System.out.println("Success: Order #" + order.getId() + " created successfully.");
    }

    private void showOrders() {
        if (orderService.getAllOrders().isEmpty()) {
            System.out.println("No orders found.");
            return;
        }
        System.out.println("\n--- ORDERS LIST ---");
        orderService.getAllOrders().forEach(System.out::println);
    }

    private void chooseDelivery() {
        Order order = selectOrder();
        if (order == null)
            return;

        System.out.println("Available delivery methods:");
        System.out.println("1. Standard courier delivery (150 rub)");
        System.out.println("2. Express delivery (350 rub)");
        System.out.println("3. Pickup (0 rub)");
        System.out.print("Your choice: ");
        String choice = scanner.nextLine().trim();

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
                throw new IllegalArgumentException("Invalid method choice.");
        }

        order.setDeliveryMethod(method);
        System.out.println("Method applied to order #" + order.getId());
    }

    private void assignCourier() {
        Order order = selectOrder();
        if (order == null)
            return;

        System.out.print("Enter courier name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter courier phone: ");
        String phone = scanner.nextLine().trim();

        order.assignCourier(new Courier(name, phone));
        System.out.println("Courier assigned to order #" + order.getId());
    }

    private void changeStatus() {
        Order order = selectOrder();
        if (order == null)
            return;

        System.out.println("Select new status:");
        System.out.println("1. In transit (IN_TRANSIT)");
        System.out.println("2. Delivered (DELIVERED)");
        System.out.println("3. Cancel order (CANCELLED)");
        System.out.print("Your choice: ");
        String choice = scanner.nextLine().trim();

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
                throw new IllegalArgumentException("Invalid status choice.");
        }

        order.updateStatus(nextStatus);
        System.out.println("Status updated to: " + order.getStatus());
    }

    private Order selectOrder() {
        System.out.print("Enter order ID: ");
        try {
            String idInput = scanner.nextLine().trim();
            int id = Integer.parseInt(idInput);
            return orderService.findOrderById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Order with ID " + id + " does not exist."));
        } catch (Exception e) {
            System.out.println("Error: ID must be a number!");
            return null;
        }
    }
}
