package org.example.processing.warehouse;

import org.example.annotation.WarehouseAction;
import org.example.annotation.simple.LogExecutionTime;
import org.example.annotation.simple.ThreadSafe;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ThreadSafe
public class WarehouseService {
    private final HashMap<String, Integer> quantities = new HashMap<>();
    private final HashMap<String, Double> prices = new HashMap<>();
    private final HashMap<String, Set<String>> groups = new HashMap<>();

    @WarehouseAction(
            commandId = 1,
            description = "Get product quantity"
    )
    public int getStock(String product) {
        return quantities.getOrDefault(product, 0);
    }

    @WarehouseAction(
            commandId = 2,
            description = "Add products"
    )
    @LogExecutionTime(logArguments = true)
    public synchronized int addProducts(String product, int quantity) {
        int current = quantities.getOrDefault(product, 0);
        int updated = current + quantity;
        quantities.put(product, updated);
        return updated;
    }

    @WarehouseAction(
            commandId = 3,
            description = "Deduct products"
    )
    @LogExecutionTime(logArguments = true)
    public synchronized int deductProducts(String product, int quantityToDeduct) {
        int current = quantities.getOrDefault(product, 0);
        int updated = Math.max(0, current - quantityToDeduct);
        quantities.put(product, updated);
        return updated;
    }

    @WarehouseAction(
            commandId = 4,
            description = "Add group of products"
    )
    @LogExecutionTime(logArguments = true)
    public synchronized void addGroup(String groupName) {
        groups.putIfAbsent(groupName, ConcurrentHashMap.newKeySet());
    }

    @WarehouseAction(
            commandId = 5,
            description = "Add product to the group"
    )
    @LogExecutionTime(logArguments = true)
    public synchronized void addProductToGroup(String groupName, String product) {
        groups.computeIfAbsent(groupName, k -> ConcurrentHashMap.newKeySet())
                .add(product);
    }

    public synchronized void setPrice(String product, double price) {
        prices.put(product, price);
    }

    public synchronized Double getPrice(String product) {
        return prices.get(product);
    }
}
