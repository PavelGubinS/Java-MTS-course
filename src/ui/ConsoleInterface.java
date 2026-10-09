package ui;

import delivery.*;
import model.*;
import service.OrderService;
import java.util.Scanner;
import java.util.List;

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

        System.out.print("Enter client phone (РФ/КЗ: +7..., РБ: +375...): ");
        String phone = scanner.nextLine().trim();
        if (!Client.isValidPhone(phone)) {
            System.out.println("Cancelled: Invalid phone format. Need correct CIS code and digit count.");
            return;
        }

        System.out.print("Enter delivery address: ");
        String address = scanner.nextLine().trim();

        if (name.isEmpty() || address.isEmpty()) {
            System.out.println("Cancelled: Name and address are required.");
            return;
        }

        System.out.println("\nAvailable Products in Warehouse:");
        List<Product> products = orderService.getWarehouse();
        for (int i = 0; i < products.size(); i++) {
            System.out.println((i + 1) + ". " + products.get(i));
        }
        System.out.print("Select product number: ");
        int prodChoice;
        try {
            prodChoice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (prodChoice < 0 || prodChoice >= products.size()) {
                System.out.println("Cancelled: Invalid product selection.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Cancelled: Invalid input.");
            return;
        }

        Product selectedProduct = products.get(prodChoice);

        System.out.print("Enter quantity (integer): ");
        int qty;
        try {
            qty = Integer.parseInt(scanner.nextLine().trim());
            if (qty <= 0) {
                System.out.println("Quantity must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Cancelled: Enter a valid integer.");
            return;
        }

        if (!selectedProduct.decreaseQuantity(qty)) {
            System.out.println(
                    "Cancelled: Not enough stock! Only " + selectedProduct.getAvailableQuantity() + " items left.");
            return;
        }

        Client client = new Client(name, phone);
        Order order = new Order(orderService.getNextId(), client, address);
        order.addItem(new OrderItem(selectedProduct.getName(), qty, selectedProduct.getPrice()));

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
        System.out.println("1. Standard courier delivery");
        System.out.println("2. Express delivery");
        System.out.println("3. Pickup (0 rub)");
        System.out.print("Your choice: ");
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
                throw new IllegalArgumentException("Invalid method choice.");
        }

        order.setDeliveryMethod(method);
        System.out.println("Method applied to order #" + order.getId());
    }

    private void assignCourier() {
        Order order = selectOrder();
        if (order == null)
            return;

        System.out.println("\nAvailable Couriers:");
        List<Courier> couriers = orderService.getCouriers();
        for (int i = 0; i < couriers.size(); i++) {
            System.out.println((i + 1) + ". " + couriers.get(i));
        }

        System.out.print("Select courier number: ");
        try {
            int courierChoice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (courierChoice < 0 || courierChoice >= couriers.size()) {
                System.out.println("Error: Invalid choice.");
                return;
            }
            Courier courier = couriers.get(courierChoice);
            order.assignCourier(courier);
            System.out.println("Courier " + courier.getName() + " assigned to order #" + order.getId());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
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
                throw new IllegalArgumentException("Invalid status choice.");
        }

        if (nextStatus == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.DELIVERED
                && order.getStatus() != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                orderService.getWarehouse().stream()
                        .filter(p -> p.getName().equalsIgnoreCase(item.getProductName()))
                        .findFirst()
                        .ifPresent(p -> p.increaseQuantity(item.getQuantity()));
            }
            System.out.println("Товары из заказа успешно возвращены на склад.");
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
