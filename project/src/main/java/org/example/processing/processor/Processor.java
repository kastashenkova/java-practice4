package org.example.processing.processor;

import org.example.encryption.Message;
import org.example.processing.SharedQueue;
import org.example.processing.warehouse.CommandResult;
import org.example.processing.warehouse.WarehouseCommand;
import org.example.processing.warehouse.WarehouseService;

public class Processor implements Runnable {
    private final SharedQueue<Message> inputQueue;
    private final SharedQueue<Message> outputQueue;
    private final WarehouseService warehouseService;
    private volatile boolean active = true;

    public Processor(SharedQueue<Message> inputQueue,
                     SharedQueue<Message> outputQueue,
                     WarehouseService warehouseService) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.warehouseService = warehouseService;
    }

    public void process(Message message) {
        WarehouseCommand command = WarehouseCommand.parse(
                message.commandId(), message.messageString());
        CommandResult result = execute(command);

        String responseText = result.success()
                ? "OK: " + result.message()
                : "ERROR: " + result.message();

        Message response = new Message(
                message.uniqueIdentifier(),
                message.messageNumber(),
                message.commandId(),
                message.userId(),
                responseText
        );

        try {
            outputQueue.produce(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private CommandResult execute(WarehouseCommand command) {
        try {
            return switch (command.type()) {
                case GET_PRODUCT_QUANTITY -> {
                    String product = command.params()[0];
                    int quantity = warehouseService.getStock(product);
                    yield CommandResult.success(product + " quantity = " + quantity);
                }
                case ADD_PRODUCTS -> {
                    String product = command.params()[0];
                    int quantity = Integer.parseInt(command.params()[1]);
                    int total = warehouseService.addProducts(product, quantity);
                    yield CommandResult.success("Added " + quantity + " of " + product + ", total = " + total);
                }
                case DEDUCT_PRODUCTS -> {
                    String product = command.params()[0];
                    int quantity = Integer.parseInt(command.params()[1]);
                    int remainder = warehouseService.deductProducts(product, quantity);
                    yield CommandResult.success("Deducted " + quantity + " of " + product + ", remainder =" + remainder);
                }
                case ADD_GROUP -> {
                    warehouseService.addGroup(command.params()[0]);
                    yield CommandResult.success("Group created: " + command.params()[0]);
                }
                case ADD_PRODUCT_NAME_TO_GROUP -> {
                    warehouseService.addProductToGroup(command.params()[0], command.params()[1]);
                    yield CommandResult.success("Added " + command.params()[1] + " to group " + command.params()[0]);
                }
                case SET_PRODUCT_PRICE -> {
                    double price = Double.parseDouble(command.params()[1]);
                    warehouseService.setPrice(command.params()[0], price);
                    yield CommandResult.success("New " + command.params()[0] + " price = " + price);
                }
            };
        } catch (Exception e) {
            return CommandResult.error("Command failed: " + command);
        }
    }

    @Override
    public void run() {
        while (active && !Thread.currentThread().isInterrupted()) {
            try {
                process(inputQueue.consume());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stop() {
        active = false;
    }
}
