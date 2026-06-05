package org.example.processing.warehouse;

public record WarehouseCommand(CommandType type, String[] params) {

    public static WarehouseCommand parse(int commandId, String messageString) {
        CommandType type = CommandType.fromCode(commandId);
        String[] params = (messageString == null || messageString.isBlank())
                ? new String[0]
                : messageString.split(":");
        return new WarehouseCommand(type, params);
    }
}
